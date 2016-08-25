/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import zmbh.commands.unused.MakeMeasurementsCommand_AllSlices_AllDir;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Annotation>RUN Annotation (all dir)", label="")
public class RunAnnotationCommand_allDir implements Command {
    
    @Parameter
    CommandService commandService;
    
    @Parameter(type = ItemIO.INPUT)
    File inputStackDir;
    
    @Parameter(type = ItemIO.INPUT)
    File annotedRecordDir;
    
    @Parameter(type = ItemIO.INPUT)
    File cellDir;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    
    @Override
    public void run() {
        
        if(inputStackDir.isDirectory() && cellDir.isDirectory() && annotedRecordDir.isDirectory() && saveDir.isDirectory()){
            File[] inputStackFileList = inputStackDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
            File[] cellFileList = cellDir.listFiles((File pathname) -> pathname.getName().endsWith(".mat"));
            File[] annotedRecordFileList = annotedRecordDir.listFiles((File pathname) -> pathname.getName().endsWith(".csv"));
            Future<CommandModule> promise;           
            
            if(inputStackFileList.length == cellFileList.length && inputStackFileList.length == annotedRecordFileList.length){
                for(int i = 0; i < inputStackFileList.length; i++){                    
                    try {
                        promise = commandService.run(RunAnnotationCommand.class, true,"inDatasetFile", inputStackFileList[i], "cellFile", cellFileList[i], "recordClassFile", annotedRecordFileList[i], "saveDir", saveDir);
                        promise.get();
                        //ij.WindowManager.closeAllWindows();
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(MakeMeasurementsCommand_AllSlices_AllDir.class.getName()).log(Level.SEVERE, null, ex);
                    }                   
                }
            }
            else{
                System.err.println("Number of stacks and masks should be equal");
            }
        }
        else{
            System.err.println("Selection is not a directory");
        }
        
    }
    
}