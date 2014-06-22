/**
 * Lock Manager Class Nice wrapper around basic read/write locks
 *
 * @author Alex Halter and Jacopo Cirrone
 *
 */
package dm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LockManager {

    public enum LockType {

        READ, WRITE
    }

    /**
     * Data for each Lock: type and who holds the lock
     */
    public class LockData {

        public LockType type;
        public List<String> holders;
    }

    //locks by variable
    private Map<String, LockData> locks;

    //locks by transaction
    private Map<String, List<String>> transLocks;

    LockManager() {
        locks = new HashMap<>();
        transLocks = new HashMap<>();
    }

    /**
     * get all lockholders for a variable
     *
     * @param var the variable to look up
     * @return a list of lock holders
     * @author Alex Halter
     */
    public List<String> getLockHolders(String var) {
        return locks.get(var).holders;
    }

    /**
     * Request a lock, if it can grant the lock, it grants the lock and returns
     * true otherwise returns false with no side effect
     *
     * @param var the variable to lock
     * @param trans the transaction doing the locking
     * @param type the type of lock
     * @return true if the lock is granted
     * @author Alex Halter and Jacopo Cirrone
     */
    public boolean getLock(String var, String trans, LockType type) {
        LockData l = locks.get(var);

        //grant the lock if there is no lock yet
        if (l == null) {
            l = new LockData();
            l.holders = new ArrayList<>();
            l.holders.add(trans);
            l.type = type;
            putLockinTransLock(var, trans);
            locks.put(var, l);
            return true;
            //grant the lock if its a read request and lock type is read
        } else if (l.type == LockType.READ && type == LockType.READ) {
            l.holders.add(trans);
            putLockinTransLock(var, trans);
            return true;
            //grant the lock if the current lock holder is making the request
            // and either its a write lock (exclusive)
            // or its a read lock and the requestor is the only holder of the lock (lock upgrade)
        } else if (((l.type == LockType.WRITE) || (l.holders.size() == 1)) && l.holders.get(0).equals(trans)) {
            l.type = LockType.WRITE;
            return true;
        }
        return false;
    }

    /**
     * get the lock type of a variable
     *
     * @param var the variable to look up
     * @return the lock type or null if it isn't locked
     */
    public LockType getLockType(String var) {
        LockData l = locks.get(var);
        return l == null ? null : l.type;
    }

    /**
     * Internal helper to maintain a data structure of locks by transaction so
     * that when a transaction ends, it can give up its locks easily
     *
     * @param var the variable to lock
     * @param trans the transaction doing the locking
     * @author Alex Halter
     */
    private void putLockinTransLock(String var, String trans) {
        List<String> exists = transLocks.get(trans);
        if (exists == null) {
            exists = new ArrayList<>();
            transLocks.put(trans, exists);
        }
        exists.add(var);

    }

    /**
     * for internal debugging, prints out the current lock state
     */
    public void printAll() {

        System.out.println("All locks:");
        System.out.println("Locks by Variable");
        for (String var : locks.keySet()) {
            LockData ld = locks.get(var);
            System.out.println(var + " " + ld.type.toString() + " " + ld.holders.size());
        }

        System.out.println("Transactions holding locks");
        for (String tran : transLocks.keySet()) {
            System.out.println("Transaction: " + tran + " holds locks:");
            List<String> vars = transLocks.get(tran);
            for (String var : vars) {
                System.out.println("Var : " + var);
            }
        }
    }

    /**
     * Gives up the locks for a transaction when it ends
     *
     * @param trans the transaction giving up locks
     */
    public void giveUpLocks(String trans) {
        if (transLocks.containsKey(trans)) {
            //get the locks the transaction has
            List<String> exists = transLocks.get(trans);
            if (exists == null) {
                return;
            }

            //go through the locks and remove this transaction from the lock holders
            //delete the lock if it is the only lock holder
            for (String var : exists) {
                LockData l = locks.get(var);
                if (l != null) {
                    if (l.type == LockType.WRITE) {
                        locks.remove(var);
                    } else {
                        if (l.holders.size() == 1) {
                            locks.remove(var);
                        } else {
                            l.holders.remove(trans);
                        }
                    }
                }
            }
            transLocks.remove(trans);
        }
    }
}
