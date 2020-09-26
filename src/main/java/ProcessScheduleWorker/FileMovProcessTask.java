package ProcessScheduleWorker;

import ColorUiPackage.ConsoleColors;
import log.WimsSchedulerLogger;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class FileMovProcessTask {
    private WimsSchedulerLogger wimsSchedulerLogger = new WimsSchedulerLogger();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
    public void moveFile(File sourceFile, File archiveFile,String schedulerLog){

        try {
            FileUtils.moveFile(sourceFile, archiveFile);
            wimsSchedulerLogger.logSchedulerJob(schedulerLog," File Moved " + sourceFile + " --> " + archiveFile + "  [" +dateFormatter.format(new Date() ) + "] ");
            System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + " File Moved " + sourceFile + " --> " + archiveFile + ConsoleColors.RESET);
        } catch (FileExistsException fe){
            if(archiveFile.delete()){
                try {
                    FileUtils.moveFile(sourceFile, archiveFile);
                    wimsSchedulerLogger.logSchedulerJob(schedulerLog," File  Deleted and Moved " + sourceFile + " --> " + archiveFile + "  [" +dateFormatter.format(new Date() ) + "] ");
                    System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + " File  Deleted and Moved " + sourceFile + " --> " + archiveFile + ConsoleColors.RESET);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    public void copyFileToDir(File sourceFile,File destinationFile,long copyPollingDelay,String schedulerLog){
        sleep.accept( copyPollingDelay);
        try {
            FileUtils.copyFile(sourceFile,destinationFile);
            wimsSchedulerLogger.logSchedulerJob(schedulerLog,"Copied the file " +sourceFile + "--> " + destinationFile + "  [" +dateFormatter.format(new Date() ) + "] ");
            System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + "Copied the file " +sourceFile + "--> " + destinationFile + ConsoleColors.RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final Consumer<Long> sleep = (pollingDelay) ->{
        try {
            Thread.sleep(pollingDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

}
