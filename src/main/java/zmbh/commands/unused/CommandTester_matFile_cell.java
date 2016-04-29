/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLObject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * @author User
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>deprecated CMD readMatFile_getCellInfo", label="")
public class CommandTester_matFile_cell implements Command {
    
    @Parameter
    File cellFile;

    @Override
    public void run() {
        
        try {
            MatFileReader reader = new MatFileReader(cellFile.getPath());
            //System.out.println(reader.getMatFileHeader().toString());
            Map<String, MLArray> content = reader.getContent();
            //System.out.println(content);
            MLObject dataArray = (MLObject) content.get("cells");
            System.out.println(dataArray.getN());
            Map<String, MLArray> obj = dataArray.getFields(0);
            //System.out.println(obj.size());
            
        } catch (IOException ex) {
            Logger.getLogger(CommandTester_matFile_cell.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
