/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.roi.ComputeConvexHullRoi;
import zmbh.commands.roi.ConvertPixelIndexToPoint;
import zmbh.commands.ImageJ1PluginAdapter;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class AnnotationCommand2 implements Command {

    @Parameter
    CommandService cmdService;
       
    @Parameter(type = ItemIO.INPUT)
    Dataset inDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outDataset;
    
    @Parameter(type = ItemIO.INPUT)
    ArrayList<CellXseed> cellxSeedList;
    
    
    
    @Override
    public void run() {
        Future<CommandModule> promise;
        CommandModule promiseContent;
        ImagePlus imagePlus = ImageJ1PluginAdapter.unwrapDataset(inDataset);    
        
        // Get a list of imageProcessor for each image in ths stack
        ArrayList<ImageProcessor> impList = new ArrayList<>();
        for(int i = 0; i < inDataset.dimension(2); i++){
            imagePlus.setPosition(i+1, 1, 1);
            imagePlus.resetDisplayRange();
            ImageProcessor imp = imagePlus.getProcessor().convertToRGB();
            imp.setColor(Color.BLACK);
            imp.setLineWidth(1);
            impList.add(imp);
        }
        imagePlus.changes = false;
        imagePlus.close();
        
        
        // Get all roi on the image
        ArrayList<Roi> roiList = new ArrayList<>();         
        for(CellXseed cellxSeed : cellxSeedList){
            try {                
                promise = cmdService.run(ConvertPixelIndexToPoint.class, false, "imgHeigth", (int) inDataset.dimension(1), "perimeterPixelListIndex", cellxSeed.getPerimeterPixelListIndex());
                promiseContent = promise.get();
                List<Point> pointArray = (List<Point>) promiseContent.getOutput("pointArray");
                
                promise = cmdService.run(ComputeConvexHullRoi.class, false, "pointArray", pointArray);
                promiseContent = promise.get();
                Roi roi = (Roi) promiseContent.getOutput("roi");
                roiList.add(roi);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(AnnotationCommand2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Draw rois on the image
        // Color depends on the population the cell belongs
        for(Roi roi : roiList){
            Color color = Color.ORANGE;
            
            for(ImageProcessor imp : impList){
                imp.setColor(color);
                imp.draw(roi);
            }            
        }
        
        // Create a new stack with annotated images
        ImageStack imageStack = new ImageStack((int)inDataset.dimension(0), (int)inDataset.dimension(1), ColorModel.getRGBdefault());
        for( ImageProcessor ip : impList){
            imageStack.addSlice(ip);
        }       
        ImagePlus annotatedStack = new ImagePlus(inDataset.getName() + "- annotated", imageStack);
        annotatedStack.show();
         /*
        File savedir = new File(saveDir);
        if(savedir.isDirectory() && savedir.canWrite()){
            IJ.save(annotatedStack, savedir.getPath() + "/" + annotatedStack.getTitle() + ".tif");
        }
        */
    }
    
}