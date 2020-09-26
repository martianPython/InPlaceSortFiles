package ProcessScheduleWorker;

import java.io.File;
import java.time.LocalDateTime;

public class ProcessWimsSchedule implements IProcessWimsSchedule {
private final FileMovProcessTask fileMovProcessTask = new FileMovProcessTask();
    private String logFolder = "/opt/wims/data/";
    @Override
    public void consumeNProcessWimsScheduledFiles(File aFIle,String movDir,File archiveDirectory,String satelliteId,long copyPollingDelay) {
       StringBuilder logFileName = (new StringBuilder(logFolder).append(satelliteId).append("/Schedule_").append(LocalDateTime.now().getDayOfYear()).append("_").append(LocalDateTime.now().getYear()).append("log.txt"));
        String newFileName = aFIle.getName().split("_")[0] + "_"+satelliteId + "_" + aFIle.getName().split("_")[2] + "_" + aFIle.getName().split("_")[3]
                + "_" + aFIle.getName().split("_")[4];
       // File copyFile = new File(aFIle.getAbsolutePath()+newFileName);
       // newFileName
        fileMovProcessTask.copyFileToDir(aFIle,new File(movDir + newFileName),copyPollingDelay,logFileName.toString());
        fileMovProcessTask.moveFile(aFIle,new File(archiveDirectory+"/"+aFIle.getName()),logFileName.toString());


//        System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + "Moved the file " +aFIle + "--> " + movDir + aFIle.getName());
    }
}
