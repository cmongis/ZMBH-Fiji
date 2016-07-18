/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.roi;

import ij.gui.Roi;
import ij.gui.ShapeRoi;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class GetBackGroundRoi implements Command {
    
    
    @Parameter(type = ItemIO.INPUT)
    ShapeRoi fullImgRoi;
    
    @Parameter(type = ItemIO.INPUT)
    List<Roi> roiList;
    
    @Parameter(type = ItemIO.OUTPUT)
    ShapeRoi backgroundRoi;
    
    @Override
    public void run() {
        ArrayList<ShapeRoi> shapeRoiList = new ArrayList<>();
        ArrayList<ShapeRoi> xorShapeRoiList = new ArrayList<>();
        
        roiList.forEach((roi) -> shapeRoiList.add(new ShapeRoi(roi)));

        shapeRoiList.forEach((roi) -> xorShapeRoiList.add(roi.xor(fullImgRoi)));
        
        if(!xorShapeRoiList.isEmpty()){
            backgroundRoi = xorShapeRoiList.remove(0);
            xorShapeRoiList.forEach((roi) -> backgroundRoi = backgroundRoi.xor(roi));
        }
        
    }
    
}