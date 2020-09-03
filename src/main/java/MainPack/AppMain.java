package MainPack;

import ColorUiPackage.ConsoleColors;
import FileDaoWoker.FileTasks;
import PriorityLevelSchedulerWorker.PriorityLevelScheduler;
import SatelliteWimsWokers.SatelliteThreadTemplate;
import SchedulerConfiguration.SpacecraftConfig;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static PriorityLevelSchedulerWorker.PriorityLevelScheduler.*;

public class AppMain {
    public static void main(String[] args) {


        String[] satellitesAllocatedForWims = new String[0];
        //int number =3;
        // AtomicBoolean [] atomicBooleansArray = new AtomicBoolean[number];
        //  Arrays.fill(atomicBooleansArray,new AtomicBoolean(false));
        //atomicBooleansArray[0].set(true  );
        //StringBuilder irnssSpacecraftConfigFile = new StringBuilder("Irnss_");
        StringBuilder pathOfConfigFile = new StringBuilder("/home/koushik/Desktop/WIMS/Config/").append("WimsSchedulerConfig.dat");
       // StringBuilder pathOfConfigFile = new StringBuilder("/home/user/WIMS/Config/").append("WimsSchedulerConfig.dat");
        try (InputStream input = new FileInputStream(pathOfConfigFile.toString())) {
            Properties prop = new Properties();
            prop.load(input);
            satellitesAllocatedForWims = (prop.getProperty("wimsSatellites").split(","));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(2);
        ThreadPoolExecutor threadPoolExecutorService = (ThreadPoolExecutor) threadPoolExecutor;
        System.out.println(Arrays.toString(satellitesAllocatedForWims));


        for (String aWimsSatellite : satellitesAllocatedForWims) {
            StringBuilder irnssSpacecraftConfigFile =new StringBuilder("/home/koushik/Desktop/WIMS/Config/Irnss_").append(aWimsSatellite).append("_WIMS_Config.txt");
            System.out.println(irnssSpacecraftConfigFile.toString());
         SpacecraftConfig spacecraftConfig = SpacecraftConfig.getYamlConfiguration(irnssSpacecraftConfigFile.toString());
            System.out.println(spacecraftConfig.toString());
            SatelliteThreadTemplate satellite1AThread = new SatelliteThreadTemplate(aWimsSatellite,spacecraftConfig);
            threadPoolExecutorService.execute(satellite1AThread);
        }


    }

}
