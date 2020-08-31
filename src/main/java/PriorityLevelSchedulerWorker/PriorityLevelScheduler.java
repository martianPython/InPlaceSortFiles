package PriorityLevelSchedulerWorker;

import ColorUiPackage.ConsoleColors;
import FileDaoWoker.FileTasks;
import FirstComeFirstServerWorker.AnonymousPriorityFileComparator;
import ProcessScheduleWorker.FileMovProcessTask;
import ProcessScheduleWorker.IProcessWimsSchedule;
import ProcessScheduleWorker.ProcessWimsSchedule;
import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
//
public class PriorityLevelScheduler {
ProcessWimsSchedule processWimsSchedule = new ProcessWimsSchedule();
public    void scheduleSingleSegmentWimsFiles(List<File> oneSegmentPriorityQueue, int[] oneSegmentMessageIdArray, File inputDirectory,String movDir) {
        System.out.println(ConsoleColors.RED_BOLD_BRIGHT + " starting scheduleSingleSegmentWimsFiles " + ConsoleColors.RESET);
        FileTasks fileTasks = new FileTasks();
        for (File aFile:oneSegmentPriorityQueue) {
            //System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + aFile +ConsoleColors.RESET);
            processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
           // System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT + " will check for higher Priority Messages here");
            List<Integer> oneSegmentMessageIdList =  Arrays.stream(oneSegmentMessageIdArray).boxed().collect(Collectors.toList());
            int[] higherPriorityMessageIdArray = Arrays.stream(oneSegmentMessageIdArray, 0, oneSegmentMessageIdList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
            fileTasks.scheduleHigherPriorityOneSegmentFilesPresent(inputDirectory,movDir,higherPriorityMessageIdArray);

        }
    }


    public  void scheduleMultiSegmentPriority1LevelWimsFiles(String wimsSatellite,HashMap<String, Boolean> roundRobinHashMap,HashMap<String, Boolean> firstComeFirstServerProcessingMap,
                                                             boolean sequencePreemptiveRoundRobin, File inputDirectory, String movDir, int[] multiSegmentsPriority1List,
                                                             int[] oneSegmentMessageIdArray, HashMap<String,Boolean> reverseHashMap) {
        System.out.println(ConsoleColors.RED_BOLD_BRIGHT + " starting scheduleMultiSegmentPriority1LevelWimsFiles " + ConsoleColors.RESET);
        FileTasks fileTasks = new FileTasks();
        if(!roundRobinHashMap.get("multiSegmentsPriority1List"))
        {
            List<File>  filesPriorityQueue = fileTasks.fetchFilesFromDirectoryPerMsgId(inputDirectory,multiSegmentsPriority1List);
            if(firstComeFirstServerProcessingMap.get("multiSegmentsPriority1List")){
                filesPriorityQueue =  sortFileBasedOnTimeStamp(filesPriorityQueue);
                filesPriorityQueue = fileTasks.reverseMessageId(reverseHashMap,filesPriorityQueue,multiSegmentsPriority1List,wimsSatellite);
                for (File aFile:filesPriorityQueue) {
                    //System.out.println(ConsoleColors.CYAN_BOLD_BRIGHT + aFile +ConsoleColors.RESET);
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
                    List<Integer> multipleSegmentPriority1LevelList =  Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority1List, 0, multipleSegmentPriority1LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
                   // System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));
                    fileTasks.scheduleHigherPriorityOneNMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,new int[]{});

                }
            }else{ // Strict Priority Scheduling
                filesPriorityQueue = fileTasks.reverseMessageId(reverseHashMap,filesPriorityQueue,multiSegmentsPriority1List,wimsSatellite);
                for (File aFile:filesPriorityQueue) {
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
                    List<Integer> multipleSegmentPriority1LevelList =  Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority1List, 0, multipleSegmentPriority1LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
                    System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));
                    fileTasks.scheduleHigherPriorityOneNMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,higherPriorityMessageIdArray);

                }
            }


        }else{
            //  Round Robin Processing to be executed here
            List<List<File>> listList = fileTasks.yieldSamePrioritySchedule(multiSegmentsPriority1List,(inputDirectory),reverseHashMap);
            listList.removeIf(List::isEmpty);
            File aFileInList= null;
            for (List<File> fileList: listList ) {
                for (File aFile: fileList) {
                    aFileInList=aFile;
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
                    //System.out.println(aFile);
                    if(!sequencePreemptiveRoundRobin){
                        List<Integer> multipleSegmentPriority1LevelList =  Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
                        int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority1List, 0, multipleSegmentPriority1LevelList.indexOf(Integer.parseInt(aFileInList.getName().split("_")[2]))).toArray();
                        fileTasks.scheduleHigherPriorityOneNMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,new int[]{});
                    }

                }
                if(sequencePreemptiveRoundRobin){
                    List<Integer> multipleSegmentPriority1LevelList =  Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority1List, 0, multipleSegmentPriority1LevelList.indexOf(Integer.parseInt(aFileInList.getName().split("_")[2]))).toArray();
                    System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));
                    fileTasks.scheduleHigherPriorityOneNMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,new int[]{});
                }

                System.out.println();
            }
