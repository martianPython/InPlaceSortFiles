package ProcessScheduleWorker;

import java.io.File;
import java.util.function.Consumer;

public interface IProcessWimsSchedule {
    void consumeNProcessWimsScheduledFiles(File aFIle,String movDir);
}
