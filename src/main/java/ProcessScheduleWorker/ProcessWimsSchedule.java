package ProcessScheduleWorker;

import java.io.File;

public class ProcessWimsSchedule implements IProcessWimsSchedule {
private FileMovProcessTask fileMovProcessTask = new FileMovProcessTask();
    @Override
    public void consumeNProcessWimsScheduledFiles(File aFIle,String movDir) {
        fileMovProcessTask.moveFile(aFIle,new File(movDir + aFIle.getName()));
    }
}
