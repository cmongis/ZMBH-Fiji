/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.util;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class AddSliceToStack implements Command {
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    UIService ui;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset stack;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset slice;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset extendedStack;
    
    @Override
    public void run() {

        long[] dimensions = new long[stack.numDimensions()];
        stack.dimensions(dimensions);
        //dimensions[stack.dimensionIndex(Axes.Z)] += 1;
        dimensions[2] += 1;
        
        String inputDatasetName = stack.getName();
        String[] split = inputDatasetName.split("\\.");
        //String outputDatasetName = split[0] + "_ext" + "." + split[1];
        CalibratedAxis[] axes = new CalibratedAxis[stack.numDimensions()];
        stack.axes(axes);
        AxisType[] axesType = new AxisType[stack.numDimensions()];
        for(int i = 0; i < stack.numDimensions(); i++){
            axesType[i] = axes[i].type();
        }
        extendedStack = datasetService.create(dimensions, inputDatasetName, axesType, stack.getType().getBitsPerPixel(), stack.isSigned(), !stack.isInteger());
        
        RandomAccess<RealType<?>> stackRandomAccess = stack.randomAccess();
        RandomAccess<RealType<?>> extstackRandomAccess = extendedStack.randomAccess();
        RandomAccess<RealType<?>> sliceRandomAccess = slice.randomAccess();
        
        for(int i = 0; i < stack.dimension(2); i++){
            stackRandomAccess.setPosition(new long[]{0, 0, i});
            extstackRandomAccess.setPosition(new long[]{0, 0, i});
            for(int x = 0; x < stack.dimension(stack.dimensionIndex(Axes.X)); x++){
                for(int y = 0; y < stack.dimension(stack.dimensionIndex(Axes.Y)); y++){
                    stackRandomAccess.setPosition(x, 0);
                    stackRandomAccess.setPosition(y, 1);               

                    extstackRandomAccess.setPosition(x, 0);
                    extstackRandomAccess.setPosition(y, 1);

                    extstackRandomAccess.get().setReal(stackRandomAccess.get().getRealDouble());
                }
            }
        }
        
        
        extstackRandomAccess.setPosition(new long[]{0, 0, extendedStack.dimension(2)-1});
        sliceRandomAccess.setPosition(new long[]{0, 0});

        for(int x = 0; x < dimensions[0]; x++){
            for(int y = 0; y < dimensions[1]; y++){
                extstackRandomAccess.setPosition(x, 0);
                extstackRandomAccess.setPosition(y, 1);               

                sliceRandomAccess.setPosition(x, 0);
                sliceRandomAccess.setPosition(y, 1);

                extstackRandomAccess.get().setReal(sliceRandomAccess.get().getRealDouble());
            }
        }
        
        //ui.show(extendedStack);
        
    }
    
}