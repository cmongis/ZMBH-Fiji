/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

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
import zmbh.commands.util.AddSliceToStack;
import zmbh.commands.util.ExtractSliceCommand;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Add brightField alldir", label="")
public class AddBrightfield implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    File stackDir;
    
    @Parameter(type = ItemIO.INPUT)
    File bfStackDir;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    @Override
    public void run() {
        
        File[] stackFileList = stackDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
        File[] bfStackFileList = bfStackDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
        
        if(stackFileList.length == bfStackFileList.length){            
            for(int i = 0; i < stackFileList.length; i++){
                try {
                    File stackFile = stackFileList[i];
                    
                    File bfStackFile = bfStackFileList[i];
                    
                    Dataset stack = ioService.open(stackFile.getPath());
                    Dataset bfStack = ioService.open(bfStackFile.getPath());
                    
                    Future<CommandModule> promise = cmdService.run(ExtractSliceCommand.class, true,
                            "inputDataset", bfStack,
                            "sliceNumber", 4);
                    CommandModule promiseContent = promise.get();
                    Dataset bfSlice = (Dataset) promiseContent.getOutput("outputDataset");
                    
                    promise = cmdService.run(AddSliceToStack.class, true,
                            "stack", stack,
                            "slice", bfSlice);
                    promiseContent = promise.get();
                    Dataset extendedStack = (Dataset) promiseContent.getOutput("extendedStack");
                    
                    ioService.save(extendedStack, saveDir.getPath() + "/" + stack.getName());
                    
                } catch (IOException ex) {
                    Logger.getLogger(AddBrightfield.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AddBrightfield.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(AddBrightfield.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        else{
            System.err.println("Number of files should be equal, stackDir : " + stackFileList.length + "files, brightfields : " + bfStackFileList.length + "files");
        }
        
        
    }
    
}