/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.roi;

import zmbh.commands.segmentation.LoadCellXseedList;
import zmbh.commands.segmentation.CellXseed;
import zmbh.commands.annotation.AnnotationCommand;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.frame.RoiManager;
import io.scif.services.DatasetIOService;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import zmbh.commands.ImageJ1PluginAdapter;
import zmbh.commands.MyContrastAjuster;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Roi>CMD Display cell roi", label="")
public class DisplayCellRoi implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    File inDatasetFile;
    
    @Parameter(type = ItemIO.INPUT)
    File cellFile;
    
    
    @Override
    public void run() {
        try {
            Dataset inDataset = ioService.open(inDatasetFile.getPath());
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            promise = cmdService.run(LoadCellXseedList.class, false, "cellFile", cellFile);
            promiseContent = promise.get();
            ArrayList<CellXseed> cellxSeedList = (ArrayList<CellXseed>) promiseContent.getOutput("cellxSeedList");
            
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
                    Logger.getLogger(AnnotationCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            //ShapeRoi fullImgRoi = new ShapeRoi(new Roi(0,0, inDataset.dimension(0), inDataset.dimension(1)));
            
            promise = cmdService.run(GetCellRoi.class, false, "roiList", roiList);
            promiseContent = promise.get();
            ShapeRoi cellRoi = (ShapeRoi) promiseContent.getOutput("cellRoi");
            
            ImagePlus imagePlus = ImageJ1PluginAdapter.unwrapDataset(inDataset);
            imagePlus.setRoi(cellRoi);
            imagePlus.getProcessor().setColor(Color.GREEN);
            imagePlus.getProcessor().fill(imagePlus.getRoi());
            
            imagePlus.show();
            
            MyContrastAjuster ca = new MyContrastAjuster();
            ca.run("");
            for(ActionListener a: ca.resetB.getActionListeners()) {
                a.actionPerformed(new ActionEvent((Object) ca.resetB, ActionEvent.ACTION_PERFORMED, ""));
            }
            ca.done = true;
            ca.close();
            
            RoiManager roiManager =  RoiManager.getInstance();
            if (roiManager == null)
                roiManager = new RoiManager();

            roiManager.addRoi(imagePlus.getRoi());
            roiManager.select(0);
            
        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(DisplayCellRoi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}