/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.measure;

import zmbh.commands.correction.SliceCorrectCommand_DarkField_FlatField_AllSlices;
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
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import zmbh.commands.util.ConvertDatasetEncodingCommand;
import zmbh.commands.segmentation.LoadSegmentationMaskWithIdCommand;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class MakeMeasurementsCommand_AllSlices implements Command {
    
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
            
            promise = commandService.run(SliceCorrectCommand_DarkField_FlatField_AllSlices.class, true, "inputDataset", inputStackDataset, "darkfieldValue", darkfieldValue, "mCherryFlatFieldFile", mCherryFlatFieldFile, "gfpFlatFieldFile", gfpFlatFieldFile, "bfpFlatFieldFile", bfpFlatFieldFile);
            promiseContent = promise.get();
            inputStackDataset = (Dataset) promiseContent.getOutput("inputDataset");
            
            /*
            promise = commandService.run(LoadSegmentationMaskCommand.class, true, "maskFile", maskFile);
            promiseContent = promise.get();
            inputMaskDataset = (Dataset) promiseContent.getOutput("maskDataset");
            */
            promise = commandService.run(LoadSegmentationMaskWithIdCommand.class, true, "maskFile", maskFile);
            promiseContent = promise.get();
            inputMaskDataset = (Dataset) promiseContent.getOutput("maskDataset");
            
            /*
            promise = commandService.run(GetBinaryMaskFromSegmentationMask.class, true, "inDataset", inputMaskDataset);
            promiseContent = promise.get();
            Dataset binaryMaskDataset = (Dataset) promiseContent.getOutput("outDataset");
            */
            
            /*
            for(int i = 0; i < inputStackDataset.dimension(2); i++){
                promise = commandService.run(MakeMeasurementsCommand.class, true, "dataset", inputStackDataset, "maskDataset", binaryMaskDataset, "sliceNumber", i,"saveDir", saveDir);
                promise.get();
            }
            */
            for(int i = 0; i < inputStackDataset.dimension(2); i++){
                promise = commandService.run(MakeMeasurementsCommand.class, true, "dataset", inputStackDataset, "maskDataset", inputMaskDataset, "sliceNumber", i,"saveDir", saveDir);
                promise.get();
            }
            
            ij.WindowManager.closeAllWindows();
            
        } catch (IOException | InterruptedException | ExecutionException ex) {
            Logger.getLogger(MakeMeasurementsCommand_AllSlices.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
}
