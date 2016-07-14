/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import bunwarpj.bUnwarpJ_;
import zmbh.config.LoadJSON2;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import org.mapdb.Atomic;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleService;
import org.scijava.object.DefaultObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;
import zmbh.config.Get4ImgStack;

/**
 *
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Test", label="")
public class test implements Command {
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    DefaultObjectService DefaultObjectService;
    
    @Parameter
    ModuleService mService;
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    PluginService pluginService;
    
    @Parameter
    UIService uiService;
    
    @Parameter
    File jsonFile;
    
    
    @Override
    public void run() {
        try {
            /*
            try {
            Future<CommandModule> promise = cmdService.run(testUselesscreatedataset.class, true);
            CommandModule promiseContent = promise.get();
            Dataset outDataset = (Dataset) promiseContent.getOutput("outDataset");
            if(outDataset != null){
            System.out.println(outDataset.getName());
            }
            } catch (InterruptedException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
            //bUnwarpJ_ bUnwarpj = new bUnwarpJ_();
            //bUnwarpj.run("");
            
            
            /*
            Future<CommandModule> promise = cmdService.run(LoadJSON2.class, true);
            CommandModule promiseContent = promise.get();
            Map<String, Map<String, Integer>> map = (Map<String, Map<String, Integer>>) promiseContent.getOutput("outObject");
            
            for(Map.Entry<String, Map<String, Integer>> entry : map.entrySet()){
                System.out.println(entry.getKey());
                for(Map.Entry<String, Integer> e:entry.getValue().entrySet()){
                    System.out.println(e.getKey());
                    System.out.println(e.getValue());
                }
                System.out.println("");
            }
            */
            
            
            Future<CommandModule> promise = cmdService.run(LoadJSON2.class, true, "jsonFile", jsonFile);
            CommandModule promiseContent = promise.get();
            Map<String, Map<String, Integer>> imageMap = (Map<String, Map<String, Integer>>) promiseContent.getOutput("outObject");
            
                      
            promise = cmdService.run(Get4ImgStack.class, true,
                    "imageMap", imageMap);
            promiseContent = promise.get();
            Dataset dataset = (Dataset) promiseContent.getOutput("outStack");
            uiService.show(dataset);
            
            
            
        } catch (InterruptedException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}