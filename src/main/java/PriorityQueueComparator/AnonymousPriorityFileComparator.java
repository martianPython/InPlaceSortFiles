package PriorityQueueComparator;

import java.io.File;
import java.util.Comparator;

public class AnonymousPriorityFileComparator implements Comparator<File> {
    @Override
    public int compare(File fileA, File fileB) {
        if(fileA.getName().equalsIgnoreCase(fileB.getName())){
            return 0;
        }else{
            return fileA.lastModified()>fileB.lastModified()?1:-1;
        }


    }
}
