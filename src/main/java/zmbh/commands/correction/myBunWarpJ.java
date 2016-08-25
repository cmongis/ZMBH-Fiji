package zmbh.commands.correction;;

import bunwarpj.MiscTools;
import bunwarpj.Transformation;
import ij.ImagePlus;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;


/**
 *
 * @author Potier Guillaume, 2016
 */
    
@Plugin(type = Command.class)
public class myBunWarpJ implements Command {
    
    @Parameter(type = ItemIO.INPUT)
    ImagePlus targetImp;
    
    @Parameter(type = ItemIO.INPUT)
    ImagePlus sourceImp;    
    
    @Parameter(type = ItemIO.INPUT)
    Transformation warp;
    
    @Parameter(type = ItemIO.OUTPUT)
    ImagePlus correctedSource;
    
    
    
    @Override
    public void run() {
        //Apply previously computed transformation
        MiscTools.applyTransformationToSourceMT(sourceImp, targetImp, warp.getIntervals(), warp.getDirectDeformationCoefficientsX(), warp.getDirectDeformationCoefficientsY());
        sourceImp.resetDisplayRange();
        correctedSource = sourceImp;
    }
    
}