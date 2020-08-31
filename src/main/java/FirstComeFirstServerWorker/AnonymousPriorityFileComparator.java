package FirstComeFirstServerWorker;

import java.io.File;
import java.util.Comparator;

public class AnonymousPriorityFileComparator implements Comparator<File> {
    @Override
    public int compare(File fileA, File fileB) {
        if(fileA.getName().split("_")[2].equalsIgnoreCase(fileB.getName().split("_")[2])){
            return fileA.lastModified()>fileB.lastModified()?1:-1;
        }else{
           return 0;
        }


    }
}
