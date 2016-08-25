/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

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


@Plugin(type = Command.class)
public class GetBinaryMaskFromSegmentationMask_OneBlob implements Command {
    
    @Parameter
    CommandService commandService;

    @Parameter(type = ItemIO.INPUT)
    Dataset inDataset;
    
    @Parameter(type = ItemIO.INPUT)
    float blobValue;
    
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
            RandomAccess<RealType<?>> inRa = inDataset.randomAccess();
            RandomAccess<RealType<?>> outRa = outDataset.randomAccess();
            
            for(long x = 0; x < width; x++){
                for(long y = 0; y < height; y++){
                    inRa.setPosition(x, 0);
                    inRa.setPosition(y, 1);
                    outRa.setPosition(x, 0);
                    outRa.setPosition(y, 1);
                    
                    if(inRa.get().getRealFloat() == blobValue){
                        outRa.get().setReal(0);
                    }
                    else{
                        outRa.get().setReal(255);
                    }                   
                }
            }
            
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(GetBinaryMaskFromSegmentationMask.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
}