/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import io.scif.services.DatasetIOService;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author Guillaume
 */


@Plugin(type = Command.class, menuPath = "Dev-commands>CMD devide by", label="")
public class DivideBy implements Command {
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    DatasetIOService datasetIoService;
    
    @Parameter
    ImageDisplay currentDisplay;
    
    @Parameter
    File file;
    
    @Override
    public void run() {  
        Dataset inputDataset = imageDisplayService.getActiveDataset(currentDisplay);
        Dataset flatFieldDataset = null;
        try {
            flatFieldDataset = datasetIoService.open(file.getPath());
            System.out.println(flatFieldDataset);
        } catch (IOException ex) {
            Logger.getLogger(DivideBy.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        long[] inputPosition = new long[currentDisplay.numDimensions()];
        long[] flatFieldPosition = new long[flatFieldDataset.numDimensions()];
        currentDisplay.localize(inputPosition);
        
        System.out.println(Arrays.toString(inputPosition));
        System.out.println(Arrays.toString(flatFieldPosition));

        long inputWidth = inputDataset.max(0) + 1;
        long inputHeight = inputDataset.max(1) + 1;
        
        long flatFieldWidth = flatFieldDataset.max(0) + 1;
        long flatFieldHeight = flatFieldDataset.max(1) + 1;       
        
        if(inputWidth == flatFieldWidth && inputHeight == flatFieldHeight){
            RandomAccess<RealType<?>> inputRandomAccess = inputDataset.randomAccess();
            RandomAccess<RealType<?>> flatFieldRandomAccess = flatFieldDataset.randomAccess();
            inputRandomAccess.setPosition(inputPosition);
            flatFieldRandomAccess.setPosition(flatFieldPosition);

            for(int x = 0; x < inputWidth; x++) {
                for(int y = 0; y < inputHeight; y++) {
                    inputRandomAccess.setPosition(x,0);
                    inputRandomAccess.setPosition(y,1);

                    flatFieldRandomAccess.setPosition(x,0);
                    flatFieldRandomAccess.setPosition(y,1);

                    float oldValue = inputRandomAccess.get().getRealFloat();
                    float flatFieldValue = flatFieldRandomAccess.get().getRealFloat();
                    if(flatFieldValue != 0){
                        inputRandomAccess.get().setReal(oldValue/flatFieldValue);
                    }
                }
            }
            currentDisplay.update();
        }
        else{
            System.err.println("Incompatible dimensions");
        } 
    }
}
