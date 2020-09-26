package PriorityLevelSchedulerWorker;

import ColorUiPackage.ConsoleColors;
import FileDaoWoker.FileTasks;
import FirstComeFirstServerWorker.FilePriorityComparator;
import ProcessScheduleWorker.ProcessWimsSchedule;
import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
//
public class PriorityLevelScheduler {
    String satelliteId;
ProcessWimsSchedule processWimsSchedule = new ProcessWimsSchedule();

    public PriorityLevelScheduler(String satelliteId) {
        this.satelliteId = satelliteId;
    }

    public  void scheduleSingleSegmentWimsFiles(List<File> oneSegmentPriorityQueue, int[] oneSegmentMessageIdArray, File inputDirectory,String movDir,File archiveDirectory,long copyPollingDelay) {
//        System.out.println(ConsoleColors.RED_BOLD_BRIGHT + " starting scheduleSingleSegmentWimsFiles " + ConsoleColors.RESET);
        FileTasks fileTasks = new FileTasks();
        for (File aFile:oneSegmentPriorityQueue) {
            //System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + aFile +ConsoleColors.RESET);
            // System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT + " will check for higher Priority Messages here");
            List<Integer> oneSegmentMessageIdList =  Arrays.stream(oneSegmentMessageIdArray).boxed().collect(Collectors.toList());
            int[] higherPriorityMessageIdArray = Arrays.stream(oneSegmentMessageIdArray, 0, oneSegmentMessageIdList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
            fileTasks.scheduleHigherPriorityOneSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,higherPriorityMessageIdArray,copyPollingDelay);
            processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);

        }
    }


    public  void scheduleMultiSegmentPriority1LevelWimsFiles(String wimsSatellite,HashMap<String, Boolean> roundRobinHashMap,HashMap<String, Boolean> firstComeFirstServerProcessingMap,
                                                             boolean sequencePreemptiveRoundRobin, File inputDirectory, String movDir,File archiveDirectory, int[] multiSegmentsPriority1List,
                                                             int[] oneSegmentMessageIdArray, HashMap<String,Boolean> reverseHashMap,long copyPollingDelay) {
//        System.out.println(ConsoleColors.RED_BOLD_BRIGHT + " starting scheduleMultiSegmentPriority1LevelWimsFiles " + ConsoleColors.RESET);
        FileTasks fileTasks = new FileTasks();
        if(!roundRobinHashMap.get("multiSegmentsPriority1List"))
        {
            List<File>  filesPriorityQueue = fileTasks.fetchFilesFromDirectoryPerMsgId(inputDirectory,multiSegmentsPriority1List,satelliteId);
            if(firstComeFirstServerProcessingMap.get("multiSegmentsPriority1List")){
                filesPriorityQueue =  sortFileBasedOnTimeStamp(filesPriorityQueue);
//                System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT +  " POst Sorting as per Time Stamp " + ConsoleColors.RESET);
//                for (File file:filesPriorityQueue) {
//                    System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT + file + ConsoleColors.RESET);
//                }
                filesPriorityQueue = fileTasks.reverseMessageId(reverseHashMap,filesPriorityQueue,multiSegmentsPriority1List,wimsSatellite);
                for (File aFile:filesPriorityQueue) {
                    //System.out.println(ConsoleColors.CYAN_BOLD_BRIGHT + aFile +ConsoleColors.RESET);
//                    List<Integer> multipleSegmentPriority1LevelList =  Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
//                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority1List, 0, multipleSegmentPriority1LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
//                    System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));

                    List<File> oneSegmentPriorityQueue = fileTasks.fetchOneSegmentFilesPriorityWise(inputDirectory,oneSegmentMessageIdArray,satelliteId);
                    scheduleSingleSegmentWimsFiles(oneSegmentPriorityQueue,oneSegmentMessageIdArray,inputDirectory,movDir,archiveDirectory,copyPollingDelay);
//                    fileTasks.scheduleHigherPriorityOneNMultipleSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,oneSegmentMessageIdArray,new int[]{});
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);

                }
            }else{
                // Strict Priority Scheduling
                FilePriorityComparator filePriorityComparator = new FilePriorityComparator(multiSegmentsPriority1List);
                filesPriorityQueue.sort(filePriorityComparator);
                filesPriorityQueue = fileTasks.reverseMessageId(reverseHashMap,filesPriorityQueue,multiSegmentsPriority1List,wimsSatellite);
                for (File aFile:filesPriorityQueue) {
                    List<Integer> multipleSegmentPriority1LevelList =  Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority1List, 0, multipleSegmentPriority1LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
//                    System.out.println("--more priority from level 1 ---> " + Arrays.toString(higherPriorityMessageIdArray));


                    List<File> oneSegmentPriorityQueue = fileTasks.fetchOneSegmentFilesPriorityWise(inputDirectory,oneSegmentMessageIdArray,satelliteId);
                    scheduleSingleSegmentWimsFiles(oneSegmentPriorityQueue,oneSegmentMessageIdArray,inputDirectory,movDir,archiveDirectory,copyPollingDelay);
//
                    List<File>  updatedSameLevelHigherPriorityFiles = fileTasks.fetchFilesFromDirectoryPerMsgId(inputDirectory,higherPriorityMessageIdArray,satelliteId);
                    for (File aSameLevelHigherPriorityFile: updatedSameLevelHigherPriorityFiles) {
                        List<File> updatedOneSegmentPriorityQueue = fileTasks.fetchOneSegmentFilesPriorityWise(inputDirectory,oneSegmentMessageIdArray,satelliteId);
                        scheduleSingleSegmentWimsFiles(updatedOneSegmentPriorityQueue,oneSegmentMessageIdArray,inputDirectory,movDir,archiveDirectory,copyPollingDelay);
                        processWimsSchedule.consumeNProcessWimsScheduledFiles(aSameLevelHigherPriorityFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);

                    }
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);

//                    fileTasks.scheduleHigherPriorityOneNMultipleSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,oneSegmentMessageIdArray,higherPriorityMessageIdArray);

                }
            }
        }else{
            //  Round Robin Processing to be executed here
            List<List<File>> listList = fileTasks.yieldSamePrioritySchedule(multiSegmentsPriority1List,(inputDirectory),reverseHashMap,satelliteId);
            listList.removeIf(List::isEmpty);
            File aFileInList= null;
            for (List<File> fileList: listList ) {
                for (File aFile: fileList) {
                    aFileInList=aFile;
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);
                    //System.out.println(aFile);
                    if(!sequencePreemptiveRoundRobin){
                        List<Integer> multipleSegmentPriority1LevelList =  Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
                        int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority1List, 0, multipleSegmentPriority1LevelList.indexOf(Integer.parseInt(aFileInList.getName().split("_")[2]))).toArray();
                        System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));


                        List<File> oneSegmentPriorityQueue = fileTasks.fetchOneSegmentFilesPriorityWise(inputDirectory,oneSegmentMessageIdArray,satelliteId);
                        scheduleSingleSegmentWimsFiles(oneSegmentPriorityQueue,oneSegmentMessageIdArray,inputDirectory,movDir,archiveDirectory,copyPollingDelay);

