/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import ij.gui.Roi;
import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import zmbh.commands.segmentation.CellRecord;
import zmbh.commands.util.ExtractSliceCommand;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class ComposeImg implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    List<Map<String, File>> mapFileList;
    
    @Parameter
    String title;
    
    @Parameter(type = ItemIO.INPUT)
    int sliceNumber;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset blueCompDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset noblueCompDataset;
    
    @Override
    public void run() {
        try {
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            Map<CellRecord, Roi> roimap = new HashMap<>();
            Map<CellRecord, Dataset> datamap = new HashMap<>();
            
            // Get mapped data
            for(Map<String, File> map : mapFileList){
                Dataset stackDataset = ioService.open(map.get("stackFile").getPath());
                
                promise = cmdService.run(ExtractSliceCommand.class, true, "inputDataset", stackDataset, "sliceNumber", sliceNumber);
                promiseContent = promise.get();
                Dataset slicedataset = (Dataset) promiseContent.getOutput("outputDataset");
                               
                promise = cmdService.run(MapImageData.class, true, "recordClassFile", map.get("recordFile"), "cellFile", map.get("cellFile"), "data", slicedataset);
                promiseContent = promise.get();
                Map<CellRecord, Roi> tmproimap = (Map<CellRecord, Roi>) promiseContent.getOutput("roimap");
                Map<CellRecord, Dataset> tmpdatamap = (Map<CellRecord, Dataset>) promiseContent.getOutput("datamap");
                
                roimap.putAll(tmproimap);
                datamap.putAll(tmpdatamap);
            }
            
            // split data according to population class
            Map<CellRecord, Roi> blueroiMap = new HashMap<>();
            Map<CellRecord, Roi> noblueroiMap = new HashMap<>();
            for(Map.Entry<CellRecord, Roi> entry : roimap.entrySet()){
                if(entry.getKey().isBlueClass()){
                    blueroiMap.put(entry.getKey(), entry.getValue());
                }
                else{
                    noblueroiMap.put(entry.getKey(), entry.getValue());
                }
            }
            
            // Create composite image for each population class
            promise = cmdService.run(CreateComp.class, true, "roimap", blueroiMap, "datamap", datamap, "title", title + "_rbd2");
            promiseContent = promise.get();
            blueCompDataset = (Dataset) promiseContent.getOutput("compDataset");
            
            promise = cmdService.run(CreateComp.class, true, "roimap", noblueroiMap, "datamap", datamap, "title", title + "_wt");
            promiseContent = promise.get();
            noblueCompDataset = (Dataset) promiseContent.getOutput("compDataset");
            
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ComposeImg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(ComposeImg.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ComposeImg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}