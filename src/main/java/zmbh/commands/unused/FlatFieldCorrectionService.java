/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zmbh.commands.unused;

import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import org.scijava.service.SciJavaService;

/**
 * @author Guillaume
 */

public interface FlatFieldCorrectionService extends SciJavaService{
    void substractDarkSignal(ImageDisplay currentDisplay, Dataset inputDataset, int expValue);
    void divideBymCherryFlatField(Dataset inputDataset, Dataset flatFieldDataset);
    void divideByGfpFlatField(Dataset inputDataset, Dataset flatFieldDataset);

    public void divideByFlatField(ImageDisplay currentDisplay, Dataset dataset, Dataset flatFieldDataset);
}
