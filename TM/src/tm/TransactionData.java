/**
 * Data associated with a transaction
 *
 * @author Alex Halter and Jacopo Cirrone
 *
 */
package tm;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TransactionData {

    public boolean isRO = false;
    public int startTime;
    public String name;
    //integer array list (correct indexing, not off by one)
    public Set<Integer> accessed = new HashSet<>();// list to check for 2PC
    public Command waiting;
}
