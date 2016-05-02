/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.Animator;
import ij.plugin.Zoom;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import io.scif.services.DatasetIOService;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD HumanEye Validation", label="")
public class HumanEyeValidation implements Command {
    
    @Parameter
    CommandService commandService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    UIService uiService;
    
    @Parameter(type = ItemIO.INPUT)
    File recordFile;
    
    @Parameter(type = ItemIO.INPUT)
    String stackDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String maskDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String eyeValidationOutDir;
    
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
            List<CSVRecord> records = csvFileParser.getRecords();
            records.remove(0);
            List<MyCellRecord> cellRecordList  = new ArrayList<>();
            ArrayList availableRecords = new ArrayList();
            
            for (CSVRecord record : records) {
                int index = Integer.parseInt(record.get("index"));
                float X = Float.parseFloat(record.get("X"));
                float Y = Float.parseFloat(record.get("Y"));
                String label = record.get("Label");
                cellRecordList.add(new MyCellRecord(index, X, Y, label));
                availableRecords.add(availableRecords.size());
            }
            
            Random random = new Random();
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            for(int i = 0; i < cellRecordList.size(); i++){
            
                int randInt = random.nextInt(availableRecords.size());
                MyCellRecord record = cellRecordList.get((int) availableRecords.get(randInt));
                availableRecords.remove(randInt);
                
            
                String[] split = record.getLabel().split(":");
                String imageFile = split[0];
                int sliceNumber = Integer.parseInt(split[1]);
                
                String maskFileName = imageFile.split("\\.")[0] + ".mat";
                System.out.println(record.getLabel());
                System.out.println(maskFileName);
                File maskFile = new File(maskDirPath + "/" + maskFileName);
                
                promise = commandService.run(LoadSegmentationMaskCommand.class, true, "maskFile", maskFile);
                promiseContent = promise.get();
                Dataset inputMaskDataset = (Dataset) promiseContent.getOutput("maskDataset");

                promise = commandService.run(GetBinaryMaskFromSegmentationMask.class, true, "inDataset", inputMaskDataset);
                promiseContent = promise.get();
                Dataset binaryMaskDataset = (Dataset) promiseContent.getOutput("outDataset");
                
                System.out.println(binaryMaskDataset);     
                
                ImagePlus maskImp = ImageJ1PluginAdapter.unwrapDataset(binaryMaskDataset);
                ManyBlobs allBlobs = new ManyBlobs(maskImp);
                allBlobs.findConnectedComponents();
                System.out.println(allBlobs.size());
                Blob specificBlob = allBlobs.getSpecificBlob((int) record.getX(), (int)record.getY());

                Polygon p = specificBlob.getOuterContour();
                int n = p.npoints;
                float[] x = new float[p.npoints];
                float[] y = new float[p.npoints]; 

                for (int j=0; j<n; j++) {
                    x[j] = p.xpoints[j]+0.5f;
                    y[j] = p.ypoints[j]+0.5f;
                }
                Roi roi = new PolygonRoi(x,y,n,Roi.POLYGON);
                
                Dataset dataset = ioService.open(stackDirPath + "/" + imageFile);
                uiService.show(dataset);

                Animator animator = new Animator();
                for(int j = 1; j < 7; j ++){
                    animator.run("next");
                }

                ImagePlus imp = ImageJ1PluginAdapter.unwrapDataset(dataset);
                ImageProcessor processor = imp.getProcessor();

                RoiManager roiManager =  RoiManager.getInstance();
                if (roiManager == null)
                    roiManager = new RoiManager();

                roiManager.addRoi(roi);
                roiManager.select(0);

                Zoom zoom = new Zoom();
                zoom.run("to");
                zoom.run("out");
                zoom.run("out");
                zoom.run("out");
                zoom.run("out");

                MyContrastAjuster ca = new MyContrastAjuster();
                ca.run("");

                for(ActionListener a: ca.resetB.getActionListeners()) {
                    a.actionPerformed(new ActionEvent((Object) ca.resetB, ActionEvent.ACTION_PERFORMED, ""));
                }
                ca.done = true;
                ca.close();
                promise = commandService.run(HumanEyeValidationWindow.class, true);
                promiseContent = promise.get();

                boolean dead = (boolean) promiseContent.getOutput("outdead");
                boolean overlap = (boolean) promiseContent.getOutput("outoverlap");
                boolean outfocus = (boolean) promiseContent.getOutput("outoutfocus");
                boolean good = (boolean) promiseContent.getOutput("outgood");
                boolean stop = (boolean) promiseContent.getOutput("outstop");
                
                record.setDead(dead);
                record.setOverlap(overlap);
                record.setOutfocus(outfocus);
                record.setGood(good);
                record.setValidated(true);          

                roiManager.deselect(roi);
                roiManager.removeAll();

                System.out.println(i);
                ij.WindowManager.closeAllWindows();
                
                if(stop){
                    break;
                }
            }
            
            Writer writer = new FileWriter(eyeValidationOutDir + "/" + recordFile.getName() + "_validated.csv");
            
            CSVFormat format = CSVFormat.DEFAULT.withHeader("index", "X", "Y", "label", "dead", "overlap", "outfocus", "good");
            CSVPrinter csvFilePrinter = new CSVPrinter(writer, format);
            
            for(MyCellRecord record:cellRecordList){
                if(record.isValidated()){
                    List dataRecord = new ArrayList();
                    dataRecord.add(String.valueOf(record.getIndex()));
                    dataRecord.add(String.valueOf(record.getX()));
                    dataRecord.add(String.valueOf(record.getY()));
                    dataRecord.add(record.getLabel());
                    dataRecord.add(String.valueOf(record.isDead()));
                    dataRecord.add(String.valueOf(record.isOverlap()));
                    dataRecord.add(String.valueOf(record.isOutfocus()));
                    dataRecord.add(String.valueOf(record.isGood()));
                    csvFilePrinter.printRecord(dataRecord);
                }
                
            }

            
            writer.flush();
            writer.close();
            csvFilePrinter.close();
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HumanEyeValidation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HumanEyeValidation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(HumanEyeValidation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}