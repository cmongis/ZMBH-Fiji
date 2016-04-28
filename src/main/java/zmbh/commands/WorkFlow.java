/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import ij.gui.ImageWindow;
import ij.plugin.Animator;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.display.ImageDisplayService;
import net.imagej.plugins.commands.axispos.AxisPositionForward;
import net.imagej.plugins.commands.axispos.SetAxisPosition;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD START WorkFlow", label="")
public class WorkFlow implements Command {
    
    @Parameter
    DisplayService displayService;
    
    @Parameter
    ImageDisplayService imgDisplayService;
    
    @Parameter
    UIService uiService;
    
    @Parameter
    DatasetIOService datasetioService;
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    CommandService commandService;
    
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

    @Override
    public void run() {
        
        Dataset inputStackDataset;
        Dataset inputMaskDataset;
        Future<CommandModule> promise;
        CommandModule promiseContent;
                
        try {

            inputStackDataset = datasetioService.open(inputStackFile.getPath());
            promise = commandService.run(ConvertDatasetEncodingCommand.class, true, "inDataset", inputStackDataset, "targetType", targetType);
            promiseContent = promise.get();
            inputStackDataset = (Dataset) promiseContent.getOutput("outDataset");
            
            promise = commandService.run(CommandTester_flatfield.class, true, "inputDataset", inputStackDataset, "darkfieldValue", darkfieldValue, "mCherryFlatFieldFile", mCherryFlatFieldFile, "gfpFlatFieldFile", gfpFlatFieldFile, "bfpFlatFieldFile", bfpFlatFieldFile);
            promise.get();
            
            promise = commandService.run(CommandTester_matFile_mask.class, true, "maskFile", maskFile);
            promiseContent = promise.get();
            inputMaskDataset = (Dataset) promiseContent.getOutput("maskDataset");
            
            promise = commandService.run(makeBinary.class, true, "inDataset", inputMaskDataset);
            promiseContent = promise.get();
            Dataset binaryMaskDataset = (Dataset) promiseContent.getOutput("outDataset");
            
            ImageWindow window = (ImageWindow) ij.WindowManager.getWindow(ij.WindowManager.getImageTitles()[0]);
            Display<?> display = displayService.getDisplay(ij.WindowManager.getImageTitles()[0]);

            ij.WindowManager.setCurrentWindow(window);            
            displayService.setActiveDisplay(display);
            
            Animator animator = new Animator();
                    
            promise = commandService.run(SetAxisPosition.class, true, "oneBasedPosition", 1);
            promise.get();
            
            promise = commandService.run(Commandtester_findBlobs.class, true, "dataset", inputStackDataset, "maskDataset", binaryMaskDataset, "saveDir", saveDir);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true); 
            promise.get();
            animator.run("next");
            
            promise = commandService.run(Commandtester_findBlobs.class, true, "dataset", inputStackDataset, "maskDataset", binaryMaskDataset, "saveDir", saveDir);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            animator.run("next");
            
            promise = commandService.run(Commandtester_findBlobs.class, true, "dataset", inputStackDataset, "maskDataset", binaryMaskDataset, "saveDir", saveDir);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            animator.run("next");
            
            promise = commandService.run(Commandtester_findBlobs.class, true, "dataset", inputStackDataset, "maskDataset", binaryMaskDataset, "saveDir", saveDir);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            animator.run("next");
            
            promise = commandService.run(Commandtester_findBlobs.class, true, "dataset", inputStackDataset, "maskDataset", binaryMaskDataset, "saveDir", saveDir);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            animator.run("next");
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            animator.run("next");
            
            promise = commandService.run(Commandtester_findBlobs.class, true, "dataset", inputStackDataset, "maskDataset", binaryMaskDataset, "saveDir", saveDir);
            promise.get();

            ij.WindowManager.closeAllWindows();
            
        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(WorkFlow.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
}
