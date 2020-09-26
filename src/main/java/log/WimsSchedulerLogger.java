package log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class WimsSchedulerLogger {
    public void logSchedulerJob(String logFileName, String schedulerStatus) {
        File file = new File(logFileName);

        try {
            FileUtils.writeStringToFile(file, schedulerStatus + "\n", true);
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }
}
