/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands;

import bunwarpj.MiscTools;
import ij.ImagePlus;
import io.scif.services.DatasetIOService;
import java.awt.Point;
import java.io.File;
import java.util.Stack;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleService;
import org.scijava.object.DefaultObjectService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.ui.UIService;

/**
 *
 * @author Potier Guillaume, 2016
 */

@Plugin(type = Command.class, menuPath = "Dev-commands>CMD Test", label="")
public class test implements Command {
    @Parameter
    DatasetService datasetService;
    
    @Parameter
    DatasetIOService ioService;
    
    @Parameter
    DefaultObjectService DefaultObjectService;
    
    @Parameter
    ModuleService mService;
    
    @Parameter
    CommandService cmdService;
    
    @Parameter
    ObjectService objService;
    
    @Parameter
    PluginService pluginService;
    
    @Parameter
    UIService uiService;
    @Override
    public void run() {
        
    }
    
}