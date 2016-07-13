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
import java.util.List;
import java.util.Set;
import org.jgrapht.alg.HamiltonianCycle;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 *
 * @author Guillaume
 */

public class HamiltonianCycleHelper {
 
 private SimpleWeightedGraph<Point, DefaultWeightedEdge> g = new SimpleWeightedGraph<Point, DefaultWeightedEdge>(
   DefaultWeightedEdge.class);
 
 public void addVertex(Point id) {
  g.addVertex(id);
 }
 
 public void addVertex(List<Point> ids) {
  for (Point id : ids) {
   g.addVertex(id);
  }
 }
 
 public void addEdge(Point source, Point destination, int weight) {
  DefaultWeightedEdge edge = g.addEdge(source, destination);
  g.setEdgeWeight(edge, weight);
 }
 
 public void init(List<Point> points){
     addVertex(points);
     for(Point p1:points){
         for(Point p2:points){
             if(p1 != p2){
                 if(!g.containsEdge(p1, p2)){
                     DefaultWeightedEdge e = g.addEdge(p1, p2);
                     g.setEdgeWeight(e, p1.distance(p2));
                 }
             }
         }
     }
 }
 
 public Roi run(){
    List<Point> result = HamiltonianCycle.getApproximateOptimalForCompleteGraph(g);
     
    int[] xPoints = new int[result.size()];
    int[] yPoints = new int[result.size()];
    int nPoints = result.size();
    for(int k = 0; k < result.size(); k++){
        xPoints[k] = result.get(k).x;
        yPoints[k] = result.get(k).y;
    }
     
    Polygon p = new Polygon(xPoints, yPoints, nPoints);
    PolygonRoi roi = new PolygonRoi(p, Roi.POLYGON);
    
    return roi;
 }
 
 
}