//                        fileTasks.scheduleHigherPriorityOneNMultipleSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,oneSegmentMessageIdArray,new int[]{});
                    }

                }
                if(sequencePreemptiveRoundRobin){
                    List<Integer> multipleSegmentPriority1LevelList =  Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority1List, 0, multipleSegmentPriority1LevelList.indexOf(Integer.parseInt(aFileInList.getName().split("_")[2]))).toArray();
                    System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));
                    List<File> oneSegmentPriorityQueue = fileTasks.fetchOneSegmentFilesPriorityWise(inputDirectory,oneSegmentMessageIdArray,satelliteId);
                    scheduleSingleSegmentWimsFiles(oneSegmentPriorityQueue,oneSegmentMessageIdArray,inputDirectory,movDir,archiveDirectory,copyPollingDelay);

//                    fileTasks.scheduleHigherPriorityOneNMultipleSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,oneSegmentMessageIdArray,new int[]{});
                }

                System.out.println();
            }
// scheduling of     multiSegmentsPriority1List ends here
        }
    }
    private List<File> sortFileBasedOnTimeStamp(List<File> fileList){
        File[] arrayOfFiles = fileList.toArray(new File[0]);
        Arrays.sort(arrayOfFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
      fileList.clear();
        fileList =  new ArrayList<>(Arrays.asList(arrayOfFiles));
//        System.out.println("From sort based FUcntion");
//        for (File file:fileList ) {
//            System.out.println(ConsoleColors.BLUE_BOLD_BRIGHT + file + ConsoleColors.RESET);
//        }
        return  fileList;
    }

    public  void scheduleMultiSegmentPriority2LevelWimsFiles(String wimsSatellite,HashMap<String, Boolean> roundRobinHashMap,HashMap<String, Boolean> firstComeFirstServerProcessingMap,boolean sequencePreemptiveRoundRobin,File inputDirectory,String movDir
            ,File archiveDirectory,int[] multiSegmentsPriority1List,int[] multiSegmentsPriority2List,
                                                                    int[] oneSegmentMessageIdArray,HashMap<String,Boolean> reverseHashMap,long copyPollingDelay) {
//        System.out.println(ConsoleColors.RED_BOLD_BRIGHT + " starting scheduleMultiSegmentPriority2LevelWimsFiles " + ConsoleColors.RESET);
        FileTasks fileTasks = new FileTasks();
        if(!roundRobinHashMap.get("multiSegmentsPriority2List"))
        {
            List<File>  filesPriorityQueue = fileTasks.fetchFilesFromDirectoryPerMsgId(inputDirectory,multiSegmentsPriority2List,satelliteId);

            if(firstComeFirstServerProcessingMap.get("multiSegmentsPriority2List")){
                filesPriorityQueue =  sortFileBasedOnTimeStamp(filesPriorityQueue);
//                System.out.println("----------------------------here --------------------");
//                for (File file:filesPriorityQueue ) {
//                    System.out.println(ConsoleColors.BLUE + file + ConsoleColors.RESET);
//                }
                filesPriorityQueue = fileTasks.reverseMessageId(reverseHashMap,filesPriorityQueue,multiSegmentsPriority2List,wimsSatellite);
                for (File aFile:filesPriorityQueue) {
//                    List<Integer> multipleSegmentPriority2LevelList =  Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList());
//                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority2List, 0, multipleSegmentPriority2LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
//                    System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));
                    // scan if any Priority_1_Files have been queued or not //
//                    List<File> priority1FileList = fileTasks.fetchFilesFromDirectoryPerMsgId(inputDirectory,multiSegmentsPriority1List,satelliteId);
                    scheduleMultiSegmentPriority1LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDirectory,movDir,archiveDirectory,multiSegmentsPriority1List,oneSegmentMessageIdArray,
                            reverseHashMap,copyPollingDelay);
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);

//                    fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,oneSegmentMessageIdArray,multiSegmentsPriority1List, new int[]{});

                }
            }else{
                // strict priority scheduling
                FilePriorityComparator filePriorityComparator = new FilePriorityComparator(multiSegmentsPriority2List);
                filesPriorityQueue.sort(filePriorityComparator);
                filesPriorityQueue = fileTasks.reverseMessageId(reverseHashMap,filesPriorityQueue,multiSegmentsPriority2List,wimsSatellite);
                for (File aFile:filesPriorityQueue) {
                    // System.out.println(ConsoleColors.CYAN_BOLD_BRIGHT + aFile +ConsoleColors.RESET);
                    List<Integer> multipleSegmentPriority2LevelList =  Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority2List, 0, multipleSegmentPriority2LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
//                    System.out.println("--from priority level 2 -> " + Arrays.toString(higherPriorityMessageIdArray));
                    scheduleMultiSegmentPriority1LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDirectory,movDir,archiveDirectory,multiSegmentsPriority1List,oneSegmentMessageIdArray,
                            reverseHashMap,copyPollingDelay);

                    List<File>  updatedSameLevelHigherPriorityFiles = fileTasks.fetchFilesFromDirectoryPerMsgId(inputDirectory,higherPriorityMessageIdArray,satelliteId);
                    for (File aSameLevelHigherPriorityFile: updatedSameLevelHigherPriorityFiles) {
                        scheduleMultiSegmentPriority1LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDirectory,movDir,archiveDirectory,multiSegmentsPriority1List,oneSegmentMessageIdArray,
                                reverseHashMap,copyPollingDelay);
                        processWimsSchedule.consumeNProcessWimsScheduledFiles(aSameLevelHigherPriorityFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);

                    }
//                    fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,oneSegmentMessageIdArray,multiSegmentsPriority1List,higherPriorityMessageIdArray);
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);

                }
            }


        }else{
            //  Round Robin Processing to be executed here
            List<List<File>> listList = fileTasks.yieldSamePrioritySchedule(multiSegmentsPriority2List,(inputDirectory),reverseHashMap,satelliteId);
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
                    processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir,archiveDirectory,satelliteId,copyPollingDelay);
                    //System.out.println(aFile);
                    if(!sequencePreemptiveRoundRobin){
                        List<Integer> multipleSegmentPriority2LevelList =  Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList());
                        int[] higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority2List, 0, multipleSegmentPriority2LevelList.indexOf(Integer.parseInt(aFile.getName().split("_")[2]))).toArray();
                        System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));
                        scheduleMultiSegmentPriority1LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDirectory,movDir,archiveDirectory,multiSegmentsPriority1List,oneSegmentMessageIdArray,
                                reverseHashMap,copyPollingDelay);

