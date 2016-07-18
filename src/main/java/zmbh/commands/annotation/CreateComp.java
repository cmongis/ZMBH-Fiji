/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import ij.gui.Roi;
import static java.lang.Float.max;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;
import java.util.Map;
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
import org.scijava.ui.UIService;
import zmbh.commands.segmentation.CellRecord;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class CreateComp implements Command {
    
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    UIService uiService;
    
    @Parameter(type = ItemIO.INPUT)
    Map<CellRecord, Roi> roimap;
    
    @Parameter(type = ItemIO.INPUT)
    Map<CellRecord, Dataset> datamap;
    
    @Parameter
    String title;
    
    @Parameter(type = ItemIO.OUTPUT)
    Dataset compDataset;
    
    @Override
    public void run() {
        
        // Get max side length of cell bounding square
        int maxSquareSide = 0;
        for(CellRecord record : roimap.keySet()){
            float value = max(record.getBoundingSquareWidth(), record.getBoundingSquareHeight());
            if(value > maxSquareSide){
                maxSquareSide = (int) value;
            }
        }
        // Compute composite image size
        double composeImgSize = sqrt(maxSquareSide* maxSquareSide *  roimap.keySet().size());
        double nbCellPerRow = composeImgSize / maxSquareSide;
        nbCellPerRow = ceil(nbCellPerRow);
        composeImgSize = (nbCellPerRow) * maxSquareSide;
        compDataset = datasetService.create(new long[]{(long)composeImgSize,(long)composeImgSize}, title, new AxisType[]{Axes.X, Axes.Y}, 32, true, true);

        RandomAccess<RealType<?>> outputRandomAccess = compDataset.randomAccess();
        
        // Copy pixel data from cell rois to composite image
        int counter = 0;
        for(Map.Entry<CellRecord, Roi> entry : roimap.entrySet()){
            Dataset data = datamap.get(entry.getKey());
            RandomAccess<RealType<?>> inputRandomAccess = data.randomAccess();

            long x_in = (long) (entry.getKey().getX()-(maxSquareSide/2));
            long y_in = (long)(entry.getKey().getY()-(maxSquareSide/2));

            long x_out = (long) (counter % nbCellPerRow)*maxSquareSide;                
            long y_out = (long) floor(counter / nbCellPerRow)*maxSquareSide;

            inputRandomAccess.setPosition(new long[]{x_in, y_in, 0});
            outputRandomAccess.setPosition(new long[]{x_out, y_out});

            for(int x = 0; x < maxSquareSide; x++){
                for(int y = 0; y < maxSquareSide; y++){
                    inputRandomAccess.setPosition(x_in, 0);
                    inputRandomAccess.setPosition(y_in, 1);

                    outputRandomAccess.setPosition(x_out, 0);
                    outputRandomAccess.setPosition(y_out, 1);

                    if(x_in >= 0 && x_in < data.dimension(0) && y_in >= 0 && y_in < data.dimension(1)){
                        if(entry.getValue().contains((int)x_in, (int)y_in)){
                            outputRandomAccess.get().setReal(inputRandomAccess.get().getRealDouble());
                        }
                    }
                    y_in++;
                    y_out++;
                }
                x_in++;
                x_out++;
                y_in = (long)(entry.getKey().getY()-(maxSquareSide/2));
                y_out = (long) floor(counter / nbCellPerRow)*maxSquareSide;
            }
            counter++;
        }       
    }    
}