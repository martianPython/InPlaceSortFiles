package FileDaoWoker;

import AppPredicates.AppPredicate;
import PriorityQueueComparator.AnonymousPriorityFileComparator;
import PriorityQueueComparator.FilePriorityComparator;
import RoundRobinDaoWorker.RoundRobinProcess;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FileTasks {
    RoundRobinProcess assembleSamePriorityRoundRobinSchedule = new RoundRobinProcess();


    public PriorityQueue<File> fetchFilesFromDirectoryPerMsgId(File directory,int[] multiSegmentsPriorityList) {
        FilePriorityComparator filePriorityComparator = new FilePriorityComparator(multiSegmentsPriorityList);
        PriorityQueue<File> priorityQueue = new PriorityQueue<>(filePriorityComparator);
        try {
        priorityQueue.addAll(fetchListOfWimsFilesMessageId(directory,multiSegmentsPriorityList));
        return  priorityQueue;
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return priorityQueue;
    }
    public List<File> fetchAnonymousPriorityFiles(File wimsDirectory,List<Integer> totalMessageSetAssignedPriority ){
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
        List<File> anonymousPriorityFiles = (fetchListOfWimsFilesMessageId(wimsDirectory,remainingPriorityList));
        anonymousPriorityFiles.sort(new AnonymousPriorityFileComparator());
        return anonymousPriorityFiles;
    }


    public List<File> fetchListOfWimsFilesMessageId (File wimsDirectory,int[] messageIdArray){
      return   FileUtils.listFiles(wimsDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
                .stream()
                .filter(aFile-> AppPredicate.isIn.test(Integer.parseInt(aFile.getName().split("_")[2]),messageIdArray))
                 .collect(Collectors.toList());
    }


    public static List<File> fetchMessageIdWimsFiles(int messageId,File wimsDirectory){
        return FileUtils.listFiles(wimsDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
                .stream()
                .filter(theFile->AppPredicate.isTheFileOfMessageId.test(theFile,messageId))
                .collect(Collectors.toList());
    }



    public List<List<File>> yieldSamePrioritySchedule(int[] messageIdArray, File wimsDirectory, HashMap<String, Boolean> reverseHashMap){

        List<File> messageId1FileList = fetchMessageIdWimsFiles(messageIdArray[0],wimsDirectory);
        List<List<File>> messageId1PerSeqFileList =  fetchListOfMessageIDSequenceNumberFile((messageId1FileList));

        List<File> messageId2FileList = fetchMessageIdWimsFiles(messageIdArray[1],wimsDirectory);
        List<List<File>> messageId2PerSeqFileList = (messageIdArray[1]!=1)? fetchListOfMessageIDSequenceNumberFile((messageId2FileList)):Collections.emptyList();

        List<File> messageId3FileList = fetchMessageIdWimsFiles(messageIdArray[2],wimsDirectory);
        List<List<File>> messageId3PerSeqFileList =  (messageIdArray[2]!=-1)?fetchListOfMessageIDSequenceNumberFile((messageId3FileList)):Collections.emptyList();


        List<File> messageId4FileList = fetchMessageIdWimsFiles(messageIdArray[3],wimsDirectory);
        List<List<File>> messageId4PerSeqFileList =  (messageIdArray[3]!=-1)?fetchListOfMessageIDSequenceNumberFile((messageId4FileList)):Collections.emptyList();

        return assembleSamePriorityRoundRobinSchedule.
       assembleSamePriorityRoundRobinSchedule(messageId1PerSeqFileList,messageId2PerSeqFileList,messageId3PerSeqFileList,messageId4PerSeqFileList,messageIdArray,reverseHashMap);


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
    public List<File> fetchAllFilesFromDirectory(File wimsDirectory){
        return new ArrayList<>(FileUtils.listFiles(wimsDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
    }
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



}
