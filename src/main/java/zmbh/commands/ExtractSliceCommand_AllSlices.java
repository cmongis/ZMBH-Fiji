/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
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

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Extract all slices", label="")
public class ExtractSliceCommand_AllSlices implements Command {

    @Parameter
    DatasetService datasetService;
    
    @Parameter
    CommandService cmdService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset inputDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    ArrayList<Dataset> outputDatasetArray = new ArrayList<>();
    
    @Override
    public void run() {
        
        long nbSlices = inputDataset.dimension(2);
        Future<CommandModule> promise;
        CommandModule promiseContent;
        
        for(int i = 0; i < nbSlices; i++){
            
            try {
                promise = cmdService.run(ExtractSliceCommand.class, true, "inputDataset", inputDataset, "sliceNumber", i);
                promiseContent = promise.get();
                Dataset outputDataset = (Dataset) promiseContent.getOutput("outputDataset");
                outputDatasetArray.add(outputDataset);
            } catch (InterruptedException ex) {
                Logger.getLogger(ExtractSliceCommand_AllSlices.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(ExtractSliceCommand_AllSlices.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
    }
    
}