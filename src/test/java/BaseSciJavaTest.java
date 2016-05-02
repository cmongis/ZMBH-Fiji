
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.scijava.Context;
import org.scijava.SciJava;
import org.scijava.plugin.PluginInfo;
import org.scijava.service.Service;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */

public class BaseSciJavaTest {
    
    public static SciJava scijava;
    
    public BaseSciJavaTest(){
        if(scijava == null) {
            System.out.println("Initializing SCIJAVA");
            
            // avoid error : Invalid service: net.imagej.legacy.LegacyConsoleService
            scijava = new SciJava(true); 
            List<PluginInfo<Service>> availableServices = scijava.getContext().getPluginIndex().getPlugins(Service.class);
            List<Class<? extends Service>> toLoad = new ArrayList<>();
            for(PluginInfo<Service> plinfo : availableServices){
                if(plinfo.getPluginClass() != null){
                    if(!plinfo.getClassName().contains("legacy")){
                        toLoad.add(plinfo.getPluginClass());
                        //System.out.println(plinfo.getPluginClass());
                    }
                }
            }           
            scijava = new SciJava(toLoad);
            System.out.println("Loading " + toLoad.size() + " services");
            scijava.getContext().inject(this);
            System.out.println("Injection OK");
        }
    }
      
}
