/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import ij.io.FileSaver;
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

/**
 *
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Extract slice (all dir)", label="")
public class ExtractSliceCommand_AllDir implements Command {
    
    @Parameter
    DatasetIOService datasetioService;
    
    @Parameter
    CommandService commandService;
    
    @Parameter(type = ItemIO.INPUT)
    int sliceNumber;
       
    @Parameter(type = ItemIO.INPUT)
    File stackDir;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    
    @Override
    public void run() {
        
        if(stackDir.isDirectory()){
            File[] fileList = stackDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
            Future<CommandModule> promise;
            CommandModule promiseContent;
            for(File file : fileList){
                try {
                    Dataset inputDataset = datasetioService.open(file.getPath());
                    promise = commandService.run(ExtractSliceCommand.class, true, "inputDataset", inputDataset, "sliceNumber", sliceNumber);
                    promiseContent = promise.get();
                    Dataset outputDataset = (Dataset) promiseContent.getOutput("outputDataset");
                    if(saveDir.isDirectory()){
                        FileSaver fileSaver = new FileSaver(ImageJ1PluginAdapter.unwrapDataset(outputDataset));
                        fileSaver.saveAsTiff(saveDir.getPath()+ "/" + outputDataset.getName());
                    }
                    else{
                        System.err.println(saveDir.getPath() + " is not a directory");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ExtractSliceCommand.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(ExtractSliceCommand_AllDir.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                ij.WindowManager.closeAllWindows();
            }
        }
        else{
            System.err.println(stackDir.getPath() + " is not a directory");
        }
        
    } 
}