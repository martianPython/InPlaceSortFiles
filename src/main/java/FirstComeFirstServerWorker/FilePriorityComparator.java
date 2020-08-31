package FirstComeFirstServerWorker;

import java.io.File;
import java.util.Comparator;
/**
 * <b>Ordering of PriorityQueue</b>
 * The Class provides feature  to impose  a total ordering on the Priority Queue of WIMS Files .
 * It helps exercise precise control over the File Ordering based on the priority of the WIMS Message-ID as provided in the configuration file.
 * It's feature shall be exploited by the user to strictly arrange the Priority Queue .
 * @author Koushik Nag
 * @version 1.0
 * @since  3-07-2020
 */
public class FilePriorityComparator implements Comparator<File> {
    private int[] messageIdArray ;
    public FilePriorityComparator(int[] messageIdArray){
        this.messageIdArray = messageIdArray;
    }

    /**
     * The method shall be exercise ordering on the Priority Queue consists of the WIMS Files.
     * @param fileA One file, which is to be arranged as per priority provided in the Configuration File.
     * @param fileB Another file, which is to be arranged as per priority provided in the Configuration File.
     * @return value post comparison on the files.
     */
    @Override
    public int compare(File fileA, File fileB) {
        int fileAMessageId= Integer.parseInt(fileA.getName().split("_")[2]);
        int fileBMessageId= Integer.parseInt(fileB.getName().split("_")[2]);
        int fileASeqNum = Integer.parseInt(fileA.getName().split("_")[3]);
        int fileBSeqNum = Integer.parseInt(fileB.getName().split("_")[3]);
        if(fileAMessageId==fileBMessageId){
           // if(fileASeqNum==fileBSeqNum){
                return fileA.lastModified()<fileB.lastModified()?-1:1;
           // }

        }
        if(fileAMessageId==messageIdArray[0]){
            return -1;
        }
        if(fileBMessageId==messageIdArray[0]){
            return 1;
        }
        if(fileAMessageId==messageIdArray[1]){
            return -1;
        }
        if(fileBMessageId==messageIdArray[1]){
            return 1;
        }
        if(fileAMessageId==messageIdArray[2]){
            return -1;
        }
        if(fileBMessageId==messageIdArray[2]){
            return 1;
        }
        if(fileAMessageId==messageIdArray[3]){
            return -1;
        }
        if(fileBMessageId==messageIdArray[3]){
            return 1;
        }
        return 0;
    }
}
