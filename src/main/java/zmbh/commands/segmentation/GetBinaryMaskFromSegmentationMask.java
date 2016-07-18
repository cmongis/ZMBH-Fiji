/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.segmentation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.util.ConvertDatasetEncodingCommand;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Segmentation>CMD Get binary mask from segmentation mask", label="")
public class GetBinaryMaskFromSegmentationMask implements Command {
    
    @Parameter
    CommandService commandService;

    @Parameter(type = ItemIO.INPUT)
    Dataset inDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outDataset;
    
    
    @Override
    public void run() {
        
        try {
            Future<CommandModule> promise;
            CommandModule promiseContent;
            promise = commandService.run(ConvertDatasetEncodingCommand.class, true, "inDataset", inDataset, "targetType", "8-bit unsigned integer");
            promiseContent = promise.get();
            outDataset = (Dataset) promiseContent.getOutput("outDataset");
            
            long width = outDataset.max(0) + 1;
            long height = outDataset.max(1) + 1;
            RandomAccess<RealType<?>> randomAccess = outDataset.randomAccess();
            
            for(long x = 0; x < width; x++){
                for(long y = 0; y < height; y++){
                    randomAccess.setPosition(x, 0);
                    randomAccess.setPosition(y, 1);
                    
                    if(randomAccess.get().getRealFloat() > 0){
                        randomAccess.get().setReal(0);
                    }
                    else{
                        randomAccess.get().setReal(255);
                    }                   
                }
            }
            
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(GetBinaryMaskFromSegmentationMask.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
}