/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.segmentation;

/**
 *
 * @author User
 */
public class CellXseed {
    private int id;
    
    private double majorAxisLength;
    private double minorAxisLength;
    
    private double cellVolume;
    private double cytosolVolume;
    
    private double equivDiameter;

    private double orientation;
    private double eccentricity;
    private double probBeingValid;
    
    private double houghCenterX;
    private double houghCenterY;
    private double houghRadius;
    
    private double centroidX;
    private double centroidY;
    
    private double perimeter;
    
    private double[] perimeterPixelListIndex;
    private double[] cytosolPixelListIndex;
    private double[] membranePixelListIndex;

    public CellXseed(int id, double majorAxisLength, double minorAxisLength, double cellVolume, double cytosolVolume, double equivDiameter, double orientation, double eccentricity, double probBeingValid, double houghCenterX, double houghCenterY, double houghRadius, double centroidX, double centroidY, double perimeter, double[] perimeterPixelListIndex, double[] cytosolPixelListIndex, double[] membranePixelListIndex) {
        this.id = id;
        this.majorAxisLength = majorAxisLength;
        this.minorAxisLength = minorAxisLength;
        this.cellVolume = cellVolume;
        this.cytosolVolume = cytosolVolume;
        this.equivDiameter = equivDiameter;
        this.orientation = orientation;
        this.eccentricity = eccentricity;
        this.probBeingValid = probBeingValid;
        this.houghCenterX = houghCenterX;
        this.houghCenterY = houghCenterY;
        this.houghRadius = houghRadius;
        this.centroidX = centroidX;
        this.centroidY = centroidY;
        this.perimeter = perimeter;
        this.perimeterPixelListIndex = perimeterPixelListIndex;
        this.cytosolPixelListIndex = cytosolPixelListIndex;
        this.membranePixelListIndex = membranePixelListIndex;
    }

    public int getId() {
        return id;
    }
    
    public double getMajorAxisLength() {
        return majorAxisLength;
    }

    public void setMajorAxisLength(double majorAxisLength) {
        this.majorAxisLength = majorAxisLength;
    }

    public double getMinorAxisLength() {
        return minorAxisLength;
    }

    public void setMinorAxisLength(double minorAxisLength) {
        this.minorAxisLength = minorAxisLength;
    }

    public double getCellVolume() {
        return cellVolume;
    }

    public void setCellVolume(double cellVolume) {
        this.cellVolume = cellVolume;
    }

    public double getCytosolVolume() {
        return cytosolVolume;
    }

    public void setCytosolVolume(double cytosolVolume) {
        this.cytosolVolume = cytosolVolume;
    }

    public double getEquivDiameter() {
        return equivDiameter;
    }

    public void setEquivDiameter(double equivDiameter) {
        this.equivDiameter = equivDiameter;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public double getProbBeingValid() {
        return probBeingValid;
    }

    public void setProbBeingValid(double probBeingValid) {
        this.probBeingValid = probBeingValid;
    }

    public double getHoughCenterX() {
        return houghCenterX;
    }

    public void setHoughCenterX(double houghCenterX) {
        this.houghCenterX = houghCenterX;
    }

    public double getHoughCenterY() {
        return houghCenterY;
    }

    public void setHoughCenterY(double houghCenterY) {
        this.houghCenterY = houghCenterY;
    }

    public double getHoughRadius() {
        return houghRadius;
    }

    public void setHoughRadius(double houghRadius) {
        this.houghRadius = houghRadius;
    }

    public double getCentroidX() {
        return centroidX;
    }

    public void setCentroidX(double centroidX) {
        this.centroidX = centroidX;
    }

    public double getCentroidY() {
        return centroidY;
    }

    public void setCentroidY(double centroidY) {
        this.centroidY = centroidY;
    }

    public double getPerimeter() {
        return perimeter;
    }

    public void setPerimeter(double perimeter) {
        this.perimeter = perimeter;
    }

    public double[] getPerimeterPixelListIndex() {
        return perimeterPixelListIndex;
    }

    public void setPerimeterPixelListIndex(double[] perimeterPixelListIndex) {
        this.perimeterPixelListIndex = perimeterPixelListIndex;
    }

    public double[] getCytosolPixelListIndex() {
        return cytosolPixelListIndex;
    }

    public void setCytosolPixelListIndex(double[] cytosolPixelListIndex) {
        this.cytosolPixelListIndex = cytosolPixelListIndex;
    }

    public double[] getMembranePixelListIndex() {
        return membranePixelListIndex;
    }

    public void setMembranePixelListIndex(double[] membranePixelListIndex) {
        this.membranePixelListIndex = membranePixelListIndex;
    }
    
    
    
    
}
