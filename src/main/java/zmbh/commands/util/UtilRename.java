/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.util;

import java.io.File;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Guillaume
 */

@Plugin(type = Command.class,  menuPath = "Dev-commands>Util>Rename")
public class UtilRename implements Command {

    @Parameter
    String stackDir;
    
    @Parameter
    String maskDir;
    
    @Override
    public void run() {
        
        File[] stacklist = new File(stackDir).listFiles((File dir, String name) -> name.endsWith(".tif"));
        System.out.println(stacklist.length + " files in " + stackDir);

        File[] masklist = new File(maskDir).listFiles((File dir, String name) -> name.endsWith(".mat"));
        System.out.println(masklist.length + " files in " + maskDir);
        
        for(int i = 0; i < stacklist.length; i++){
            String newName = stacklist[i].getName().split("\\.")[0] + ".mat";
            //System.out.println(newName);
            //System.out.println(maskDir + "/" + newName);
            masklist[i].renameTo(new File(maskDir + "/" + newName));
        }
        
    }
    
}