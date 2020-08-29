package RoundRobinDaoWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoundRobinProcess {
    public List<List<File>> assembleSamePriorityRoundRobinSchedule(List<List<File>> messageSeqFileList1, List<List<File>> messageSeqFileList2, List<List<File>> messageSeqFileList3, List<List<File>> messageSeqFileList4, int[] messageIdArray, HashMap<String, Boolean> reverseHashMap)
    {

        List<List<File>> roundRobinFileList = new ArrayList<>();
        for (int i = 0; i < Math.max(Math.max(messageSeqFileList1.size(), messageSeqFileList2.size()),Math.max(messageSeqFileList3.size(),messageSeqFileList4.size())); i++) {
            if(  ! (reverseHashMap.containsKey(String.valueOf(messageIdArray[0]))) ||
                    (  ((reverseHashMap.containsKey(String.valueOf(messageIdArray[0])))) && (!reverseHashMap.get(String.valueOf(messageIdArray[0])))  ) ){
                if (i < messageSeqFileList1.size()) {
                    roundRobinFileList.add(messageSeqFileList1.get(i));
                }
            }else{
                if (i < messageSeqFileList1.size()) roundRobinFileList.add(messageSeqFileList1.get(messageSeqFileList1.size()-1-i));
            }
            if(  ! (reverseHashMap.containsKey(String.valueOf(messageIdArray[1]))) ||
                    (  ((reverseHashMap.containsKey(String.valueOf(messageIdArray[1])))) && (!reverseHashMap.get(String.valueOf(messageIdArray[1])))  ) ){

                if (i < messageSeqFileList2.size()) roundRobinFileList.add(messageSeqFileList2.get(i));
            }else{
                if (i < messageSeqFileList2.size()) roundRobinFileList.add(messageSeqFileList2.get(messageSeqFileList2.size()-1-i));
            }
            if(  ! (reverseHashMap.containsKey(String.valueOf(messageIdArray[2]))) ||
                    (  ((reverseHashMap.containsKey(String.valueOf(messageIdArray[2])))) && (!reverseHashMap.get(String.valueOf(messageIdArray[2])))  ) )
            {
                if (i < messageSeqFileList3.size()) roundRobinFileList.add(messageSeqFileList3.get(i));
            }else{
                if (i < messageSeqFileList3.size()) roundRobinFileList.add(messageSeqFileList3.get(messageSeqFileList3.size()-1-i));
            }
            if(  ! (reverseHashMap.containsKey(String.valueOf(messageIdArray[3]))) ||
                    (  ((reverseHashMap.containsKey(String.valueOf(messageIdArray[3])))) && (!reverseHashMap.get(String.valueOf(messageIdArray[3])))  ) )
            {    if (i < messageSeqFileList4.size()) roundRobinFileList.add(messageSeqFileList4.get(i));
            }else{
                if (i < messageSeqFileList4.size()) roundRobinFileList.add(messageSeqFileList4.get(messageSeqFileList4.size()-1-i));
            }
        }
        return roundRobinFileList;
    }
}
