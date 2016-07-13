/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.measure;

import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import java.awt.Point;
import java.awt.Polygon;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.annotation.AnnotationCommand;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.roi.ComputeConvexHullRoi;
import zmbh.commands.ImageJ1PluginAdapter;


/**
 *
 * @author Guillaume
 */

@Plugin(type = Command.class)
public class MakeMeasurementsV3 implements Command {

    @Parameter
    CommandService cmdService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset inDataset;
    
    @Parameter(type = ItemIO.INPUT)
    CellXseed cellxSeed;
    
    @Parameter(type = ItemIO.INPUT)
    Analyzer analyzer;
    
    @Parameter(type = ItemIO.OUTPUT)
    ResultsTable res;
    
    
    
    @Override
    public void run() {
        
        //Roi roi = computeRoi((int) inDataset.dimension(1), cellxSeed.getPerimeterPixelListIndex());
        Future<CommandModule> promise;
        CommandModule promiseContent;
        Roi roi = null;
        try {
            promise = cmdService.run(ComputeConvexHullRoi.class, false, "imgHeigth", (int) inDataset.dimension(1), "perimeterPixelListIndex", cellxSeed.getPerimeterPixelListIndex());
            promiseContent = promise.get();
            roi = (Roi) promiseContent.getOutput("roi");
        } catch (InterruptedException ex) {
            Logger.getLogger(AnnotationCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(AnnotationCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ImagePlus inDatasetImp = ImageJ1PluginAdapter.unwrapDataset(inDataset);
        inDatasetImp.setRoi(roi);
        
        analyzer.setup("", inDatasetImp);
        ResultsTable resultTable = Analyzer.getResultsTable();
        resultTable.showRowNumbers(false);
        resultTable.update(Analyzer.getMeasurements(), inDatasetImp, null);
        resultTable.reset();
        
               
        for(int sliceNumber = 0; sliceNumber < inDataset.dimension(2); sliceNumber++){
            inDatasetImp.setPosition(sliceNumber+1, 1, 1);
            analyzer.measure();
            resultTable.show("Results");
        }
 
        res = (ResultsTable) resultTable.clone();

    }
    
    /* // OLD Version
    private Roi computeRoi(int imgHeigth, double[] perimeterPixelListIndex){
        int[] xPoints = new int[perimeterPixelListIndex.length];
        int[] yPoints = new int[perimeterPixelListIndex.length];
        int nPoints = perimeterPixelListIndex.length;
        ArrayList<Point> pointArray = new ArrayList<>();

        for(int i = 0; i < perimeterPixelListIndex.length; i++){
            pointArray.add(new Point((int) (perimeterPixelListIndex[i] / imgHeigth), (int) (perimeterPixelListIndex[i] % imgHeigth)));
        }

        ArrayList<Point> sortedPointArray = new ArrayList<>();
        sortedPointArray.add(pointArray.remove(0));

        while(pointArray.size() > 0){
            Point currentPoint = sortedPointArray.get(sortedPointArray.size() - 1);
            Point nextPoint = null;
            for(int i = 0; i < pointArray.size(); i++){
                double dist = currentPoint.distance(pointArray.get(i));
                if(nextPoint == null || dist < currentPoint.distance(nextPoint)){
                    nextPoint = pointArray.get(i);
                }
            }

            sortedPointArray.add(pointArray.remove(pointArray.indexOf(nextPoint)));
        }

        for(int i = 0; i < sortedPointArray.size(); i++){
            xPoints[i] = sortedPointArray.get(i).x;
            yPoints[i] = sortedPointArray.get(i).y;
        }

        Polygon p = new Polygon(xPoints, yPoints, nPoints);
        PolygonRoi roi = new PolygonRoi(p, Roi.POLYGON);
        return roi;
    }
    */
    private Roi computeRoi(int imgHeigth, double[] perimeterPixelListIndex){
        
        
        ArrayList<Point> pointArray = new ArrayList<>();

        for(int i = 0; i < perimeterPixelListIndex.length; i++){
            pointArray.add(new Point((int) (perimeterPixelListIndex[i] / imgHeigth), (int) (perimeterPixelListIndex[i] % imgHeigth)));
        }

        ArrayList<Point> sortedPointArray = new ArrayList<>();
        sortedPointArray.add(pointArray.remove(0));

        while(pointArray.size() > 0){
            Point currentPoint = sortedPointArray.get(sortedPointArray.size() - 1);
            Point nextPoint = null;
            ArrayList<Point> pixelArray = new ArrayList<>();
            for(int i = 0; i < pointArray.size(); i++){
                double dist = currentPoint.distance(pointArray.get(i));
                if(nextPoint == null){         
                    if(pointArray.size() == 1){
                        if(sortedPointArray.get(0).distance(pointArray.get(0)) < sortedPointArray.get(0).distance(sortedPointArray.get(sortedPointArray.size() - 1))){
                            nextPoint = pointArray.get(0);
                        }
                        else{
                            pixelArray.add(pointArray.get(0));
                        }
                    }
                    else{
                        nextPoint = pointArray.get(i);
                    }
                    
                    //pixelArray.add(nextPoint);
                }
                else if( dist < currentPoint.distance(nextPoint)){
                    pixelArray.clear();
                    nextPoint = pointArray.get(i);
                    //pixelArray.add(nextPoint);
                }
                else if(dist == currentPoint.distance(nextPoint)){
                    pixelArray.add(pointArray.get(i));
                }
            }
            if(nextPoint != null){
                sortedPointArray.add(pointArray.remove(pointArray.indexOf(nextPoint)));
            }
            
            pointArray.removeAll(pixelArray);
        }

        
        int[] xPoints = new int[sortedPointArray.size()];
        int[] yPoints = new int[sortedPointArray.size()];
        int nPoints = sortedPointArray.size();
        for(int i = 0; i < sortedPointArray.size(); i++){
            xPoints[i] = sortedPointArray.get(i).x;
            yPoints[i] = sortedPointArray.get(i).y;
        }

        Polygon p = new Polygon(xPoints, yPoints, nPoints);
        PolygonRoi roi = new PolygonRoi(p, Roi.POLYGON);
        return roi;
    }
    
}