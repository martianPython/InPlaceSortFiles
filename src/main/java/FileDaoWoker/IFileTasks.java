package FileDaoWoker;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public interface IFileTasks {


    List<File> fetchFilesFromDirectoryPerMsgId(File directory, int[] multiSegmentsPriorityList,String satelliteId);

    void scheduleHigherPriorityOneSegmentFilesPresent(File directory,String movDir, File archiveDirectory,String satelliteId, int[] higherPriorityOneSegmentMessageIdArray,long copyPollingDelay);

//    void scheduleHigherPriorityOneNMultipleSegmentFilesPresent(File directory,String movDir, File archiveDirectory,String satelliteId, int[] higherPriorityOneSegmentMessageIdArray, int[] priority1LevelFiles);

    List<File> fetchOneSegmentFilesPriorityWise(File directory, int[] oneSegmentMessageIdArray,String satelliteId);

  //  List<File> fetchAnonymousPriorityFiles(File wimsDirectory, List<Integer> totalMessageSetAssignedPriority);

    List<File> fetchListOfWimsFilesMessageIdNotSingleSegment(File wimsDirectory, int[] messageIdArray,String satelliteId);

    List<File> fetchOneSegmentWimsFile(File wimsDirectory, int[] oneSegmentMessageIdArray,String satelliteId);

    List<List<File>> yieldSamePrioritySchedule(int[] messageIdArray, File wimsDirectory, HashMap<String, Boolean> reverseHashMap,String satelliteId);

    List<File> fetchAllFilesFromDirectory(File wimsDirectory);

   // int[] listAllUniqueMsgIdInWimsSystem(File wimsDirectory);

    boolean isOneSegmentMessageIdFile(File file, int lineIndexOfSegment, long expectedNumberOfSegments);

  //  void scheduleHigherPriority2NMultipleSegmentFilesPresent(File directory,String movDir, File archiveDirectory,String satelliteId, int[] higherPriorityOneSegmentMessageIdArray, int[] priority1LevelFiles, int[] priority2LevelFiles);
}
