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
    
    //@Parameter
    //File jsonFile;
    
    //@Parameter
    //File nd2Dir;
    
    //@Parameter
    //String pathToConverter;
    
    //@Parameter
    //String saveDir;
    @Parameter(label = "Landmarks File", required = false)
    File landmarksFile = null;

    @Parameter(type = ItemIO.INPUT)
    Dataset sourceDataset;

    @Parameter(type = ItemIO.INPUT)
    Dataset targetDataset;

    @Parameter(type = ItemIO.OUTPUT)
    Dataset outputDataset;
    
      /**
     * Image representation for source image
     */
    private ImagePlus sourceImp;
    /**
     * Image representation for target image
     */
    private ImagePlus targetImp;
    
    @Override
    public void run() {
        Runtime.getRuntime().gc();
        this.sourceImp = ImageJ1PluginAdapter.unwrapDataset(sourceDataset).duplicate();
        this.targetImp = ImageJ1PluginAdapter.unwrapDataset(targetDataset);
        Stack<Point> sourceStack = new Stack<>();
        Stack<Point> targetStack = new Stack<>();
        bunwarpj.Param parameter = new bunwarpj.Param(2, 0, 3, 4, 0, 0, 1, 0, 0, 0.01);//(mode, maxImageSubsamplingFactor, min_scale_deformation, max_scale_deformation, divWeight, curlWeight, landmarkWeight, imageWeight, consistencyWeight, stopThreshold);
//        sourceImp.setProcessor(sourceImp.getProcessor().convertToFloat());
//        targetImp.setProcessor(targetImp.getProcessor().convertToFloat());

//        sourceImp = convertToGray32(sourceImp);
//        targetImp = convertToGray32(targetImp);
                
        MiscTools.loadPoints(landmarksFile.getAbsolutePath(), sourceStack, targetStack);

        bunwarpj.Transformation transformation = bunwarpj.bUnwarpJ_.computeTransformationBatch(sourceImp.getWidth(), sourceImp.getHeight(), targetImp.getWidth(), targetImp.getHeight(), sourceStack, targetStack, parameter);
        bunwarpj.MiscTools.applyTransformationToSourceMT(targetImp, targetImp, transformation.getIntervals(), transformation.getDirectDeformationCoefficientsX(), transformation.getDirectDeformationCoefficientsY());
        sourceImp.resetDisplayRange();
        Img img = ImagePlusAdapter.wrapImgPlus(sourceImp);
        
        outputDataset = datasetService.create(img);//        init();
        uiService.show(outputDataset);
        
    }
    
}