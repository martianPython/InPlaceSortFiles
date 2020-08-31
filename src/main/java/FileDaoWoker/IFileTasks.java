package FileDaoWoker;

import AppPredicates.AppPredicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public interface IFileTasks {


    List<File> fetchFilesFromDirectoryPerMsgId(File directory, int[] multiSegmentsPriorityList);

    void scheduleHigherPriorityOneSegmentFilesPresent(File directory,String movDir, int[] higherPriorityOneSegmentMessageIdArray);

    void scheduleHigherPriorityOneNMultipleSegmentFilesPresent(File directory,String movDir, int[] higherPriorityOneSegmentMessageIdArray, int[] priority1LevelFiles);

    List<File> scheduleOneSegmentFilesPriorityWise(File directory, int[] oneSegmentMessageIdArray);

    List<File> fetchAnonymousPriorityFiles(File wimsDirectory, List<Integer> totalMessageSetAssignedPriority);

    List<File> fetchListOfWimsFilesMessageIdNotSingleSegment(File wimsDirectory, int[] messageIdArray);

    List<File> fetchOneSegmentWimsFile(File wimsDirectory, int[] oneSegmentMessageIdArray);

    List<List<File>> yieldSamePrioritySchedule(int[] messageIdArray, File wimsDirectory, HashMap<String, Boolean> reverseHashMap);

    List<File> fetchAllFilesFromDirectory(File wimsDirectory);

    int[] listAllUniqueMsgIdInWimsSystem(File wimsDirectory);

    boolean isOneSegmentMessageIdFile(File file, int lineIndexOfSegment, long expectedNumberOfSegments);

    void scheduleHigherPriority2NMultipleSegmentFilesPresent(File directory,String movDir, int[] higherPriorityOneSegmentMessageIdArray, int[] priority1LevelFiles, int[] priority2LevelFiles);
}
