/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.roi;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.awt.Point;
import java.awt.Polygon;
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
public class ComputeConvexHullRoi implements Command {

    @Parameter(type = ItemIO.INPUT)
    List<Point> pointArray;
    
    @Parameter(type = ItemIO.OUTPUT)
    PolygonRoi roi;
    
    @Override
    public void run() {
        List<Point> convexHull = GrahamScan.getConvexHull(pointArray);
        
        //Arrange coordinates in arrays for Polygon constructor
        int[] xPoints = new int[convexHull.size()];
        int[] yPoints = new int[convexHull.size()];
        int nPoints = convexHull.size();
        for(int k = 0; k < convexHull.size(); k++){
            xPoints[k] = convexHull.get(k).x;
            yPoints[k] = convexHull.get(k).y;
        }

        Polygon p = new Polygon(xPoints, yPoints, nPoints);
        roi = new PolygonRoi(p, Roi.POLYGON);        
    }
    
}