//                        fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,oneSegmentMessageIdArray,multiSegmentsPriority1List,new int[]{});
                    }

                }
                if(sequencePreemptiveRoundRobin){
                    List<Integer> multipleSegmentPriority2LevelList =  Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList());
                    int[] higherPriorityMessageIdArray = new int[4];
                    if (aFileInList != null) {
                        higherPriorityMessageIdArray = Arrays.stream(multiSegmentsPriority2List, 0, multipleSegmentPriority2LevelList.indexOf(Integer.parseInt(aFileInList.getName().split("_")[2]))).toArray();
                    }
                    System.out.println("---> " + Arrays.toString(higherPriorityMessageIdArray));
                    scheduleMultiSegmentPriority1LevelWimsFiles(this.satelliteId,roundRobinHashMap,firstComeFirstServerProcessingMap,sequencePreemptiveRoundRobin,inputDirectory,movDir,archiveDirectory,multiSegmentsPriority1List,oneSegmentMessageIdArray,
                            reverseHashMap,copyPollingDelay);

//                    fileTasks.scheduleHigherPriority2NMultipleSegmentFilesPresent(inputDirectory,movDir,archiveDirectory,satelliteId,oneSegmentMessageIdArray,multiSegmentsPriority1List,new int[]{});
                }


            }
// scheduling of     multiSegmentsPriority1List ends here
        }
    }

   /* public  void scheduleMultiSegmentRemainingPriorityWimsFiles(int[] oneSegmentMessageIdArray,int[] multiSegmentsPriority1List,int[] multiSegmentsPriority2List,
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
    } */



}
