package RoundRobinDaoWorker;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public interface IRoundRobinProcess {
    List<List<File>> assembleSamePriorityRoundRobinSchedule(List<List<File>> messageSeqFileList1, List<List<File>> messageSeqFileList2, List<List<File>> messageSeqFileList3, List<List<File>> messageSeqFileList4, int[] messageIdArray, HashMap<String, Boolean> reverseHashMap);
}
