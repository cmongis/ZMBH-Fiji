/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.correction;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import io.scif.services.DatasetIOService;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.ImageJ1PluginAdapter;
import zmbh.commands.roi.ComputeConvexHullRoi;
import zmbh.commands.roi.ConvertPixelIndexToPoint;
import zmbh.commands.roi.GetBackGroundRoi;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.segmentation.LoadCellXseedList;

/**
 *
 * @author User
 */


@Plugin(type = Command.class)
public class GetBackgroundCorrectedImages implements Command {
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter(type = ItemIO.INPUT)
    File stackDir;
    
    @Parameter(type = ItemIO.INPUT)
    File cellDir;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outStack;
    
    @Override
    public void run() {
        
        File[] cellFileList = cellDir.listFiles((File pathname) -> pathname.getName().endsWith(".mat"));
        File[] fileStackList = stackDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
                
        if(fileStackList.length == cellFileList.length){
            for(int i = 0; i < fileStackList.length; i++){
                try {
                    Dataset inDataset = ioService.open(fileStackList[i].getPath());
                    
                    //Load Segmentation info
                    Future<CommandModule> promise = cmdService.run(LoadCellXseedList.class, false, "cellFile", cellFileList[i]);
                    CommandModule promiseContent = promise.get();
                    ArrayList<CellXseed> cellxSeedList = (ArrayList<CellXseed>) promiseContent.getOutput("cellxSeedList");
                    
                    // Get all ROI on the image
                    ArrayList<Roi> roiList = new ArrayList<>();
                    for(CellXseed cellxSeed : cellxSeedList){
                        promise = cmdService.run(ConvertPixelIndexToPoint.class, false, "imgHeigth", (int) inDataset.dimension(1), "perimeterPixelListIndex", cellxSeed.getPerimeterPixelListIndex());
                        promiseContent = promise.get();
                        List<Point> pointArray = (List<Point>) promiseContent.getOutput("pointArray");
                        promise = cmdService.run(ComputeConvexHullRoi.class, false, "pointArray", pointArray);
                        promiseContent = promise.get();
                        Roi roi = (Roi) promiseContent.getOutput("roi");
                        roiList.add(roi);
                    }
                    
                    //Get the ROI of the full image
                    ShapeRoi fullImgRoi = new ShapeRoi(new Roi(0,0, inDataset.dimension(0), inDataset.dimension(1)));
                    
                    //Get the background ROI
                    promise = cmdService.run(GetBackGroundRoi.class, false, "fullImgRoi", fullImgRoi, "roiList", roiList);
                    promiseContent = promise.get();
                    ShapeRoi backgroundRoi = (ShapeRoi) promiseContent.getOutput("backgroundRoi");

                    Analyzer analyzer = new Analyzer();
                    Analyzer.setMeasurement(Measurements.MEAN, true);

                    ImagePlus inDatasetImp = ImageJ1PluginAdapter.unwrapDataset(inDataset);
                    
                    //Perform measurements on background ROI
                    inDatasetImp.setRoi(backgroundRoi);
                    analyzer.setup("", inDatasetImp);
                    ResultsTable resultTable = Analyzer.getResultsTable();
                    resultTable.showRowNumbers(false);
                    resultTable.update(Analyzer.getMeasurements(), inDatasetImp, null);
                    resultTable.reset();
                    
                    ArrayList<Double> backGroundvalueList = new ArrayList<>();
                    System.out.println("current stack = " + inDataset.getName());
                    for(int sliceNumber = 0; sliceNumber < inDataset.dimension(2); sliceNumber++){
                        inDatasetImp.setPosition(sliceNumber+1, 1, 1);
                        analyzer.measure();
                        resultTable.show("Results");
                        double backgroundValue = resultTable.getValue("Mean", sliceNumber);                      
                        backGroundvalueList.add(backgroundValue);                        
                        System.out.println("backgroundValue = " + backgroundValue);
                    }
                    
                    inDatasetImp.deleteRoi();      
                    
                    long inputWidth = inDataset.dimension(0);
                    long inputHeight = inDataset.dimension(1);
                    
                    //Create a new Dataset
                    //Copy pixel values
                    //Substract background averageValue
                    long[] dimensions = new long[inDataset.numDimensions()];
                    inDataset.dimensions(dimensions);
                    AxisType[] axes = new AxisType[]{Axes.X, Axes.Y, Axes.Z};
                    outStack = datasetService.create(dimensions, inDataset.getName(), axes, inDataset.getType().getBitsPerPixel(), inDataset.isSigned(), !inDataset.isInteger(), false);
                    RandomAccess<RealType<?>> inputRandomAccess = inDataset.randomAccess();
                    RandomAccess<RealType<?>> outRandomAccess = outStack.randomAccess();
                    
                    for(int k = 0; k < backGroundvalueList.size(); k++){
                        inputRandomAccess.setPosition(new long[]{0, 0, k});
                        outRandomAccess.setPosition(new long[]{0, 0, k});
                        
                        for(int x = 0; x < inputWidth; x++) {
                            for(int y = 0; y < inputHeight; y++) {
                                inputRandomAccess.setPosition(x,0);
                                inputRandomAccess.setPosition(y,1);

                                outRandomAccess.setPosition(x,0);
                                outRandomAccess.setPosition(y,1);

                                float oldValue = inputRandomAccess.get().getRealFloat();
                                float newValue = (float) (oldValue - backGroundvalueList.get(k));

                                outRandomAccess.get().setReal(newValue);
                            }
                        }
                    }
                    
                    
                    ioService.save(outStack, saveDir + "/" + outStack.getName());
                } catch (IOException | InterruptedException | ExecutionException ex) {
                    Logger.getLogger(GetBackgroundCorrectedImages.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
}