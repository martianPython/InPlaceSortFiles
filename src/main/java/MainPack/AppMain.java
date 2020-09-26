package MainPack;

import SatelliteWimsWokers.SatelliteThreadTemplate;
import SchedulerConfiguration.SpacecraftConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class AppMain {
    public static void main(String[] args) {


        String[] satellitesAllocatedForWims = new String[0];
        long iterationPollingDelay=0;
        StringBuilder pathOfConfigFile = new StringBuilder("/opt/wims/config/AppConfig/").append("WimsSchedulerConfig.dat");
        try (InputStream input = new FileInputStream(pathOfConfigFile.toString())) {
            Properties prop = new Properties();
            prop.load(input);
            satellitesAllocatedForWims = (prop.getProperty("wimsSatellites").split(","));
            iterationPollingDelay = Long.parseLong(prop.getProperty("iterationPollingDelay"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(satellitesAllocatedForWims.length);
        ThreadPoolExecutor threadPoolExecutorService = (ThreadPoolExecutor) threadPoolExecutor;
//        System.out.println(Arrays.toString(satellitesAllocatedForWims));

        for (String aWimsSatellite : satellitesAllocatedForWims) {
            StringBuilder irnssSpacecraftConfigFile =new StringBuilder("/opt/wims/config/SatConfig/Irnss_").append(aWimsSatellite).append("_WIMS_Config.yaml");
//            System.out.println(irnssSpacecraftConfigFile.toString());
         SpacecraftConfig spacecraftConfig = SpacecraftConfig.getYamlConfiguration(irnssSpacecraftConfigFile.toString());
//            System.out.println(spacecraftConfig.toString());
            SatelliteThreadTemplate satellite1AThread = new SatelliteThreadTemplate(aWimsSatellite,spacecraftConfig,iterationPollingDelay);
            threadPoolExecutorService.execute(satellite1AThread);
        }


    }

}
