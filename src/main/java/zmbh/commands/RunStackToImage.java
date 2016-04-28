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
import java.util.ArrayList;
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

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD run StackToImages", label="")
public class RunStackToImage implements Command {

    @Parameter
    DatasetIOService datasetioService;
    
    @Parameter
    CommandService commandService;
       
    @Parameter(type = ItemIO.INPUT)
    File stackDir;
    
    @Parameter(type = ItemIO.INPUT)
    File saveDir;
    
    
    @Parameter(type = ItemIO.INPUT, required = false)
    String trash;
    
    
    @Override
    public void run() {
        
        if(stackDir.isDirectory()){
            File[] fileList = stackDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
            Future<CommandModule> promise;
            CommandModule promiseContent;
            int counter = 0;
            for(File file : fileList){
                try {
                    Dataset inputDataset = datasetioService.open(file.getPath());
                    promise = commandService.run(StackToImages.class, true, "inputDataset", inputDataset);
                    promiseContent = promise.get();
                    ArrayList<Dataset> outputDatasetArray = (ArrayList<Dataset>) promiseContent.getOutput("outputDatasetArray");
                    //datasetioService.save(outputDataset, saveDir.getPath()+ "/" + outputDataset.getName());
                    for(int i = 0; i < outputDatasetArray.size(); i++){
                        FileSaver fileSaver = new FileSaver(ImageJ1PluginAdapter.unwrapDataset(outputDatasetArray.get(i)));
                        fileSaver.saveAsTiff(saveDir.getPath()+ "/" + outputDatasetArray.get(i).getName() + ".tif");
                    }
                    
                    
                    counter++;
                } catch (IOException ex) {
                    Logger.getLogger(CommandTester_extract_brightfield.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CommandTester_extract_brightField_allDir.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(CommandTester_extract_brightField_allDir.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                ij.WindowManager.closeAllWindows();
            }
        }
        else{
            System.err.println(stackDir.getPath() + " is not a directory");
        }
        
    } 
    
}