package MainPack;

import FileDaoWoker.FileTasks;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class AppMain {
    public static void main(String[] args) {
        boolean firstComeFirstServe =false;
    String dir = "/home/koushik/Desktop/WIMS/1A";
    int[] multiSegmentsPriority1List={21,72,20,5};
        HashMap<String, Boolean> reverseHashMap = new HashMap<>();
        reverseHashMap.put("21",false);reverseHashMap.put("41",false);reverseHashMap.put("20",false);reverseHashMap.put("5",true);
        FileTasks fileTasks = new FileTasks(multiSegmentsPriority1List);
    if(firstComeFirstServe){
        PriorityQueue<File>  filesPriorityQueue = fileTasks.fetchFilesFromDirectory(new File(dir));
        while (!filesPriorityQueue.isEmpty()){
            System.out.println(filesPriorityQueue.poll());
        }
    }else{
        //  Round Robin Processing to be executed here
        List<List<File>> listList = fileTasks.yieldSamePrioritySchedule(multiSegmentsPriority1List,new File(dir),reverseHashMap);
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
