/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import io.scif.services.DatasetIOService;
import java.util.List;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleService;
import org.scijava.module.process.PostprocessorPlugin;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.object.DefaultObjectService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Test", label="")
public class test implements Command {
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    DefaultObjectService DefaultObjectService;
    
    @Parameter
    ModuleService mService;
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    ObjectService objService;
    
    @Parameter
    PluginService pluginService;
    
    @Parameter
    UIService uiService;
    
    //@Parameter
    //File jsonFile;
    
    //@Parameter
    //File nd2Dir;
    
    //@Parameter
    //String pathToConverter;
    
    //@Parameter
    //String saveDir;
    
    @Override
    public void run() {
        
        List<Dataset> objects = objService.getObjects(Dataset.class);
        System.out.println(objects.size());
        
        //try {
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
        /*
        Future<CommandModule> promise = cmdService.run(LoadJSON2.class, true, "jsonFile", jsonFile);
        CommandModule promiseContent = promise.get();
        Map<String, Map<String, Integer>> imageMap = (Map<String, Map<String, Integer>>) promiseContent.getOutput("outObject");
        promise = cmdService.run(Get4ImgStack.class, true,
        "imageMap", imageMap);
        promiseContent = promise.get();
        Dataset dataset = (Dataset) promiseContent.getOutput("outStack");
        uiService.show(dataset);
         */
        /*
        File[] fileList = nd2Dir.listFiles((File pathname) -> pathname.getName().endsWith(".nd2"));
        for(File file : fileList){
        String split = file.getName().split("\\.")[0];
        String[] split2 = split.split("_");
        String well = split2[0];
        String seq = split2[1];
        for(int i = 0; i < 12; i++){
        String outName = well + "_" + "Point"+ String.format("%04d", i) + "_" + seq + ".tif";
        outName = saveDir + "/" + outName;
        ProcessBuilder builder = new ProcessBuilder(
        pathToConverter,
        "-series",
        Integer.toString(i),
        file.getPath(),
        outName);
        builder.inheritIO();
        for(String str : builder.command()){
        System.out.print(str + " ");
        }
        System.out.println("");
        try {
        java.lang.Process process = builder.start();
        process.waitFor();
        } catch (IOException ex) {
        Logger.getLogger(RunRScript.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
        Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        }
        /*
        } catch (IOException ex) {
        Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<PreprocessorPlugin> pre = pluginService.createInstancesOfType(PreprocessorPlugin.class);
        for(PreprocessorPlugin p : pre){
            System.out.println(p.getClass().getName());
        }
        System.out.println("");
        
        List<PostprocessorPlugin> post = pluginService.createInstancesOfType(PostprocessorPlugin.class);
        for(PostprocessorPlugin p : post){
            System.out.println(p.getClass().getName());
        }
        */
        
    }
    
}