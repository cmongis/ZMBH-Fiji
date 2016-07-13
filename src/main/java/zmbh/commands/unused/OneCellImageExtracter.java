/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import zmbh.commands.util.ConvertDatasetEncodingCommand;
import zmbh.commands.segmentation.CellRecord;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.validation.HumanEyeValidation;
import zmbh.commands.correction.SliceCorrectCommand_DarkField_FlatField_AllSlices;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class OneCellImageExtracter implements Command {
    
    @Parameter
    CommandService commandService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter(type = ItemIO.INPUT)
    File recordFile;
    
    @Parameter(type = ItemIO.INPUT)
    String stackDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String outDir;
    
    @Parameter(type = ItemIO.INPUT)
    String targetType;
    
    @Parameter(type = ItemIO.INPUT)
    float darkfieldValue;
    
    @Parameter(type = ItemIO.INPUT)
    File mCherryFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File gfpFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File bfpFlatFieldFile;
    
    @Override
    public void run() {
        try {
            
            Reader in = new FileReader(recordFile.getPath());     
            CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader("index","Label", "Area", "Mean", "StdDev", "Mode",
                    "Min", "Max", "X", "Y", "XM" ,"YM", "Perim.", "BX", "BY", "Width", "Height", "Major",
                    "Minor", "Angle", "Circ.", "Feret", "IntDen", "Median", "Skew", "Kurt", "X.Area", "RawIntDen", "Slice", "FeretX",
                    "FeretY", "FeretAngle", "MinFeret", "AR", "Round", "Solidity", "MinThr", "MaxThr",
                    "Well", "WellNumber", "WellLabel");
            
            CSVParser csvFileParser = new CSVParser(in, csvFileFormat);
            //Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
            List<CSVRecord> records = csvFileParser.getRecords();
            records.remove(0);
            List<CellRecord> cellRecordList  = new ArrayList<>();
            HashSet<String> imagelabelSet = new HashSet<>();
            
            for (CSVRecord record : records) {
                int index = Integer.parseInt(record.get("index"));
                float X = Float.parseFloat(record.get("X"));
                float Y = Float.parseFloat(record.get("Y"));
                float boundingSquareWidth = Float.parseFloat(record.get("Width"));
                float boundingSquareHeight = Float.parseFloat(record.get("Height"));
                String label = record.get("Label");                
                String[] split = label.split(":");
                String imageFile = split[0];
                cellRecordList.add(new CellRecord(index, X, Y, boundingSquareWidth, boundingSquareHeight, imageFile));
                imagelabelSet.add(imageFile);
            }
            
            
            
            
            
            
            Future<CommandModule> promise;
            CommandModule promiseContent;
            for(String imagefile : imagelabelSet){                          
            
                List<CellRecord> currentCellRecordList  = new ArrayList<>();
                for(CellRecord record : cellRecordList){
                    if(record.getLabel().equals(imagefile)){
                        currentCellRecordList.add(record);
                    }
                }

                Dataset inputDataset = ioService.open(stackDirPath + "/" + imagefile);
                
                promise = commandService.run(ConvertDatasetEncodingCommand.class, true, "inDataset", inputDataset, "targetType", targetType);
                promiseContent = promise.get();
                inputDataset = (Dataset) promiseContent.getOutput("outDataset");
                promise = commandService.run(SliceCorrectCommand_DarkField_FlatField_AllSlices.class, true, "inputDataset", inputDataset, "darkfieldValue", darkfieldValue, "mCherryFlatFieldFile", mCherryFlatFieldFile, "gfpFlatFieldFile", gfpFlatFieldFile, "bfpFlatFieldFile", bfpFlatFieldFile);
                promiseContent = promise.get();
                inputDataset = (Dataset) promiseContent.getOutput("inputDataset");
                
                long[] dimensions = new long[inputDataset.numDimensions()];
                inputDataset.dimensions(dimensions);
                dimensions[0] = 100;
                dimensions[1] = 100;
                AxisType[] axisArray = new AxisType[inputDataset.numDimensions()];
                CalibratedAxis[] axes = new CalibratedAxis[inputDataset.numDimensions()];
                inputDataset.axes(axes);
                for(int i = 0; i < axes.length; i++){
                    axisArray[i] = axes[i].type();
                }
                
                for(CellRecord record : currentCellRecordList){
                    String outputDatasetName = inputDataset.getName() + "_idCell_" + record.getIndex();
                    Dataset outputDataset = datasetService.create(dimensions, outputDatasetName, axisArray, inputDataset.getType().getBitsPerPixel(), inputDataset.isSigned(), !inputDataset.isInteger());

                    RandomAccess<RealType<?>> inRa = inputDataset.randomAccess();
                    RandomAccess<RealType<?>> outRa = outputDataset.randomAccess();

                    int X = (int) record.getX();
                    int Y = (int) record.getY();

                    if(X-50 >= 0 && X+50 <= 2048 && Y-50 >= 0 && Y+50 <= 2044){
                        for(int i = 0; i < dimensions[2]; i++){
                            for(int x = 0; x < 100; x++){
                                for(int y = 0; y < 100; y++){
                                    inRa.setPosition(new long[]{X-50+x, Y-50+y, i});
                                    outRa.setPosition(new long[]{x, y, i});

                                    outRa.get().setReal(inRa.get().getRealFloat());
                                }
                            }
                        }
                        ioService.save(outputDataset, outDir+ "/" + outputDataset.getName() + ".tif");
                    }
                    
                }
                ij.WindowManager.closeAllWindows();
            }
             
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HumanEyeValidation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HumanEyeValidation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(OneCellImageExtracter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(OneCellImageExtracter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}