package zmbh.commands.correction;;

import bunwarpj.MiscTools;
import bunwarpj.Param;
import bunwarpj.Transformation;
import bunwarpj.bUnwarpJ_;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.util.Stack;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;


/**
 *
 * @author Potier Guillaume, 2016
 */
    
@Plugin(type = Command.class, menuPath = "Dev-commands>Correction>CMD bUnwarpJ", label="")
public class myBunWarpJ implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    ImagePlus targetImp;
    
    @Parameter(type = ItemIO.INPUT)
    ImagePlus sourceImp;
    
    @Parameter(type = ItemIO.INPUT)
    String landMarkFilePath;
    
    @Parameter(type = ItemIO.OUTPUT)
    ImagePlus correctedSource;
    
    @Override
    public void run() {
        int sourceWidth = sourceImp.getWidth();
        int sourceHeight = sourceImp.getHeight();
        int targetWidth = targetImp.getWidth();
        int targetHeight = targetImp.getHeight();
        Stack<Point> sourcePoints = new Stack<>();
        Stack<Point> targetPoints = new Stack<>();
        Param parameter = new Param(2, 0, 3, 4, 0, 0, 1, 0, 0, 0.01);
        
        MiscTools.loadPoints(landMarkFilePath, sourcePoints, targetPoints);
        Transformation warp = bUnwarpJ_.computeTransformationBatch(sourceWidth, sourceHeight, targetWidth, targetHeight, sourcePoints, targetPoints, parameter);
        MiscTools.applyTransformationToSourceMT(sourceImp, targetImp, warp.getIntervals(), warp.getDirectDeformationCoefficientsX(), warp.getDirectDeformationCoefficientsY());
        sourceImp.resetDisplayRange();
        correctedSource = sourceImp;
    }
    
}