/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.correction;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
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
import zmbh.commands.util.ConvertDatasetEncodingCommand;

/**
 *
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Correction>CMD Get FF Stacks", label="")
public class ApplyFF implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService datasetioService;
    
    @Parameter(type = ItemIO.INPUT)
    File inputStack;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
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
    
    @Override
    public void run() {
        Future<CommandModule> promise;
        CommandModule promiseContent;
        
        try {
                    // Convert image encoding
                    Dataset inputStackDataset = datasetioService.open(inputStack.getPath());
                    promise = cmdService.run(ConvertDatasetEncodingCommand.class, true, "inDataset", inputStackDataset, "targetType", targetType);
                    promiseContent = promise.get();
                    inputStackDataset = (Dataset) promiseContent.getOutput("outDataset");
                    
                    // Perform flatfield and darkfield correction
                    promise = cmdService.run(SliceCorrectCommand_DarkField_FlatField_AllSlices.class, true, "inputDataset", inputStackDataset, "darkfieldValue", darkfieldValue, "mCherryFlatFieldFile", mCherryFlatFieldFile, "gfpFlatFieldFile", gfpFlatFieldFile, "bfpFlatFieldFile", bfpFlatFieldFile);
                    promiseContent = promise.get();
                    inputStackDataset = (Dataset) promiseContent.getOutput("inputDataset");
                    
                    datasetioService.save(inputStackDataset, saveDir.getPath() + "/" + inputStackDataset.getName());
                } catch (IOException ex) {
                    Logger.getLogger(GetFFImg.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GetFFImg.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(GetFFImg.class.getName()).log(Level.SEVERE, null, ex);
                }
        
    }
    
}