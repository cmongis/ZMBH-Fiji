package zmbh.commands;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.plugins.commands.assign.SubtractFromDataValues;
import net.imagej.plugins.commands.axispos.AxisPositionForward;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD subDarkSignal_divideFlatfield", label="")
public class CommandTester_flatfield implements Command {
    
    @Parameter
    CommandService commandService;
    
    @Parameter(type = ItemIO.BOTH)
    Dataset inputDataset;
    
    @Parameter(type = ItemIO.INPUT)
    float darkfieldValue;
    
    @Parameter(type = ItemIO.INPUT)
    File mCherryFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File gfpFlatFieldFile;
    
    @Parameter(type = ItemIO.INPUT)
    File bfpFlatFieldFile;
    
    @Override
    public void run() {
        
        try {
            Future promise;
            
            promise = commandService.run(SubtractFromDataValues.class, true, "value", darkfieldValue, "preview", false, "allPlanes", false);
            promise.get();
            promise = commandService.run(DivideBy.class, true, "file", mCherryFlatFieldFile);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            
            promise = commandService.run(SubtractFromDataValues.class, true, "value", darkfieldValue, "preview", false, "allPlanes", false);
            promise.get();
            promise = commandService.run(DivideBy.class, true, "file", mCherryFlatFieldFile);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            
            promise = commandService.run(SubtractFromDataValues.class, true, "value", darkfieldValue, "preview", false, "allPlanes", false);
            promise.get();
            promise = commandService.run(DivideBy.class, true, "file", gfpFlatFieldFile);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            
            promise = commandService.run(SubtractFromDataValues.class, true, "value", darkfieldValue, "preview", false, "allPlanes", false);
            promise.get();
            promise = commandService.run(DivideBy.class, true, "file", gfpFlatFieldFile);
            promise.get();
            
            promise = commandService.run(AxisPositionForward.class, true);
            promise.get();
            
            promise = commandService.run(SubtractFromDataValues.class, true, "value", darkfieldValue, "preview", false, "allPlanes", false);
            promise.get();
            promise = commandService.run(DivideBy.class, true, "file", bfpFlatFieldFile);
            promise.get();
            
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(CommandTester_flatfield.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }   
}