// scheduling of     multiSegmentsPriority1List ends here
        }
    }
    public List<File> sortFileBasedOnTimeStamp(List<File> fileList){
        File[] arrayOfFiles = fileList.toArray(new File[0]);
        Arrays.sort(arrayOfFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
      fileList.clear();
        fileList =  new ArrayList<>(Arrays.asList(arrayOfFiles));
        return  fileList;
    }

    public  void scheduleMultiSegmentPriority2LevelWimsFiles(String wimsSatellite,HashMap<String, Boolean> roundRobinHashMap,HashMap<String, Boolean> firstComeFirstServerProcessingMap,boolean sequencePreemptiveRoundRobin,File inputDirectory,String movDir,int[] multiSegmentsPriority1List,int[] multiSegmentsPriority2List,
                                                                    int[] oneSegmentMessageIdArray,HashMap<String,Boolean> reverseHashMap) {
        FileTasks fileTasks = new FileTasks();
        if(!roundRobinHashMap.get("multiSegmentsPriority2List"))
        {
            List<File>  filesPriorityQueue = fileTasks.fetchFilesFromDirectoryPerMsgId(inputDirectory,multiSegmentsPriority2List);

            if(firstComeFirstServerProcessingMap.get("multiSegmentsPriority2List")){
                filesPriorityQueue =  sortFileBasedOnTimeStamp(filesPriorityQueue);
                filesPriorityQueue = fileTasks.reverseMessageId(reverseHashMap,filesPriorityQueue,multiSegmentsPriority2List,wimsSatellite);
                for (File aFile:filesPriorityQueue) {
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
                    List<Integer> multipleSegmentPriority2LevelList =  Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority2List, 0, multipleSegmentPriority2LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
                    fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,multiSegmentsPriority1List, new int[]{});

                }
            }else{
                filesPriorityQueue = fileTasks.reverseMessageId(reverseHashMap,filesPriorityQueue,multiSegmentsPriority2List,wimsSatellite);
                for (File aFile:filesPriorityQueue) {
                    // System.out.println(ConsoleColors.CYAN_BOLD_BRIGHT + aFile +ConsoleColors.RESET);
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
                    List<Integer> multipleSegmentPriority2LevelList =  Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority2List, 0, multipleSegmentPriority2LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
                    fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,multiSegmentsPriority1List,higherPriorityMessageIdArray);

                }
            }


        }else{
            //  Round Robin Processing to be executed here
            List<List<File>> listList = fileTasks.yieldSamePrioritySchedule(multiSegmentsPriority2List,(inputDirectory),reverseHashMap);
            listList.removeIf(List::isEmpty);
            for (List<File> fileList: listList ) {
                for (File aFile: fileList) {
                    System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT + aFile + ConsoleColors.RESET);
                }
                }
            File aFileInList= null;
            for (List<File> fileList: listList ) {
                for (File aFile: fileList) {
                    aFileInList=aFile;
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
                    //System.out.println(aFile);
                    if(!sequencePreemptiveRoundRobin){
                        List<Integer> multipleSegmentPriority2LevelList =  Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList());
                        int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority2List, 0, multipleSegmentPriority2LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
                        fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,multiSegmentsPriority1List,new int[] {});
                    }

                }
                if(sequencePreemptiveRoundRobin){
                    List<Integer> multipleSegmentPriority2LevelList =  Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = new int[4];
                    if (aFileInList != null) {
                        higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority2List, 0, multipleSegmentPriority2LevelList.indexOf(Integer.parseInt(aFileInList.getName().split("_")[2]))).toArray();
                    }
                    fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,multiSegmentsPriority1List,new int[] {});
                }


            }
// scheduling of     multiSegmentsPriority1List ends here
        }
    }

    public  void scheduleMultiSegmentRemainingPriorityWimsFiles(int[] oneSegmentMessageIdArray,int[] multiSegmentsPriority1List,int[] multiSegmentsPriority2List,
                                                                       File inputDirectory,String movDir) {
        System.out.println(ConsoleColors.RED_BOLD_BRIGHT + " starting scheduleMultiSegmentRemainingPriorityWimsFiles " + ConsoleColors.RESET);
        IProcessWimsSchedule processWimsSchedule = new ProcessWimsSchedule();
        FileTasks fileTasks = new FileTasks();
        List<Integer> totalMessageSetAssignedPriority = Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
        totalMessageSetAssignedPriority.addAll(Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList()));
        totalMessageSetAssignedPriority.addAll(Arrays.stream(oneSegmentMessageIdArray).boxed().collect(Collectors.toList()));
        List<File> anonymousPriorityFiles = fileTasks.fetchAnonymousPriorityFiles(inputDirectory,totalMessageSetAssignedPriority);
        for (File aFile:anonymousPriorityFiles) {
           processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
            fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,oneSegmentMessageIdArray,multiSegmentsPriority1List,multiSegmentsPriority2List);

        }
    }



}
