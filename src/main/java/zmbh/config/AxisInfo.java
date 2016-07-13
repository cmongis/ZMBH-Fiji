/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.config;

/**
 *
 * @author User
 */
public class AxisInfo {
    private String axisType;
    private long axeDim;

    public AxisInfo(String axisType, long axeDim) {
        this.axisType = axisType;
        this.axeDim = axeDim;
    }
    
    public AxisInfo(){
        super();
    }

    public String getAxisType() {
        return axisType;
    }

    public void setAxisType(String axisType) {
        this.axisType = axisType;
    }

    public long getAxeDim() {
        return axeDim;
    }

    public void setAxeDim(int axeDim) {
        this.axeDim = axeDim;
    }
    
    
}
