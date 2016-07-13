/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
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
public class SaveJSON implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    StructureInfo obj;
    
    @Parameter(type = ItemIO.INPUT)
    File saveFile;
    
    @Override
    public void run() {
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(saveFile, obj);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(GetStackStructure.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SaveJSON.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}