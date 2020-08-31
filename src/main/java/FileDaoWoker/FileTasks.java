package FileDaoWoker;

import AppPredicates.AppPredicate;
import FirstComeFirstServerWorker.AnonymousPriorityFileComparator;
import FirstComeFirstServerWorker.FilePriorityComparator;
import ProcessScheduleWorker.IProcessWimsSchedule;
import ProcessScheduleWorker.ProcessWimsSchedule;
import RoundRobinDaoWorker.IRoundRobinProcess;
import RoundRobinDaoWorker.RoundRobinProcess;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FileTasks implements IFileTasks {
    private IRoundRobinProcess assembleSamePriorityRoundRobinSchedule = new RoundRobinProcess();
   IProcessWimsSchedule processWimsSchedule = new ProcessWimsSchedule();

    @Override
    public List<File> fetchFilesFromDirectoryPerMsgId(File directory, int[] multiSegmentsPriorityList) {
        FilePriorityComparator filePriorityComparator = new FilePriorityComparator(multiSegmentsPriorityList);
        List<File> priority1PriorityList = new ArrayList<>();
        try {
            priority1PriorityList.addAll(fetchListOfWimsFilesMessageIdNotSingleSegment(directory,multiSegmentsPriorityList));
            priority1PriorityList.sort(filePriorityComparator);
        }catch (Exception exception){
            exception.printStackTrace();
        }

        return priority1PriorityList;
    }
    @Override
    public void scheduleHigherPriorityOneSegmentFilesPresent(File directory,String movDir, int[] higherPriorityOneSegmentMessageIdArray){
        FilePriorityComparator filePriorityComparator = new FilePriorityComparator(higherPriorityOneSegmentMessageIdArray);
        List<File> updatedOneSegmentFiles = fetchOneSegmentWimsFile(directory,higherPriorityOneSegmentMessageIdArray);
        updatedOneSegmentFiles.sort(filePriorityComparator);
        if(!updatedOneSegmentFiles.isEmpty()){
            for (File aFile:updatedOneSegmentFiles) {
                processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
                //System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT + " The file is " + aFile + ConsoleColors.RESET);
            }
        }
    }
    @Override
    public void scheduleHigherPriorityOneNMultipleSegmentFilesPresent(File directory,String movDir, int[] higherPriorityOneSegmentMessageIdArray, int[] priority1LevelFiles){
        FilePriorityComparator filePriorityComparator = new FilePriorityComparator(higherPriorityOneSegmentMessageIdArray);
        List<File> updatedOneSegmentFiles = fetchOneSegmentWimsFile(directory,higherPriorityOneSegmentMessageIdArray);
        updatedOneSegmentFiles.sort(filePriorityComparator);
        if(!updatedOneSegmentFiles.isEmpty()){
            for (File aFile:updatedOneSegmentFiles) {
                processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
                //System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT + " The file is 1 segment " + aFile + ConsoleColors.RESET);

            }
        }
        FilePriorityComparator filePriorityComparatorPriority1 = new FilePriorityComparator(higherPriorityOneSegmentMessageIdArray);
        List<File> updatedMultipleSegmentPriority1LevelFiles = fetchListOfWimsFilesMessageIdNotSingleSegment(directory,priority1LevelFiles);
        updatedMultipleSegmentPriority1LevelFiles.sort(filePriorityComparatorPriority1);
        if(!updatedMultipleSegmentPriority1LevelFiles.isEmpty()){
            for (File aFile:updatedMultipleSegmentPriority1LevelFiles) {
                processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
               // System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT + " The file is MultipleSegment " + aFile + ConsoleColors.RESET);
            }
        }

    }

    @Override
    public List<File> scheduleOneSegmentFilesPriorityWise(File directory, int[] oneSegmentMessageIdArray) {
        FilePriorityComparator filePriorityComparator = new FilePriorityComparator(oneSegmentMessageIdArray);
        List<File> priorityFileList = new ArrayList<>();
        try {
            priorityFileList = new ArrayList<>(fetchOneSegmentWimsFile(directory, oneSegmentMessageIdArray));
            priorityFileList.sort(filePriorityComparator);
            return  priorityFileList;
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return priorityFileList;
    }


    @Override
    public List<File> fetchAnonymousPriorityFiles(File wimsDirectory, List<Integer> totalMessageSetAssignedPriority){
        List<Integer> totalMessageIdAvailableAtTheInstant  = Arrays.stream(listAllUniqueMsgIdInWimsSystem(wimsDirectory)).boxed().collect(Collectors.toList());
        totalMessageIdAvailableAtTheInstant.stream().distinct().collect(Collectors.toList());
        totalMessageIdAvailableAtTheInstant.removeIf(element->element==-1);
        System.out.print("Message which are present in wims system");
        Set<Integer> messageIdAtTheInstantSet = new HashSet<Integer>(totalMessageIdAvailableAtTheInstant);
        totalMessageIdAvailableAtTheInstant.clear();
        messageIdAtTheInstantSet.forEach(System.out::println);
        messageIdAtTheInstantSet.removeAll(totalMessageSetAssignedPriority);
        totalMessageSetAssignedPriority.clear();
        System.out.println("The all remaining message,which are not assigned priority are" +messageIdAtTheInstantSet);
        int[] remainingPriorityList = messageIdAtTheInstantSet.stream().mapToInt(Number::intValue).toArray();
        List<File> anonymousPriorityFiles = (fetchListOfWimsFilesMessageIdNotSingleSegment(wimsDirectory,remainingPriorityList));
        anonymousPriorityFiles.forEach(System.out::println);
        anonymousPriorityFiles.sort(new AnonymousPriorityFileComparator());
        return anonymousPriorityFiles;
    }


    @Override
    public List<File> fetchListOfWimsFilesMessageIdNotSingleSegment(File wimsDirectory, int[] messageIdArray){
      return   FileUtils.listFiles(wimsDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
                .stream()
                .filter(aFile-> AppPredicate.isIn.test(Integer.parseInt(aFile.getName().split("_")[2]),messageIdArray)
                && !isOneSegmentMessageIdFile(aFile,4,1))
                 .collect(Collectors.toList());
    }
    @Override
    public List<File> fetchOneSegmentWimsFile(File wimsDirectory, int[] oneSegmentMessageIdArray){
        return   FileUtils.listFiles(wimsDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
                .stream()
                .filter(aFile-> AppPredicate.isIn.test(Integer.parseInt(aFile.getName().split("_")[2]),oneSegmentMessageIdArray)
                && isOneSegmentMessageIdFile(aFile,4,1))
                .collect(Collectors.toList());
    }

    private static List<File> fetchMessageIdWimsFiles(int messageId, File wimsDirectory){
        return FileUtils.listFiles(wimsDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
                .stream()
                .filter(theFile-> AppPredicate.isTheFileOfMessageId.test(theFile,messageId))
                .collect(Collectors.toList());
    }
    @Override
    public List<List<File>> yieldSamePrioritySchedule(int[] messageIdArray, File wimsDirectory, HashMap<String, Boolean> reverseHashMap){

        List<File> messageId1FileList = fetchMessageIdWimsFiles(messageIdArray[0],wimsDirectory);
        List<List<File>> messageId1PerSeqFileList =  fetchListOfMessageIDSequenceNumberFile((messageId1FileList));

        List<File> messageId2FileList = fetchMessageIdWimsFiles(messageIdArray[1],wimsDirectory);
        List<List<File>> messageId2PerSeqFileList = (messageIdArray[1]!=1)? fetchListOfMessageIDSequenceNumberFile((messageId2FileList)):Collections.emptyList();

        List<File> messageId3FileList = fetchMessageIdWimsFiles(messageIdArray[2],wimsDirectory);
        List<List<File>> messageId3PerSeqFileList =  (messageIdArray[2]!=-1)?fetchListOfMessageIDSequenceNumberFile((messageId3FileList)):Collections.emptyList();


        List<File> messageId4FileList = fetchMessageIdWimsFiles(messageIdArray[3],wimsDirectory);
        List<List<File>> messageId4PerSeqFileList =  (messageIdArray[3]!=-1)?fetchListOfMessageIDSequenceNumberFile((messageId4FileList)):Collections.emptyList();

        return assembleSamePriorityRoundRobinSchedule.assembleSamePriorityRoundRobinSchedule(messageId1PerSeqFileList,messageId2PerSeqFileList,messageId3PerSeqFileList,messageId4PerSeqFileList,messageIdArray,reverseHashMap);


    }

    private static List<List<File>> fetchListOfMessageIDSequenceNumberFile(List<File> wimsMessageIdFiles){
        List<Integer> indexChangeList = indexArrayElementChange(wimsMessageIdFiles);
        System.out.println(indexChangeList+"   " + wimsMessageIdFiles.size());
        List<List<File>> listOfListOfSec = new ArrayList<List<File>>();
        if(wimsMessageIdFiles.size()>1) {
            int bound = indexChangeList.size() - 1;
            for (int index = 0; index <= bound; index++) {
                if (index == indexChangeList.size() - 1) {
                    listOfListOfSec.add(wimsMessageIdFiles.subList(indexChangeList.get(index), wimsMessageIdFiles.size()));
                } else if (indexChangeList.get(index) + 1 == indexChangeList.get(index + 1)) {
                    listOfListOfSec.add(Collections.singletonList(wimsMessageIdFiles.get(indexChangeList.get(index))));
                } else {
                    listOfListOfSec.add(wimsMessageIdFiles.subList(indexChangeList.get(index), indexChangeList.get(index + 1)));
                }
            }
        }else if (wimsMessageIdFiles.size()==1){

            listOfListOfSec.add(Collections.singletonList(wimsMessageIdFiles.get((0))));
        }
        listOfListOfSec.removeIf(List::isEmpty);

        return listOfListOfSec;
    }
    private static List<Integer> indexArrayElementChange(List<File> fileList){
        String val="";List<Integer> indexOfSequenceNumberAlterList = new ArrayList<Integer>();
        for (int index=0;index<fileList.size();index++){
            if(! ((fileList.get(index).getName().split("_")[2]).concat("_").concat((fileList.get(index).getName().split("_")[3])).equalsIgnoreCase(val))){
                indexOfSequenceNumberAlterList.add(index);
            }
            val=(fileList.get(index).getName().split("_")[2]).concat("_").concat((fileList.get(index).getName().split("_")[3]));
        }
        return indexOfSequenceNumberAlterList;
    }
    @Override
    public List<File> fetchAllFilesFromDirectory(File wimsDirectory){
        return new ArrayList<>(FileUtils.listFiles(wimsDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
    }
    @Override
    public int[] listAllUniqueMsgIdInWimsSystem(File wimsDirectory){
        HashSet<Integer> uniqueMessageIdHashSet = new HashSet<Integer>();
        for (File aMessageIdFile: fetchAllFilesFromDirectory(wimsDirectory)) {
            uniqueMessageIdHashSet.add(Integer.parseInt(aMessageIdFile.getName().split("_")[2]));
        }
        int[] messageIdList = new int[uniqueMessageIdHashSet.size()];
        int i=0;
        for (Integer val:uniqueMessageIdHashSet) {
            messageIdList[i++]=val;
        }
        return  messageIdList;
    }
    @Override
    public   boolean isOneSegmentMessageIdFile(File file, int lineIndexOfSegment, long expectedNumberOfSegments){
      //  if(Integer.parseInt(file.getName().split("_")[2])!=messageId){
       //     return false;
      //  }else {
            LineIterator lineIterator = null;
            try {
                lineIterator=  FileUtils.lineIterator(file, "UTF-8");
                int index=0;
                while (lineIterator.hasNext()){
                    String theInfo= (lineIterator.nextLine());
                    if((index++==lineIndexOfSegment) && (Long.parseLong(theInfo.substring(2),16)==expectedNumberOfSegments)){
                        return true;
                    }
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }finally {
                LineIterator.closeQuietly(lineIterator);
            }
            return  false;
        //}
    }


    @Override
    public void scheduleHigherPriority2NMultipleSegmentFilesPresent(File directory,String movDir, int[] higherPriorityOneSegmentMessageIdArray, int[] priority1LevelFiles, int[] priority2LevelFiles)
    {
        List<File> updatedOneSegmentFiles = fetchOneSegmentWimsFile(directory,higherPriorityOneSegmentMessageIdArray);
        if(!updatedOneSegmentFiles.isEmpty()){
            for (File aFile:updatedOneSegmentFiles) {
                processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
               // System.out.println(ConsoleColors.RED_BOLD_BRIGHT + " The file is 1 segment " + aFile + ConsoleColors.RESET);
            }
        }
        List<File> updatedMultipleSegmentPriority1LevelFiles = fetchListOfWimsFilesMessageIdNotSingleSegment(directory,priority1LevelFiles);
        if(!updatedMultipleSegmentPriority1LevelFiles.isEmpty()){
            for (File aFile:updatedMultipleSegmentPriority1LevelFiles) {
                processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
               // System.out.println(ConsoleColors.CYAN_BOLD_BRIGHT + " The file is MultipleSegment Priority 2" + aFile + ConsoleColors.RESET);
            }
        }

        List<File> updatedMultipleSegmentPriority2LevelFiles = fetchListOfWimsFilesMessageIdNotSingleSegment(directory,priority2LevelFiles);
        if(!updatedMultipleSegmentPriority2LevelFiles.isEmpty()){
            for (File aFile:updatedMultipleSegmentPriority2LevelFiles) {
                processWimsSchedule.consumeNProcessWimsScheduledFiles(aFile,movDir);
              //  System.out.println(ConsoleColors.WHITE_BOLD_BRIGHT + " The file is MultipleSegment Priority 2 " + aFile + ConsoleColors.RESET);
            }
        }
    }
    public List<File> reverseMessageId(HashMap<String, Boolean> reverseMap,List<File> wimsFileList,int[] multiSegmentsPriorityList,String wimsSatellite){
        System.out.println("before reverseing ");
        wimsFileList.forEach(System.out::println);

        System.out.println(Arrays.toString(multiSegmentsPriorityList));
        System.out.println(reverseMap.get(String.valueOf(multiSegmentsPriorityList[0])));
        System.out.println(reverseMap.get(String.valueOf(multiSegmentsPriorityList[1])));
        System.out.println(reverseMap.get(String.valueOf(multiSegmentsPriorityList[2])));
        System.out.println(reverseMap.get(String.valueOf(multiSegmentsPriorityList[3])));
        String satMessageId1Pair = "_"+wimsSatellite+"_"+getKDigits(multiSegmentsPriorityList[0],2),
                satMessageId2Pair ="_"+wimsSatellite+"_"+getKDigits(multiSegmentsPriorityList[1],2),
                satMessageId3Pair ="_"+wimsSatellite+"_"+getKDigits(multiSegmentsPriorityList[2],2),
                satMessageId4Pair ="_"+wimsSatellite+"_"+getKDigits(multiSegmentsPriorityList[3],2);
        if( reverseMap.get(String.valueOf(multiSegmentsPriorityList[0]))){
            int firstIndexMessageId1 = indexOfMessageId(wimsFileList,satMessageId1Pair,"first_index");
            int lastIndexMessageId1 = indexOfMessageId(wimsFileList,satMessageId1Pair,"last_index");
            System.out.println(firstIndexMessageId1 + "_+_" + lastIndexMessageId1 + "__" + satMessageId1Pair);
            wimsFileList = reverseArray(wimsFileList,firstIndexMessageId1,lastIndexMessageId1);
        }
        if(reverseMap.get(String.valueOf(multiSegmentsPriorityList[1]))){
            int firstIndexMessageId2 = indexOfMessageId(wimsFileList,satMessageId2Pair,"first_index");
            int lastIndexMessageId2 = indexOfMessageId(wimsFileList,satMessageId2Pair,"last_index");
            System.out.println(firstIndexMessageId2 + "_*_" + lastIndexMessageId2 + "__" + satMessageId2Pair);
            wimsFileList = reverseArray(wimsFileList,firstIndexMessageId2,lastIndexMessageId2);
        }
        if(reverseMap.get(String.valueOf(multiSegmentsPriorityList[2]))){
            int firstIndexMessageId3 = indexOfMessageId(wimsFileList,satMessageId3Pair,"first_index");
            int lastIndexMessageId3 = indexOfMessageId(wimsFileList,satMessageId3Pair,"last_index");
            wimsFileList = reverseArray(wimsFileList,firstIndexMessageId3,lastIndexMessageId3);
        }if(reverseMap.get(String.valueOf(multiSegmentsPriorityList[3]))){
            int firstIndexMessageId4 = indexOfMessageId(wimsFileList,satMessageId4Pair,"first_index");
            int lastIndexMessageId4 = indexOfMessageId(wimsFileList,satMessageId4Pair,"last_index");
            wimsFileList = reverseArray(wimsFileList,firstIndexMessageId4,lastIndexMessageId4);
        }
        wimsFileList.forEach(System.out::println);
        return wimsFileList;
    }
    private int indexOfMessageId(List<File> fileList, String satMessageIdPair, String indexType){
        List<String> fileNameList = new ArrayList<>();
        for (File file : fileList) {
            fileNameList.add("_" + file.getName().split("_")[1] + "_" + file.getName().split("_")[2]);
        }
        if(indexType.equalsIgnoreCase("first_index")){
            return fileNameList.indexOf(satMessageIdPair);
        }else{
            return fileNameList.lastIndexOf(satMessageIdPair);
        }
    }
    public   String getKDigits(int val,int kDigits){
        int digitsToAdd = kDigits -((int)Math.log10(val) + 1);
        return String.join("", Collections.nCopies(digitsToAdd,"0")) + val;
    }
    List<File> reverseArray(List<File> fileList,int low,int high) {
        for (int from = low, to = high; from <to; from++, to--) {
            Collections.swap(fileList, from, to);
        }
        return fileList;
    }
}
