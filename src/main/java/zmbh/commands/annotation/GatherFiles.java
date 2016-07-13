/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class GatherFiles implements Command {
    
    
    @Parameter(type = ItemIO.INPUT)
    String recordClassDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String cellDirPath;
    
    @Parameter(type = ItemIO.INPUT)
    String stackDirPath;
    
    @Parameter(type = ItemIO.OUTPUT)
    Map<Map<String, File>, String> fileSerie;
    
    @Override
    public void run() {
        
        File recordClassDir = new File(recordClassDirPath);
        File cellDir = new File(cellDirPath);
        File dataDir = new File(stackDirPath);
        
        File[] recordClassFileList = recordClassDir.listFiles((File pathname) -> pathname.getName().endsWith(".csv"));
        File[] cellFileList = cellDir.listFiles((File pathname) -> pathname.getName().endsWith(".mat"));
        File[] stackFileList = dataDir.listFiles((File pathname) -> pathname.getName().endsWith(".tif"));
        
        fileSerie = new HashMap<>();      
        
        // Get mapping between records, sgmented cells and images
        // Files mapped together should have the same name
        for(File stackFile : stackFileList){
            String stackLabel = stackFile.getName().split("\\.")[0];
            File associatedCellFile = null;
            File associatedRecordFile = null;
            
            for(File cellFile : cellFileList){
                String cellLabel = cellFile.getName().split("\\.")[0];
                if(stackLabel.equals(cellLabel)){
                    associatedCellFile = cellFile;
                    break;
                }
            }
            for(File recordFile : recordClassFileList){
                String recordLabel = recordFile.getName().split("\\.")[0];
                if(stackLabel.equals(recordLabel)){
                    associatedRecordFile = recordFile;
                    break;
                }
            }
            
            if(associatedCellFile != null && associatedRecordFile != null){
                Map<String, File> fileSet = new HashMap<>();
                fileSet.put("stackFile", stackFile);
                fileSet.put("cellFile", associatedCellFile);
                fileSet.put("recordFile", associatedRecordFile);
                fileSerie.put(fileSet, stackLabel);
            }
            
        }
        
    }
    
}