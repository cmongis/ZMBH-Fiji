/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.roi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class ConvertPixelIndexToPoint implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    int imgHeigth;
    
    @Parameter(type = ItemIO.INPUT)
    double[] perimeterPixelListIndex;
    
    @Parameter(type = ItemIO.OUTPUT)
    List<Point> pointArray;
    
    
    @Override
    public void run() {
        
        pointArray = new ArrayList<>();
        for(int k = 0; k < perimeterPixelListIndex.length; k++){
            pointArray.add(new Point((int) (perimeterPixelListIndex[k] / imgHeigth), (int) (perimeterPixelListIndex[k] % imgHeigth)));
        }
        
    }
    
}