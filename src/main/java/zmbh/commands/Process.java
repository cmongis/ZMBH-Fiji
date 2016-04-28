/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

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
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD START Process", label="")
public class Process implements Command {

    @Parameter
    CommandService commandService;
    
    @Parameter(type = ItemIO.INPUT)
    File inputStackDir;
    
    @Parameter(type = ItemIO.INPUT)
    File masksDir;
    
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
        
        if(inputStackDir.isDirectory() && masksDir.isDirectory() && saveDir.isDirectory()){
            File[] inputStackFileList = inputStackDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
            File[] masksFileList = masksDir.listFiles((File pathname) -> pathname.getName().endsWith(".mat"));
            Future<CommandModule> promise;           
            
            if(inputStackFileList.length == masksFileList.length){
                for(int i = 0; i < inputStackFileList.length; i++){
                    promise = commandService.run(WorkFlow.class, true,
                            "inputStackFile", inputStackFileList[i],
                            "maskFile", masksFileList[i],
                            "targetType", targetType,
                            "darkfieldValue", darkfieldValue,
                            "mCherryFlatFieldFile", mCherryFlatFieldFile,
                            "gfpFlatFieldFile", gfpFlatFieldFile,
                            "bfpFlatFieldFile", bfpFlatFieldFile,
                            "saveDir", saveDir);
                    
                    try {
                        promise.get();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
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