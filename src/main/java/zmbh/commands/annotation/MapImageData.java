/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import ij.gui.Roi;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import zmbh.commands.roi.ComputeConvexHullRoi;
import zmbh.commands.roi.ConvertPixelIndexToPoint;
import zmbh.commands.segmentation.CellRecord;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.segmentation.LoadCellRecordList;
import zmbh.commands.segmentation.LoadCellXseedList;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class MapImageData implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter(type = ItemIO.INPUT)
    File recordClassFile;
    
    @Parameter(type = ItemIO.INPUT)
    File cellFile;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset data;
    
    @Parameter(type = ItemIO.OUTPUT)
    Map<CellRecord, Roi> roimap;
    
    @Parameter(type = ItemIO.OUTPUT)
    Map<CellRecord, Dataset> datamap;
    
    
    @Override
    public void run() {
        
        try {
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            // Load record list
            List<CellRecord> cellRecordList  = new ArrayList<>();           
            promise = cmdService.run(LoadCellRecordList.class, true, "recordClassFile", recordClassFile);
            promiseContent = promise.get();
            cellRecordList = (List<CellRecord>) promiseContent.getOutput("cellRecordList");
            
            // Load roi list
            promise = cmdService.run(LoadCellXseedList.class, false, "cellFile", cellFile);
            promiseContent = promise.get();
            ArrayList<CellXseed> cellxSeedList = (ArrayList<CellXseed>) promiseContent.getOutput("cellxSeedList");
            ArrayList<Roi> roiList = new ArrayList<>();
            for(CellXseed cellxSeed : cellxSeedList){
                try {
                    
                    promise = cmdService.run(ConvertPixelIndexToPoint.class, false, "imgHeigth", (int) data.dimension(1), "perimeterPixelListIndex", cellxSeed.getPerimeterPixelListIndex());
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
            // map rcords to rois
            roimap = new HashMap<>();
            for(CellRecord record : cellRecordList){
                for(Roi roi : roiList){
                    if(roi.contains((int) record.getX(), (int)record.getY())){
                        roimap.put(record, roi);
                        roiList.remove(roi);
                        break;
                    }
                }
            }
            
            // map records to image data
            datamap = new HashMap<>();
            for(CellRecord record : cellRecordList){
                datamap.put(record, data);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MapImageData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MapImageData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}