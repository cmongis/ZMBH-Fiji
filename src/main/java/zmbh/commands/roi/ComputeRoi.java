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
import java.util.ArrayList;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class ComputeRoi implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    int imgHeigth;
    
    @Parameter(type = ItemIO.INPUT)
    double[] perimeterPixelListIndex;
    
    @Parameter(type = ItemIO.OUTPUT)
    PolygonRoi roi;
    
    @Override
    public void run() {
        
        ArrayList<Point> pointArray = new ArrayList<>();
        
        // create coordinate point list from pixel index
        for(int i = 0; i < perimeterPixelListIndex.length; i++){
            pointArray.add(new Point((int) (perimeterPixelListIndex[i] / imgHeigth),(int) (perimeterPixelListIndex[i] % imgHeigth)));
        }
        
        // get first point
        ArrayList<Point> sortedPointArray = new ArrayList<>();
        sortedPointArray.add(pointArray.remove(0));

        while(pointArray.size() > 0){
            // get last point
            Point currentPoint = sortedPointArray.get(sortedPointArray.size() - 1);
            
            // find next closest point
            Point nextPoint = null;
            ArrayList<Point> pixelArray = new ArrayList<>();
            for(int i = 0; i < pointArray.size(); i++){
                double dist = currentPoint.distance(pointArray.get(i));
                if(nextPoint == null){         
                    if(pointArray.size() == 1){
                        if(sortedPointArray.get(0).distance(pointArray.get(0)) < sortedPointArray.get(0).distance(sortedPointArray.get(sortedPointArray.size() - 1))){
                            nextPoint = pointArray.get(0);
                        }
                        else{
                            pixelArray.add(pointArray.get(0));
                        }
                    }
                    else{
                        nextPoint = pointArray.get(i);
                    }
                }
                else if( dist < currentPoint.distance(nextPoint)){
                    pixelArray.clear();
                    nextPoint = pointArray.get(i);
                }
                else if(dist == currentPoint.distance(nextPoint)){
                    pixelArray.add(pointArray.get(i));
                }
            }
            if(nextPoint != null){
                sortedPointArray.add(pointArray.remove(pointArray.indexOf(nextPoint)));
            }            
            pointArray.removeAll(pixelArray);
        }

        // create roi from sorted list
        int[] xPoints = new int[sortedPointArray.size()];
        int[] yPoints = new int[sortedPointArray.size()];
        int nPoints = sortedPointArray.size();
        for(int i = 0; i < sortedPointArray.size(); i++){
            xPoints[i] = sortedPointArray.get(i).x;
            yPoints[i] = sortedPointArray.get(i).y;
        }
        Polygon p = new Polygon(xPoints, yPoints, nPoints);
        roi = new PolygonRoi(p, Roi.POLYGON);
        
    }
    
}