/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import net.imagej.Dataset;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class)
public class GetMaxValueCommand implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    Dataset inDataset;
    
    @Parameter(type = ItemIO.OUTPUT)
    float maxValue;
    
    
    @Override
    public void run() {
        
        maxValue = -1;
        float pixelValue;
        
        Cursor<RealType<?>> cursor = inDataset.cursor();
        while(cursor.hasNext()){
            cursor.next();
            pixelValue = cursor.get().getRealFloat();
            if(pixelValue > maxValue){
                maxValue = pixelValue;
            }
        }
    }
    
}