package MainPack;

import FileDaoWoker.FileTasks;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class AppMain {
    public static void main(String[] args) {
        boolean firstComeFirstServe =true;
    File  dir = new File("/home/koushik/Desktop/WIMS/1A");
    int[] multiSegmentsPriority1List={21,72,20,-1};int[] multiSegmentsPriority2List={18,17,-1,-1};
        HashMap<String, Boolean> reverseHashMap = new HashMap<>();
        reverseHashMap.put("21",false);reverseHashMap.put("41",false);reverseHashMap.put("20",false);reverseHashMap.put("5",true);
        FileTasks fileTasks = new FileTasks();
    if(firstComeFirstServe){
        PriorityQueue<File>  filesPriorityQueue = fileTasks.fetchFilesFromDirectoryPerMsgId(dir,multiSegmentsPriority1List);
        while (!filesPriorityQueue.isEmpty()){
            System.out.println(filesPriorityQueue.poll());
        }
        List<Integer> totalMessageSetAssignedPriority = Arrays.stream(multiSegmentsPriority1List).boxed().collect(Collectors.toList());
        totalMessageSetAssignedPriority.addAll(Arrays.stream(multiSegmentsPriority2List).boxed().collect(Collectors.toList()));
        List<File> anonymousPriorityFiles = fileTasks.fetchAnonymousPriorityFiles(dir,totalMessageSetAssignedPriority);
        for (File aFile:anonymousPriorityFiles) {
            System.out.println(aFile);
        }
    }else{
        //  Round Robin Processing to be executed here
        List<List<File>> listList = fileTasks.yieldSamePrioritySchedule(multiSegmentsPriority1List,(dir),reverseHashMap);
        listList.removeIf(List::isEmpty);
        for (List<File> fileList: listList ) {
            for (File aFile: fileList) {
                System.out.println(aFile);
            }
            System.out.println();
        }

    }




    }

}
