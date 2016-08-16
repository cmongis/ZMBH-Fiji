/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

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
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>Util>nd2 converter")
public class Nd2Converter implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    File nd2Dir;
    
    @Parameter(type = ItemIO.INPUT)
    String pathToConverter;
    
    @Parameter(type = ItemIO.INPUT)
    int nbSeries;
    
    @Parameter(type = ItemIO.INPUT)
    String saveDir;
    
    @Override
    public void run() {
        
        File[] fileList = nd2Dir.listFiles((File pathname) -> pathname.getName().endsWith(".nd2"));
        for(File file : fileList){
            String split = file.getName().split("\\.")[0];
            String[] split2 = split.split("_");
            String well = split2[0];
            String seq = split2[1];

            for(int i = 0; i < nbSeries; i++){
                String outName = well + "_" + "Point"+ String.format("%04d", i) + "_" + seq + ".tif";
                outName = saveDir + "/" + outName;
                ProcessBuilder builder = new ProcessBuilder(
                    pathToConverter,
                    "-series",
                    Integer.toString(i),
                    file.getPath(),
                    outName);                
                builder.inheritIO();
                for(String str : builder.command()){
                    System.out.print(str + " ");
                }
                System.out.println("");
                try {
                java.lang.Process process = builder.start();
                process.waitFor();
                } catch (IOException ex) {
                    Logger.getLogger(RunRScript.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
                }
            }                
        }        
    }      
}