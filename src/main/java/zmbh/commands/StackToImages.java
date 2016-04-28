/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import java.util.ArrayList;
import net.imagej.Dataset;
import net.imagej.DatasetService;
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

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD StackToImages", label="")
public class StackToImages implements Command {

    @Parameter
    DatasetService datasetService;
    
    @Parameter(type = ItemIO.INPUT)
    Dataset inputDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    ArrayList<Dataset> outputDatasetArray = new ArrayList<>();
    
    @Override
    public void run() {
        
        for(int i = 0; i < 7; i++){
            
            long[] dimensions = new long[2];
            dimensions[0] = inputDataset.dimension(0);
            dimensions[1] = inputDataset.dimension(1);
            String outputDatasetName = inputDataset.getName() + "_" + i;
            AxisType[] axisArray = new AxisType[]{Axes.X, Axes.Y};
            Dataset outputDataset = datasetService.create(dimensions, outputDatasetName, axisArray, inputDataset.getType().getBitsPerPixel(), inputDataset.isSigned(), !inputDataset.isInteger());
            RandomAccess<RealType<?>> inputRandomAccess = inputDataset.randomAccess();
            RandomAccess<RealType<?>> outputRandomAccess = outputDataset.randomAccess();

            inputRandomAccess.setPosition(new long[]{0, 0, i});
            outputRandomAccess.setPosition(new long[]{0, 0});

            for(int x = 0; x < dimensions[0]; x++){
                for(int y = 0; y < dimensions[1]; y++){
                    inputRandomAccess.setPosition(x, 0);
                    inputRandomAccess.setPosition(y, 1);

                    outputRandomAccess.setPosition(x, 0);
                    outputRandomAccess.setPosition(y, 1);

                    outputRandomAccess.get().setReal(inputRandomAccess.get().getRealFloat());
                }
            }
            
            outputDatasetArray.add(outputDataset);
            
        }
        
    }
    
}