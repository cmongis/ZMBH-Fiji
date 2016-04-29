/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import ij.ImagePlus;
import ij.blob.ManyBlobs;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplay;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Get measurements", label="")
public class MakeMeasurementsCommand implements Command {

    @Parameter
    DatasetService datasetService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset dataset;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset maskDataset;
    
    @Parameter()
    int sliceNumber;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    //@Parameter
    //ImageDisplay imgDisplay;
    
    @Override
    public void run() {
        
        ImagePlus maskImp = ImageJ1PluginAdapter.unwrapDataset(maskDataset);
        ManyBlobs allBlobs = new ManyBlobs(maskImp);
        allBlobs.findConnectedComponents();
        
        Analyzer analyzer = new Analyzer();     
        ImagePlus datasetImp = ImageJ1PluginAdapter.unwrapDataset(dataset);
        
        //long[] position = new long[imgDisplay.numDimensions()];
        //imgDisplay.localize(position);
        //datasetImp.setPosition((int) position[2]+1, 1, 1);
        datasetImp.setPosition(sliceNumber+1, 1, 1);
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
        
        Roi roi;
        for (int i = 0; i < allBlobs.size(); i++) {
            Polygon p = allBlobs.get(i).getOuterContour();
            int n = p.npoints;
            float[] x = new float[p.npoints];
            float[] y = new float[p.npoints]; 
            
            for (int j=0; j<n; j++) {
                x[j] = p.xpoints[j]+0.5f;
                y[j] = p.ypoints[j]+0.5f;
            }
            
            roi = new PolygonRoi(x,y,n,Roi.POLYGON);
            
            datasetImp.setRoi(roi);
            analyzer.measure();
            resultTable.show("Results");
        }
        
        try {
            //try {
            //    resultTable.saveAs(saveDir.getPath() + "\\" + dataset.getName()+ (position[2]+1) + ".csv");
            //} catch (IOException ex) {
            //    Logger.getLogger(Commandtester_findBlobs.class.getName()).log(Level.SEVERE, null, ex);
            //}
            resultTable.saveAs(saveDir.getPath() + "\\records" + dataset.getName().split("\\.")[0]+ "_slice" +(sliceNumber+1) + ".csv");
        } catch (IOException ex) {
            Logger.getLogger(MakeMeasurementsCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        resultTable.reset();    
    }   
}