/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.measure;

import ij.ImagePlus;
import ij.blob.ManyBlobs;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import io.scif.services.DatasetIOService;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.segmentation.GetBinaryMaskFromSegmentationMask_OneBlob;
import zmbh.commands.GetMaxValueCommand;
import zmbh.commands.ImageJ1PluginAdapter;
import zmbh.commands.segmentation.LoadSegmentationMaskWithIdCommand;
import zmbh.commands.MyContrastAjuster;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class MakeMeasurementsV2 implements Command {
    
    @Parameter
    DatasetIOService datasetioService;
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    CommandService cmdService;
    
    @Parameter(type = ItemIO.INPUT)
    File inputStackFile;
    
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
    
    @Parameter(type = ItemIO.INPUT)
    File maskFile;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    static int counter;

    @Override
    public void run() {
        Date date = new Date();
        System.out.println(date.toString());
        
        Dataset inputStackDataset;
        Dataset inputMaskDataset;
        Future<CommandModule> promise;
        CommandModule promiseContent;
                
        try {

            inputStackDataset = datasetioService.open(inputStackFile.getPath());
            /*
            promise = cmdService.run(ConvertDatasetEncodingCommand.class, true, "inDataset", inputStackDataset, "targetType", targetType);
            promiseContent = promise.get();
            inputStackDataset = (Dataset) promiseContent.getOutput("outDataset");
            
            promise = cmdService.run(SlideCorrectCommand_DarkField_FlatField_AllSlices.class, true, "inputDataset", inputStackDataset, "darkfieldValue", darkfieldValue, "mCherryFlatFieldFile", mCherryFlatFieldFile, "gfpFlatFieldFile", gfpFlatFieldFile, "bfpFlatFieldFile", bfpFlatFieldFile);
            promiseContent = promise.get();
            inputStackDataset = (Dataset) promiseContent.getOutput("inputDataset");
     */       
            promise = cmdService.run(LoadSegmentationMaskWithIdCommand.class, true, "maskFile", maskFile);
            promiseContent = promise.get();
            inputMaskDataset = (Dataset) promiseContent.getOutput("maskDataset");
            

            Analyzer analyzer = new Analyzer();
            ImagePlus datasetImp = ImageJ1PluginAdapter.unwrapDataset(inputStackDataset);
            
            analyzer.setup("", datasetImp);
            
            Analyzer.setMeasurement(Measurements.AREA, true);
            Analyzer.setMeasurement(Measurements.MEAN, true);
            Analyzer.setMeasurement(Measurements.STD_DEV, true);
            Analyzer.setMeasurement(Measurements.MODE, true);
            Analyzer.setMeasurement(Measurements.MIN_MAX, true);
            Analyzer.setMeasurement(Measurements.CENTROID, true);
            Analyzer.setMeasurement(Measurements.CENTER_OF_MASS, true);
            Analyzer.setMeasurement(Measurements.PERIMETER, true);
            Analyzer.setMeasurement(Measurements.LIMIT, true);
            Analyzer.setMeasurement(Measurements.RECT, true);
            Analyzer.setMeasurement(Measurements.LABELS, true);
            Analyzer.setMeasurement(Measurements.ELLIPSE, true);
            Analyzer.setMeasurement(Measurements.CIRCULARITY, true);
            Analyzer.setMeasurement(Measurements.SHAPE_DESCRIPTORS, true);
            Analyzer.setMeasurement(Measurements.FERET, true);
            Analyzer.setMeasurement(Measurements.INTEGRATED_DENSITY, true);
            Analyzer.setMeasurement(Measurements.MEDIAN, true);
            Analyzer.setMeasurement(Measurements.SKEWNESS, true);
            Analyzer.setMeasurement(Measurements.KURTOSIS, true);
            Analyzer.setMeasurement(Measurements.AREA_FRACTION, true);
            Analyzer.setMeasurement(Measurements.SLICE, true);
            Analyzer.setMeasurement(Measurements.STACK_POSITION, true);
            
            ResultsTable resultTable = Analyzer.getResultsTable();
            resultTable.update(Analyzer.getMeasurements(), datasetImp, null);
            resultTable.reset();
            
            
            promise = cmdService.run(GetMaxValueCommand.class, true, "inDataset", inputMaskDataset);
            promiseContent = promise.get();
            float nbSegCells = (float) promiseContent.getOutput("maxValue");
            date = new Date();
            System.out.println(date.toString() + " nb Segmented cells : " + nbSegCells);
            for(float i = 1; i <= nbSegCells; i++){
                promise = cmdService.run(GetBinaryMaskFromSegmentationMask_OneBlob.class, true, "inDataset", inputMaskDataset, "blobValue", i);
                promiseContent = promise.get();
                Dataset oneBlobMaskDataset = (Dataset) promiseContent.getOutput("outDataset");
                
                MyContrastAjuster ca = new MyContrastAjuster();
                ca.run("");
                for(ActionListener a: ca.resetB.getActionListeners()) {
                    a.actionPerformed(new ActionEvent((Object) ca.resetB, ActionEvent.ACTION_PERFORMED, ""));
                }
                ca.done = true;
                ca.close();
                
                
                ImagePlus maskImp = ImageJ1PluginAdapter.unwrapDataset(oneBlobMaskDataset);
                ManyBlobs allBlobs = new ManyBlobs(maskImp);
                allBlobs.findConnectedComponents();
                date = new Date();
                System.out.println(date.toString() + " nb Blobs = " + allBlobs.size());
                
                /*
                Roi roi;
                for (int j = 0; j < allBlobs.size(); j++) {
                    Polygon p = allBlobs.get(j).getOuterContour();
                    int n = p.npoints;
                    float[] x = new float[p.npoints];
                    float[] y = new float[p.npoints];

                    for (int k=0; k<n; k++) {
                        x[k] = p.xpoints[k]+0.5f;
                        y[k] = p.ypoints[k]+0.5f;
                    }

                    roi = new PolygonRoi(x,y,n,Roi.POLYGON);

                    datasetImp.setRoi(roi);
                    

                    for(int sliceNumber = 0; sliceNumber < inputStackDataset.dimension(2); sliceNumber++){
                        datasetImp.setPosition(sliceNumber+1, 1, 1);
                        analyzer.measure();
                        resultTable.show("Results");
                    }
                    resultTable.saveAs(saveDir.getPath() + "\\record"+ counter + "_" + inputStackDataset.getName().split("\\.")[0]+ "_allslice.csv");
                    resultTable.reset();
                    counter++;
                    
                }
                */
                
                Polygon p = allBlobs.get(0).getOuterContour();
                int n = p.npoints;
                float[] x = new float[p.npoints];
                float[] y = new float[p.npoints];

                for (int k=0; k<n; k++) {
                    x[k] = p.xpoints[k]+0.5f;
                    y[k] = p.ypoints[k]+0.5f;
                }

                PolygonRoi roi = new PolygonRoi(x,y,n,Roi.POLYGON);

                datasetImp.setRoi(roi);
                for(int sliceNumber = 0; sliceNumber < inputStackDataset.dimension(2); sliceNumber++){
                        datasetImp.setPosition(sliceNumber+1, 1, 1);
                        analyzer.measure();
                        resultTable.show("Results");
                    }
                resultTable.saveAs(saveDir.getPath() + "\\record"+ counter + "_" + inputStackDataset.getName().split("\\.")[0]+ "_allslice.csv");
                resultTable.reset();
                counter++;
                ij.WindowManager.getCurrentWindow().close();
                
            }
                
            
            ij.WindowManager.closeAllWindows();
            
        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(MakeMeasurementsCommand_AllSlices.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
    public void setCounter(int number){
        counter = number;
    }
    
}