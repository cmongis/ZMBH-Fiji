/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.config;

import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Guillaume
 */
public class ImgCoord {
    
   private SimpleStringProperty   imgName;
   private Map<AxisInfo, Integer> axisCoordMap;

    public ImgCoord(String imgName, Map<AxisInfo, Integer> axisCoordList) {
        this.imgName = new SimpleStringProperty(imgName);
        this.axisCoordMap = axisCoordList;
    }
   
   public ImgCoord(){
       super();
   }

    public Map<AxisInfo, Integer> getAxisCoordMap() {
        return axisCoordMap;
    }

    public void setAxisCoordMap(Map<AxisInfo, Integer> axisCoordList) {
        this.axisCoordMap = axisCoordList;
    }
   
   
}
