/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
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

@Plugin(type = Command.class, menuPath = "Dev-commands>Config>CMD get stack struct", label="")
public class GetStackStructure implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset stack;
    
    //@Parameter(type = ItemIO.INPUT)
    //String savePath;
    
    @Parameter(type = ItemIO.OUTPUT)
    StructureInfo structureInfo;
        
    @Override
    public void run() {
        
        int numDimensions = stack.numDimensions();
        CalibratedAxis[] axes = new CalibratedAxis[numDimensions];
        stack.axes(axes);

        Map<Integer, AxisInfo> axisMap = new HashMap<>();
        for(CalibratedAxis axe : axes){
            AxisType axisType = axe.type();
            int axisIndex = stack.dimensionIndex(axisType);
            AxisInfo axisInfo = new AxisInfo(axisType.getLabel(), stack.dimension(axisIndex));
            axisMap.put(axisIndex, axisInfo);
        }
        
        structureInfo = new StructureInfo(numDimensions, axisMap);
        
        structureInfo.getAxisMap().entrySet().stream().forEach((entry) -> {
            System.out.println(entry.getKey() + " : " + entry.getValue().getAxisType() + " : " + entry.getValue().getAxeDim());
        });
               
        
        /*
        File saveFile = new File(savePath);
        Future<CommandModule> promise = cmdService.run(SaveJSON.class, true,
                "obj", structureInfo,
                "saveFile", saveFile);        
        try {
            promise.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(GetStackStructure.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        promise = cmdService.run(LoadJSON.class, true,
                "jsonFile", saveFile);
        try {
            CommandModule promiseContent = promise.get();
            StructureInfo output = (StructureInfo) promiseContent.getOutput("outObject");
            
            System.out.println("");
            output.getAxisMap().entrySet().stream().forEach((entry) -> {
                System.out.println(entry.getKey() + " : " + entry.getValue().getAxisType() + " : " + entry.getValue().getAxeDim());
            });
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(GetStackStructure.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        
    }
    
}