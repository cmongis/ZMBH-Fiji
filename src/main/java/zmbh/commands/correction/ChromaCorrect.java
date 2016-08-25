/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.correction;

import bunwarpj.Transformation;
import ij.ImagePlus;
import io.scif.services.DatasetIOService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.util.ExtractSliceCommand;
import zmbh.commands.ImageJ1PluginAdapter;
import zmbh.commands.util.ReplaceSlice;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class ChromaCorrect implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset stack;
    
    @Parameter(type = ItemIO.INPUT)
    int sourceSlice;
    
    @Parameter(type = ItemIO.INPUT)
    int targetSlice;
    
    @Parameter(type = ItemIO.INPUT)
    Transformation warp;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outStack;
    
    
    
    @Override
    public void run() {
        
        try {
            ImagePlus sourceImp = null;
            ImagePlus targetImp = null;
            Future<CommandModule> promise;
            CommandModule promiseContent;
            
            // get the 2 slices to correct
            promise = cmdService.run(ExtractSliceCommand.class, true, "inputDataset", stack, "sliceNumber", sourceSlice);
            promiseContent = promise.get();            
            sourceImp = ImageJ1PluginAdapter.unwrapDataset((Dataset) promiseContent.getOutput("outputDataset"));
            sourceImp.resetDisplayRange();
            
            promise = cmdService.run(ExtractSliceCommand.class, true, "inputDataset", stack, "sliceNumber", targetSlice);
            promiseContent = promise.get();
            targetImp = ImageJ1PluginAdapter.unwrapDataset((Dataset) promiseContent.getOutput("outputDataset"));
            targetImp.resetDisplayRange();
            
            // Perform correction with bUnwarpJ
            promise = cmdService.run(myBunWarpJ.class, true, "sourceImp", sourceImp, "targetImp", targetImp, "warp", warp);
            promiseContent = promise.get();
            ImagePlus correctedSource = (ImagePlus) promiseContent.getOutput("correctedSource");
            Img img = ImageJFunctions.wrap(correctedSource);
            Dataset correctedSourceDataset = datasetService.create(img);
            
            // Replace the corrected slice in the stack
            promise = cmdService.run(ReplaceSlice.class, false, "stack", stack, "sliceDataset", correctedSourceDataset, "sliceNumber", sourceSlice);
            promiseContent = promise.get();
            outStack = (Dataset) promiseContent.getOutput("outStack");
            
            
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ChromaCorrect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}