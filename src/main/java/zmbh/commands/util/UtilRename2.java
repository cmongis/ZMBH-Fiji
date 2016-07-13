package zmbh.commands.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author User
 */

@Plugin(type = Command.class)
public class UtilRename2 implements Command {

    @Parameter
    String dir;
    
    
    @Override
    public void run() {
        
        File[] filelist = new File(dir).listFiles((File dir, String name) -> name.endsWith(".tif"));
        System.out.println(filelist.length + " files in " + dir);
        Set<String> nameSet = new TreeSet<>();
        
        for(File file : filelist){
            nameSet.add(file.getName().split("\\.")[0]);
        }

        int counter = 0;
        for(String part : nameSet){
            for(File file : filelist){
                if(file.getName().contains(part)){
                    String formatCounter = String.format("%05d", counter);
                    String newName = formatCounter + "_" + file.getName();
                    System.out.println(newName);
                    file.renameTo(new File(dir + "/" + newName));
                }
            }
            counter++;
        }
    }
    
}