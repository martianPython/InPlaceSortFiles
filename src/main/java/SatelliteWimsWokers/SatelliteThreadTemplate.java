package SatelliteWimsWokers;

import FileDaoWoker.FileTasks;
import PriorityLevelSchedulerWorker.PriorityLevelScheduler;
import SchedulerConfiguration.SpacecraftConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static PriorityLevelSchedulerWorker.PriorityLevelScheduler.*;


public class SatelliteThreadTemplate implements Runnable{
    SpacecraftConfig spacecraftConfig;
    String satelliteId;
    StringBuilder pathOfConfigFile = new StringBuilder("/home/koushik/Desktop/WIMS/Config/").append("WimsSchedulerConfig.dat");
    //StringBuilder pathOfConfigFile = new StringBuilder("/home/user/WIMS/Config/").append("WimsSchedulerConfig.dat");
    private String inputDirectory ="";//="/home/koushik/Desktop/1A/";
    private String movDirectory="";//="/home/koushik/Desktop/MOV/1A/";
    private long oldTimestamp = 1;
    HashMap<String,Boolean> roundRobinHashMap = new HashMap<>();
    HashMap<String,Boolean> reverseHashMap = new HashMap<>();
    HashMap<String, Boolean> firstComeFirstServerProcessingMap = new HashMap<>();
    HashMap<String, Boolean> priorityProcessingMap = new HashMap<>();
    private long pollingDelay1A =2000;
    boolean firstComeFirstServe =true;
    boolean sequencePreemptiveRoundRobin =false;
    private int[] oneSegmentMessageIdArray;//= {41,21};
    private int[] multiSegmentsPriority1List;//= {17,18};
    private int[] multiSegmentsPriority2List;// = {20};
    public SatelliteThreadTemplate(String satelliteId,SpacecraftConfig spacecraftConfig){
        this.satelliteId=satelliteId;
        this.spacecraftConfig=spacecraftConfig;
    }
    PriorityLevelScheduler priorityLevelScheduler = new PriorityLevelScheduler();
    @Override
    public void run() {

        while (true){
            // System.out.println(ConsoleColors.CYAN + " from 1A " + ConsoleColors.RESET);
            long newTimeStamp =getLastModifiedTime(pathOfConfigFile.toString());
            if(oldTimestamp !=newTimeStamp ){
                try {
                    try (InputStream input = new FileInputStream(pathOfConfigFile.toString())) {
                        Properties properties = new Properties();
                        properties.load(input);
                        inputDirectory = (properties.getProperty("inputDirectory"));
                        movDirectory =  (properties.getProperty("movDirectory"));
                        firstComeFirstServe = Boolean.parseBoolean(properties.getProperty("firstComeFirstServe"));
                        sequencePreemptiveRoundRobin = Boolean.parseBoolean(properties.getProperty("sequencePreemptiveRoundRobin"));
                        roundRobinHashMap = spacecraftConfig.getRoundRobinMap();//(HashMap<String, Boolean>) Arrays.stream((properties.getProperty("roundRobinProcessingMap")).split(",")).map(s -> s.split(":")).collect(Collectors.toMap(e -> e[0], e -> Boolean.parseBoolean(e[1])));
                        reverseHashMap= spacecraftConfig.getReverseMap();
                        firstComeFirstServerProcessingMap=spacecraftConfig.getFirstComeFirstServeMap();
                       // priorityProcessingMap = (HashMap<String, Boolean>) Arrays.stream((properties.getProperty("priorityProcessingMap")).split(",")).map(s -> s.split(":")).collect(Collectors.toMap(e -> e[0], e -> Boolean.parseBoolean(e[1])));
                        pollingDelay1A = spacecraftConfig.getPoolingDelay();
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
                System.out.println(" The Config File Read : Pooling Interval  pollingDelay1A " + pollingDelay1A );
            }
            File inputDir = new File(inputDirectory+this.satelliteId);
            FileTasks fileTasks = new FileTasks();
            System.out.println(reverseHashMap);
            // scheduling of singleSegment Message Starts here
            List<File> oneSegmentPriorityQueue = fileTasks.scheduleOneSegmentFilesPriorityWise(inputDir,oneSegmentMessageIdArray);
            priorityLevelScheduler.scheduleSingleSegmentWimsFiles(oneSegmentPriorityQueue,oneSegmentMessageIdArray,inputDir,movDirectory);

// scheduling of singleSegment Message ends here
            // scheduling of     multiSegmentsPriority1List starts here
            priorityLevelScheduler.scheduleMultiSegmentPriority1LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDir,movDirectory,multiSegmentsPriority1List,oneSegmentMessageIdArray,
                    reverseHashMap);

            priorityLevelScheduler.scheduleMultiSegmentPriority2LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDir,movDirectory,multiSegmentsPriority1List,multiSegmentsPriority2List,oneSegmentMessageIdArray,
                    reverseHashMap);

            priorityLevelScheduler.scheduleMultiSegmentRemainingPriorityWimsFiles(oneSegmentMessageIdArray,multiSegmentsPriority1List,multiSegmentsPriority2List,inputDir,movDirectory);

            sleep.accept((long) 5000);
        }
    }
    public long getLastModifiedTime(String configFileName) {
        return (new File(configFileName).lastModified());
    }
    private static final Consumer<Long> sleep = (pollingDelay) ->{
        try {
            Thread.sleep(pollingDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
    }

