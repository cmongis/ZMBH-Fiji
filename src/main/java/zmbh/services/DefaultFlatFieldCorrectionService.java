/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.services;

import java.util.Arrays;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;

/**
 * @author Guillaume
 */

@Plugin(type = SciJavaService.class)
public class DefaultFlatFieldCorrectionService extends AbstractService implements FlatFieldCorrectionService {
    
    
    @Override
    public void substractDarkSignal(ImageDisplay currentDisplay, Dataset inputDataset, int expValue) {
        
        long[] inputPosition = new long[currentDisplay.numDimensions()];
        currentDisplay.localize(inputPosition);
        System.out.println(Arrays.toString(inputPosition));

        long inputWidth = inputDataset.max(0) + 1;
        long inputHeight = inputDataset.max(1) + 1;
        
        System.out.println("inputWidth : " + inputWidth);        
        System.out.println("inputHeigth : " + inputHeight);        
        
        RandomAccess<RealType<?>> inputRandomAccess = inputDataset.randomAccess();
        inputRandomAccess.setPosition(inputPosition);
        
        for(int x = 0; x < inputWidth; x++) {
            for(int y = 0; y < inputHeight; y++) {
                inputRandomAccess.setPosition(x,0);
                inputRandomAccess.setPosition(y,1);

                float oldValue = inputRandomAccess.get().getRealFloat();
                inputRandomAccess.get().setReal(oldValue - expValue);
                
            }
        }
    }

    @Override
    public void divideBymCherryFlatField(Dataset inputDataset, Dataset flatFieldDataset) {
        long[] position = new long[inputDataset.numDimensions()];
        
        long width = inputDataset.max(0) + 1;
        long height = inputDataset.max(1) + 1;
        long time = 1;       
        
        RandomAccess<RealType<?>> inputRandomAccess = inputDataset.randomAccess();
        RandomAccess<RealType<?>> flatFieldRandomAccess = flatFieldDataset.randomAccess();
        inputRandomAccess.setPosition(position);
        
        for(int t = 0; t < time; t++){
            System.out.println("Processing all pixels for dimension t = " + t);
            flatFieldRandomAccess.setPosition(0, 0);
            flatFieldRandomAccess.setPosition(0, 1);
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    inputRandomAccess.setPosition(x,0);
                    inputRandomAccess.setPosition(y,1);
                    inputRandomAccess.setPosition(t,2);
                    
                    flatFieldRandomAccess.setPosition(x,0);
                    flatFieldRandomAccess.setPosition(y,1);
                            
                    float oldValue = inputRandomAccess.get().getRealFloat();
                    float flatFieldValue = flatFieldRandomAccess.get().getRealFloat();
                    inputRandomAccess.get().setReal(oldValue/flatFieldValue);
                }
            }
        }
        
        
    }

    @Override
    public void divideByGfpFlatField(Dataset inputDataset, Dataset flatFieldDataset) {
        
        long[] position = new long[inputDataset.numDimensions()];
        
        long width = inputDataset.max(0) + 1;
        long height = inputDataset.max(1) + 1;
        long time = 4;       
        
        RandomAccess<RealType<?>> inputRandomAccess = inputDataset.randomAccess();
        RandomAccess<RealType<?>> flatFieldRandomAccess = flatFieldDataset.randomAccess();
        inputRandomAccess.setPosition(position);
        
        for(int t = 1; t < time; t++){
            System.out.println("Processing all pixels for dimension t = " + t);
            flatFieldRandomAccess.setPosition(0, 0);
            flatFieldRandomAccess.setPosition(0, 1);
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    inputRandomAccess.setPosition(x,0);
                    inputRandomAccess.setPosition(y,1);
                    inputRandomAccess.setPosition(t,2);
                    
                    flatFieldRandomAccess.setPosition(x,0);
                    flatFieldRandomAccess.setPosition(y,1);
                            
                    float oldValue = inputRandomAccess.get().getRealFloat();
                    float flatFieldValue = flatFieldRandomAccess.get().getRealFloat();
                    inputRandomAccess.get().setReal(oldValue/flatFieldValue);
                }
            }
        }
    }
    
    @Override
    public void divideByFlatField(ImageDisplay currentDisplay, Dataset inputDataset, Dataset flatFieldDataset) {
        
        long[] inputPosition = new long[currentDisplay.numDimensions()];
        long[] flatFieldPosition = new long[flatFieldDataset.numDimensions()];
        currentDisplay.localize(inputPosition);
        System.out.println(Arrays.toString(inputPosition));
        System.out.println(Arrays.toString(flatFieldPosition));

        long inputWidth = inputDataset.max(0) + 1;
        long inputHeight = inputDataset.max(1) + 1;
        
        long flatFieldWidth = flatFieldDataset.max(0) + 1;
        long flatFieldHeight = flatFieldDataset.max(1) + 1;
        
        System.out.println("inputWidth : " + inputWidth);        
        System.out.println("inputHeigth : " + inputHeight);        
        System.out.println("flatFieldWidth : " + flatFieldWidth);       
        System.out.println("flatFieldHeight : " + flatFieldHeight);
        
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
    }
        
    
}
