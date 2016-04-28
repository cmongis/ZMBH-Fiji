/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import io.scif.services.DatasetIOService;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.imagej.Dataset;
import net.imagej.operator.OpMultiply;
import net.imagej.plugins.commands.calculator.ImageCalculator;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD applyMask", label="")
public class CommandTester_applyMask implements Command {

    @Parameter
    CommandService commandService;

    
    @Parameter
    DatasetIOService datasetIoService;
    
    @Override
    public void run() {
        
        Dataset input1 = null;
        Dataset input2 = null;
        try {
            input1 = datasetIoService.open("src/main/resources/WellA01_Point0000_Seq0000-1_32.tif");
            input2 = datasetIoService.open("src/main/resources/WellA01_Point0000_Seq0000-1_mask_32.tif");
        } catch (IOException ex) {
            Logger.getLogger(CommandTester_applyMask.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        commandService.run(ImageCalculator.class, true, "input1", input1, "input2", input2, "op", new OpMultiply(), "newWindow", true, "wantDoubles", false);
    }
}
