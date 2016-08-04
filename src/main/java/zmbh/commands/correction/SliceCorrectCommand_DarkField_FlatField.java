/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.correction;

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
public class SliceCorrectCommand_DarkField_FlatField implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter(type = ItemIO.BOTH)
    Dataset inputDataset;
    
    @Parameter(type = ItemIO.INPUT)
    int darkfieldValue;
    
    @Parameter(type = ItemIO.INPUT)
    int sliceNumber;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset flatFieldDataset;
    
    @Override
    public void run() {  
        
        String inputDatasetEncoding = inputDataset.getTypeLabelLong();        
        if(!inputDatasetEncoding.equals("32-bit signed float")){
            try {
                System.out.println("Input dataset encoding : " + inputDatasetEncoding);
                Future<CommandModule> promise = cmdService.run(ConvertDatasetEncodingCommand.class, true, "inDataset", inputDataset, "targetType", "32-bit signed float");
                CommandModule promiseContent = promise.get();
                inputDataset = (Dataset) promiseContent.getOutput("outDataset");
            } catch (InterruptedException ex) {
                Logger.getLogger(SliceCorrectCommand_DarkField_FlatField.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(SliceCorrectCommand_DarkField_FlatField.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        long inputWidth = inputDataset.dimension(0);
        long inputHeight = inputDataset.dimension(1);
        
        long flatFieldWidth = flatFieldDataset.dimension(0);
        long flatFieldHeight = flatFieldDataset.dimension(1);       
        
        if(inputWidth == flatFieldWidth && inputHeight == flatFieldHeight){
            RandomAccess<RealType<?>> inputRandomAccess = inputDataset.randomAccess();
            RandomAccess<RealType<?>> flatFieldRandomAccess = flatFieldDataset.randomAccess();
            inputRandomAccess.setPosition(new long[]{0, 0, sliceNumber});
            flatFieldRandomAccess.setPosition(new long[]{0, 0});

            for(int x = 0; x < inputWidth; x++) {
                for(int y = 0; y < inputHeight; y++) {
                    inputRandomAccess.setPosition(x,0);
                    inputRandomAccess.setPosition(y,1);

                    flatFieldRandomAccess.setPosition(x,0);
                    flatFieldRandomAccess.setPosition(y,1);

                    float oldValue = inputRandomAccess.get().getRealFloat();
                    float darkfieldCorrectedValue = oldValue - darkfieldValue;
                    if(darkfieldCorrectedValue < 0){
                        inputRandomAccess.get().setReal(0);
                        darkfieldCorrectedValue = 0;
                    }else{
                        inputRandomAccess.get().setReal(darkfieldCorrectedValue);
                    }
                    
                    float flatFieldValue = flatFieldRandomAccess.get().getRealFloat();
                    if(flatFieldValue != 0){
                        inputRandomAccess.get().setReal(darkfieldCorrectedValue/flatFieldValue);
                    }
                }
            }
        }
        else{
            System.err.println("Incompatible dimensions");
        } 
    }
}
