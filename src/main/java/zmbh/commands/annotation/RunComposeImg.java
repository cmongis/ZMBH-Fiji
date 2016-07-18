/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Annotation>RUN ComposeImg", label="")
public class RunComposeImg implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    UIService uiService;
      
    @Parameter(type = ItemIO.INPUT)
    String recordClassDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String cellDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String stackDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    int sliceNumber;
    
    @Parameter(type = ItemIO.INPUT)
    boolean isWell;
    
    @Parameter(type = ItemIO.INPUT, required = false)
    String saveDir;
    
    @Override
    public void run() {
        
        try {           
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            // Get file mapping
            promise = cmdService.run(GatherFiles.class, true, "stackDirPath", stackDirPath, "cellDirPath", cellDirPath, "recordClassDirPath", recordClassDirPath);
            promiseContent = promise.get();
            Map<Map<String, File>, String> fileSerie = (Map<Map<String, File>, String>) promiseContent.getOutput("fileSerie");
            
            // Get unique file names
            // isWell determine if we consider each image or a well agregation
            Set<String> labelSet = new HashSet<>(fileSerie.values());
            if(isWell){
                Set<String> wellLabelSet = new HashSet<>();
                for(String label : labelSet){
                    String[] split = label.split("_");
                    wellLabelSet.add(split[0]);
                }
                labelSet = wellLabelSet;
            }
            
            // Get sub-map for composite image generation
            for(String label : labelSet){
                List<Map<String, File>> list = new ArrayList<>();
                for(Map.Entry<Map<String, File>, String> entry : fileSerie.entrySet()){
                    if(entry.getValue().contains(label)){
                        list.add(entry.getKey());
                    }
                }
                promise = cmdService.run(ComposeImg.class, true, "mapFileList", list, "title", label, "sliceNumber", sliceNumber);
                promiseContent = promise.get();
                if(saveDir != null){
                    Dataset blue = (Dataset) promiseContent.getOutput("blueCompDataset");
                    Dataset noblue = (Dataset) promiseContent.getOutput("noblueCompDataset");
                    ioService.save(blue, saveDir + "/" + blue.getName() + ".tif");
                    ioService.save(noblue, saveDir + "/" + noblue.getName() + ".tif");
                }
            }
            
  
        } catch (InterruptedException ex) {
            Logger.getLogger(ComposeImg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ComposeImg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RunComposeImg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}