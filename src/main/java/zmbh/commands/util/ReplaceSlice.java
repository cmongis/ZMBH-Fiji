/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.util;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class ReplaceSlice implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    Dataset stack;
    
    @Parameter(type = ItemIO.INPUT)
    int sliceNumber;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset sliceDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset outStack;
    
    
    
    @Override
    public void run() {
        
        long[] dimensions = new long[2];
        dimensions[0] = stack.dimension(0);
        dimensions[1] = stack.dimension(1);
        
        RandomAccess<RealType<?>> inputRandomAccess = sliceDataset.randomAccess();
        RandomAccess<RealType<?>> outputRandomAccess = stack.randomAccess();
        
        inputRandomAccess.setPosition(new long[]{0, 0});
        outputRandomAccess.setPosition(new long[]{0, 0, sliceNumber});
        

        for(int x = 0; x < dimensions[0]; x++){
            for(int y = 0; y < dimensions[1]; y++){
                inputRandomAccess.setPosition(x, 0);
                inputRandomAccess.setPosition(y, 1);

                outputRandomAccess.setPosition(x, 0);
                outputRandomAccess.setPosition(y, 1);

                outputRandomAccess.get().setReal(inputRandomAccess.get().getRealDouble());
            }
        }
        
        outStack = stack;
        
    }
    
}