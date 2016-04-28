package zmbh.commands.unused;


import ij.ImagePlus;
import zmbh.commands.ImageJ1PluginAdapter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
public class ImageJ1Runner extends ImageJ1PluginAdapter {

    @Override
    public ImagePlus run(ImagePlus input) {
        
        //Analyzer analyzer = new Analyzer();
        //analyzer.setup("set", input);
        //analyzer.measure();
        
        return input;
    }
    
}
