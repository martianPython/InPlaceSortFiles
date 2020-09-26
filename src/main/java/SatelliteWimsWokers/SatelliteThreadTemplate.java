package SatelliteWimsWokers;

import ColorUiPackage.ConsoleColors;
import FileDaoWoker.FileTasks;
import PriorityLevelSchedulerWorker.PriorityLevelScheduler;
import SchedulerConfiguration.SpacecraftConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;


public class SatelliteThreadTemplate implements Runnable{
    private SpacecraftConfig spacecraftConfig;
    private String satelliteId;
    private long iterationPollingDelay;
    private StringBuilder pathOfConfigFile = new StringBuilder("/opt/wims/config/AppConfig/").append("WimsSchedulerConfig.dat");
    //StringBuilder pathOfConfigFile = new StringBuilder("/home/user/WIMS/Config/").append("WimsSchedulerConfig.dat");
    private String inputDirectory ="";//="/home/koushik/Desktop/1A/";
    private String movDirectory="";//="/home/koushik/Desktop/MOV/1A/";
    private long oldTimestamp = 1;
    private HashMap<String,Boolean> roundRobinHashMap = new HashMap<>();
    private HashMap<String,Boolean> reverseHashMap = new HashMap<>();
   // File archiveDirectory = new File("/root/Desktop/wims_archive/");
   private HashMap<String, Boolean> firstComeFirstServerProcessingMap = new HashMap<>();
    HashMap<String, Boolean> priorityProcessingMap = new HashMap<>();
    private long pollingDelay =2000;
    private String archiveDirectory ="/root/Desktop/wims_archive/";
    boolean firstComeFirstServe =true;
    boolean sequencePreemptiveRoundRobin =false;
    private int[] oneSegmentMessageIdArray;//= {41,21};
    private int[] multiSegmentsPriority1List;//= {17,18};
    private int[] multiSegmentsPriority2List;// = {20};
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
    public SatelliteThreadTemplate(String satelliteId,SpacecraftConfig spacecraftConfig,long iterationPollingDelay){
        this.satelliteId=satelliteId;
        this.spacecraftConfig=spacecraftConfig;
        this.iterationPollingDelay = iterationPollingDelay;
    }

    @Override
    public void run() {

        while (true){
            System.out.println(ConsoleColors.YELLOW_BOLD_BRIGHT + "------------------Satellite Thread " + satelliteId + " [" + dateFormatter.format(new Date())+"]  ------------------"+ ConsoleColors.RESET);
            long newTimeStamp =getLastModifiedTime(pathOfConfigFile.toString());
            if(oldTimestamp !=newTimeStamp ){
                try {
                    try (InputStream input = new FileInputStream(pathOfConfigFile.toString())) {
                        Properties properties = new Properties();
                        properties.load(input);
                        inputDirectory = (properties.getProperty("inputDirectory"));
                        movDirectory =  (properties.getProperty("movDirectory"));
//                        firstComeFirstServe = Boolean.parseBoolean(properties.getProperty("firstComeFirstServe"));
                        sequencePreemptiveRoundRobin =spacecraftConfig.isPreemptAfterEachSequence();
                        roundRobinHashMap = spacecraftConfig.getRoundRobinMap();//(HashMap<String, Boolean>) Arrays.stream((properties.getProperty("roundRobinProcessingMap")).split(",")).map(s -> s.split(":")).collect(Collectors.toMap(e -> e[0], e -> Boolean.parseBoolean(e[1])));
                        reverseHashMap= spacecraftConfig.getReverseMap();
                        firstComeFirstServerProcessingMap=spacecraftConfig.getFirstComeFirstServeMap();
                        archiveDirectory=(properties.getProperty("archiveDirectory"));

                       // priorityProcessingMap = (HashMap<String, Boolean>) Arrays.stream((properties.getProperty("priorityProcessingMap")).split(",")).map(s -> s.split(":")).collect(Collectors.toMap(e -> e[0], e -> Boolean.parseBoolean(e[1])));
                        pollingDelay = spacecraftConfig.getPoolingDelay();
                        oneSegmentMessageIdArray = spacecraftConfig.getOneSegmentMessageId().stream().mapToInt(Integer::intValue).toArray();
                        multiSegmentsPriority1List = spacecraftConfig.getMultiSegmentsPriority1List().stream().mapToInt(Integer::intValue).toArray();
                        multiSegmentsPriority2List = spacecraftConfig.getMultiSegmentsPriority2List().stream().mapToInt(Integer::intValue).toArray();

                        } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } catch (Exception ioException) {
                    ioException.printStackTrace();
                }
                oldTimestamp = newTimeStamp;
//                System.out.println(" The Config File Read : Pooling Interval  pollingDelay " + pollingDelay);
            }
            PriorityLevelScheduler priorityLevelScheduler = new PriorityLevelScheduler(this.satelliteId);
            File archveDirectory = new File(archiveDirectory);
            File inputDir = new File(inputDirectory+this.satelliteId);
//            System.out.println(ConsoleColors.BLUE_BOLD_BRIGHT + " The Input directory " + inputDir + ConsoleColors.RESET);
            FileTasks fileTasks = new FileTasks();
//            System.out.println(reverseHashMap);
            // scheduling of singleSegment Message Starts here
            List<File> oneSegmentPriorityQueue = fileTasks.fetchOneSegmentFilesPriorityWise(inputDir,oneSegmentMessageIdArray,satelliteId);
//            System.out.println(ConsoleColors.RED_BOLD_BRIGHT + "The archive directory " + archiveDirectory);
            priorityLevelScheduler.scheduleSingleSegmentWimsFiles(oneSegmentPriorityQueue,oneSegmentMessageIdArray,inputDir,movDirectory,archveDirectory, pollingDelay);

// scheduling of singleSegment Message ends here
            // scheduling of     multiSegmentsPriority1List starts here
            priorityLevelScheduler.scheduleMultiSegmentPriority1LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDir,movDirectory,archveDirectory,multiSegmentsPriority1List,oneSegmentMessageIdArray,
                    reverseHashMap, pollingDelay);

            priorityLevelScheduler.scheduleMultiSegmentPriority2LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDir,movDirectory,archveDirectory,multiSegmentsPriority1List,multiSegmentsPriority2List,oneSegmentMessageIdArray,
                    reverseHashMap, pollingDelay);

            //priorityLevelScheduler.scheduleMultiSegmentRemainingPriorityWimsFiles(oneSegmentMessageIdArray,multiSegmentsPriority1List,multiSegmentsPriority2List,inputDir,movDirectory);

            delay.accept( iterationPollingDelay);

        }
    }
    private long getLastModifiedTime(String configFileName) {
        return (new File(configFileName).lastModified());
    }
    private static final Consumer<Long> delay = (pollingDelay) ->{
        try {
            Thread.sleep(pollingDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
    }

