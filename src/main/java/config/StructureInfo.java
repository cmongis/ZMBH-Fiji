/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.util.Map;

/**
 *
 * @author User
 */
public class StructureInfo {
    private int numDimensions;
    private Map<Integer, AxisInfo> axisMap;
    
    /*
    private ImgCoord fluoQuant1;
    private ImgCoord fluoQuant2;
    private ImgCoord fluoDiscri;
    private ImgCoord brightfield;
    */

    public StructureInfo(int numDimensions, Map<Integer, AxisInfo> axisMap) {
        this.numDimensions = numDimensions;
        this.axisMap = axisMap;
    }
    
    public StructureInfo(){
        super();
    }

    public int getNumDimensions() {
        return numDimensions;
    }

    public void setNumDimensions(int numDimensions) {
        this.numDimensions = numDimensions;
    }

    public Map<Integer, AxisInfo> getAxisMap() {
        return axisMap;
    }

    public void setAxisMap(Map<Integer, AxisInfo> axisMap) {
        this.axisMap = axisMap;
    }

    
    
}
