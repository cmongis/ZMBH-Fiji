/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.segmentation;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class LoadCellList implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    File cellFile;
    
    @Parameter(type = ItemIO.OUTPUT)
    ArrayList<CellXseed> cellxSeedList;
    
    @Override
    public void run() {
        
        try {
            MatFileReader reader = new MatFileReader(cellFile.getPath());
            Map<String, MLArray> content = reader.getContent();
            
            MLObject dataArray =  (MLObject) content.get("cells");
            cellxSeedList = new ArrayList<>();
            
            for(int k = 0; k < dataArray.getSize(); k++){
                
                Map<String, MLArray> fields = dataArray.getFields(k);
                
                int size = ((MLDouble) fields.get("perimeterPixelListLindx")).getSize();
                double[] perimeterPixelListIndex = new double[size];
                for(int i = 0; i < size; i++){
                    perimeterPixelListIndex[i] = ((MLDouble) fields.get("perimeterPixelListLindx")).get(i);
                }

                size = ((MLDouble) fields.get("cytosolPixelListLindx")).getSize();
                double[] cytosolPixelListIndex = new double[size];
                for(int i = 0; i < size; i++){
                    cytosolPixelListIndex[i] = ((MLDouble) fields.get("cytosolPixelListLindx")).get(i);
                }

                size = ((MLDouble) fields.get("membranePixelListLindx")).getSize();
                double[] membranePixelListIndex = new double[size];
                for(int i = 0; i < size; i++){
                    membranePixelListIndex[i] = ((MLDouble) fields.get("membranePixelListLindx")).get(i);
                }


                CellXseed segCell = new CellXseed(k,
                                                  ((MLDouble) dataArray.getFields(k).get("majorAxisLength")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("minorAxisLength")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("cellVolume")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("cytosolVolume")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("equivDiameter")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("orientation")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("eccentricity")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("probBeingValid")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("houghCenterX")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("houghCenterY")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("houghRadius")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("centroid")).get(0),
                                                  ((MLDouble) dataArray.getFields(k).get("centroid")).get(1),
                                                  ((MLDouble) dataArray.getFields(k).get("perimeter")).get(0),
                                                  perimeterPixelListIndex,
                                                  cytosolPixelListIndex,
                                                  membranePixelListIndex
                );
                
                cellxSeedList.add(segCell);
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(LoadCellList.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}