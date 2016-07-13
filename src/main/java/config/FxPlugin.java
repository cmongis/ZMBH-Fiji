/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import ij.ImagePlus;
import io.scif.services.DatasetIOService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import zmbh.commands.ImageJ1PluginAdapter;

/**
 *
 * @author Guillaume
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>FX>JavaFX Plugin")
public class FxPlugin implements Command {
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    ImageDisplayService imageDisplayService;
    
    @Parameter
    DisplayService displayService;
    
    @Parameter
    private ImageJ ij;
    
    @Parameter(type = ItemIO.INPUT)
    File stackFile;
    
    @Override
    public void run() {
        
        try {
            Dataset dataset = ioService.open(stackFile.getPath());
            //DatasetView view = (DatasetView) imageDisplayService.createDataView(dataset);
            ImageDisplay view = (ImageDisplay) displayService.createDisplayQuietly(dataset);
                       
            
            MainAppFrame app = new MainAppFrame(ij, view);
            app.setTitle("Stack Config Builder");
            app.init();
        } catch (IOException ex) {
            Logger.getLogger(FxPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}