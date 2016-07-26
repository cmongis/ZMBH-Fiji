
package zmbh.commands.measure;

import zmbh.commands.correction.ChromaCorrect;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import io.scif.services.DatasetIOService;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;
import zmbh.commands.util.AddSliceToStack;
import zmbh.commands.annotation.AnnotationCommand;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.roi.ComputeConvexHullRoi;
import zmbh.commands.roi.ConvertPixelIndexToPoint;
import zmbh.commands.roi.GetBackGroundRoi;
import zmbh.commands.ImageJ1PluginAdapter;
import zmbh.commands.segmentation.LoadCellXseedList;
import zmbh.commands.roi.RoiDataset;

/**
 *
 * @author Potier Guillaume, 2016
 */


@Plugin(type = Command.class, menuPath = "Dev-commands>Measure>CMD RUN Measurements V4", label="")
public class RunMeasurementsV4 implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    ModuleService mService;
    
    @Parameter
    UIService ui;
    
    @Parameter
    PluginService pluginService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter(type = ItemIO.INPUT)
    File inDatasetFile;
    
    @Parameter(type = ItemIO.INPUT)
    File cellFile;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    
    @Override
    public void run() {
        Date startRun = new Date();
        Future<CommandModule> promise;
        CommandModule promiseContent;
        try {
            Dataset extentedStack = ioService.open(inDatasetFile.getPath());
            
            // Load roi list
            promise = cmdService.run(LoadCellXseedList.class, false, "cellFile", cellFile);
            promiseContent = promise.get();
            ArrayList<CellXseed> cellxSeedList = (ArrayList<CellXseed>) promiseContent.getOutput("cellxSeedList");
            
            
            ArrayList<Roi> roiList = new ArrayList<>();       
            for(CellXseed cellxSeed : cellxSeedList){
                try {

                    promise = cmdService.run(ConvertPixelIndexToPoint.class, false, "imgHeigth", (int) extentedStack.dimension(1), "perimeterPixelListIndex", cellxSeed.getPerimeterPixelListIndex());
                    promiseContent = promise.get();
                    List<Point> pointArray = (List<Point>) promiseContent.getOutput("pointArray");

                    promise = cmdService.run(ComputeConvexHullRoi.class, false, "pointArray", pointArray);
                    promiseContent = promise.get();
                    Roi roi = (Roi) promiseContent.getOutput("roi");
                    roiList.add(roi);
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(AnnotationCommand.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(AnnotationCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            
            // Set measurements parameters
            Analyzer analyzer = new Analyzer();
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
                                    
            // Create a list of results : one for each image            
            ArrayList<ArrayList<String>> results = new ArrayList<>();
            for(int sliceNumber = 0; sliceNumber < extentedStack.dimension(2); sliceNumber++){
                results.add(new ArrayList<>());
            }
            
            ImagePlus inDatasetImp = ImageJ1PluginAdapter.unwrapDataset(extentedStack);
            
            ArrayList<Long> arrayTime = new ArrayList<>();
            ArrayList<Long> arrayTime1 = new ArrayList<>();
            
            for(Roi roi : roiList){
                long startRoiDataset = System.nanoTime();
                // Get the square bounding dataset of 1 roi
                // Used to compute Haralick features
                promise = cmdService.run(RoiDataset.class, true, "inDataset", extentedStack, "roi", roi);
                promiseContent = promise.get();
                Dataset outDataset = (Dataset) promiseContent.getOutput("outDataset");
                //ImagePlus outDatasetImp = ImageJ1PluginAdapter.unwrapDataset(outDataset);
                
                arrayTime1.add((long) promiseContent.getOutput("time1"));
                long stopRoiDatset = System.nanoTime();
                arrayTime.add(stopRoiDatset - startRoiDataset);
                
                // Run measures
                promise = cmdService.run(MakeMeasurementsV4.class, true, "inDatasetImp", inDatasetImp, "nbSlice", extentedStack.dimension(2), "roi", roi, "analyzer", analyzer, "roiDataset", outDataset);
                promiseContent = promise.get();
                ResultsTable resultsTable = (ResultsTable) promiseContent.getOutput("res");
                for(int i = 0; i < resultsTable.size(); i++){
                    results.get(i).add(resultsTable.getRowAsString(i));
                }     
                //System.gc();
            }
            
            double t1 = arrayTime1.stream().mapToLong(L -> L.longValue()).average().getAsDouble();
            System.out.println("Time1 = " + t1);
            System.out.println("Total = " + arrayTime.stream().mapToLong(L -> L.longValue()).average().getAsDouble());
            System.out.println("");
            
            cmdService.dispose();
            
            
            // save in a file
            Writer writer;
            CSVFormat format;
            CSVPrinter csvFilePrinter;
            for(int i = 0; i < results.size(); i++){               
                writer = new FileWriter(saveDir.getPath() + "/" + extentedStack.getName() + "_slice" + i + ".csv");
                format = CSVFormat.DEFAULT.withHeader(Analyzer.getResultsTable().getHeadings());
                csvFilePrinter = new CSVPrinter(writer, format);
                for(String records : results.get(i)){
                    String[] split = records.split("\\t");
                    csvFilePrinter.printRecord(split);
                }
                //writer.flush();
                //writer.close();
                csvFilePrinter.flush();
                csvFilePrinter.close();
            }
            
            /*
            // Same measurement procedure for the background of the image
            // The background roi is the inverse of the union of all rois in the image
            ArrayList<ArrayList<String>> backgroundResults = new ArrayList<>();
            for(int sliceNumber = 0; sliceNumber < extentedStack.dimension(2); sliceNumber++){
                backgroundResults.add(new ArrayList<>());
            }
            ShapeRoi fullImgRoi = new ShapeRoi(new Roi(0,0, extentedStack.dimension(0), extentedStack.dimension(1)));
            
            promise = cmdService.run(GetBackGroundRoi.class, false, "fullImgRoi", fullImgRoi, "roiList", roiList);
            promiseContent = promise.get();
            ShapeRoi backgroundRoi = (ShapeRoi) promiseContent.getOutput("backgroundRoi");
            
            promise = cmdService.run(MakeMeasurementsV4.class, false, "inDatasetImp", inDatasetImp, "nbSlice", extentedStack.dimension(2), "roi", backgroundRoi, "analyzer", analyzer);

            promiseContent = promise.get();
            ResultsTable resultsTable = (ResultsTable) promiseContent.getOutput("res");
            for(int i = 0; i < backgroundResults.size(); i++){
                    backgroundResults.get(i).add(resultsTable.getRowAsString(i));
            }
             // save in a file
            for(int i = 0; i < backgroundResults.size(); i++){               
                writer = new FileWriter(saveDir.getPath() + "/" + extentedStack.getName() + "_slice" + i + "_background.csv");
                format = CSVFormat.DEFAULT.withHeader(Analyzer.getResultsTable().getHeadings());
                csvFilePrinter = new CSVPrinter(writer, format);
                for(String records : backgroundResults.get(i)){
                    String[] split = records.split("\\t");
                    csvFilePrinter.printRecord(split);
                }
                writer.flush();
                writer.close();
                csvFilePrinter.close();
            }
            */
            Date stopRun = new Date();
            System.out.println("RunMeasurementsV4 exec time : " + (stopRun.getTime() - startRun.getTime())/1000 + "s");
            System.out.println("RunMeasurementsV4 blobs processed : " + roiList.size());            
            //datasetService.getDatasets().remove(extentedStack);
            
            

        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(RunMeasurementsV4.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}