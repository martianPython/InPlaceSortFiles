package ProcessScheduleWorker;

import java.io.File;

public interface IProcessWimsSchedule {
    void consumeNProcessWimsScheduledFiles(File aFIle,String movDir,File archiveDirectory,String satelliteId,long copyPollingDelay);
}
