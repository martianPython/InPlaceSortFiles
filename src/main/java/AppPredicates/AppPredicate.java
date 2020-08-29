package AppPredicates;

import java.io.File;
import java.util.Arrays;
import java.util.function.BiPredicate;

public class AppPredicate {
    public static BiPredicate<Integer,int[] > isIn =(messageId, messageIdArray)->
            Arrays.stream(messageIdArray).anyMatch(i -> i == messageId);

    public static BiPredicate<File,Integer> isTheFileOfMessageId = (theFile, messageId)->
            Integer.parseInt(theFile.getName().split("_")[2])==messageId;
}
