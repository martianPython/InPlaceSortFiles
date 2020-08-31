package ProcessScheduleWorker;

import ColorUiPackage.ConsoleColors;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class FileMovProcessTask {
    public void moveFile(File sourceFile, File destinationFile){
        try {
            FileUtils.moveFile(sourceFile, destinationFile);
            System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + " File Moved " + sourceFile + " --> " + destinationFile + ConsoleColors.RESET);
        } catch (FileExistsException fe){

            if(destinationFile.delete()){
                try {
                    FileUtils.moveFile(sourceFile, destinationFile);
                    System.out.println(ConsoleColors.GREEN_BOLD_BRIGHT + " File Moved " + sourceFile + " --> " + destinationFile + ConsoleColors.RESET);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    sleep.accept((long) 3000);
    }
    private static final Consumer<Long> sleep = (pollingDelay) ->{
        try {
            Thread.sleep(pollingDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

}
