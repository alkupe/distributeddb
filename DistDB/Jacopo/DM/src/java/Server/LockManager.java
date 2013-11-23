/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author alexh
 */
public class LockManager {
    public enum LockType {
        READ,WRITE
    }
    public class LockData {
        public LockType type;
        public int count;
    }
    
    private Map<String,LockData> locks;
    private Map<String,ArrayList<String>> transLocks;
    
    LockManager() {
        locks = new HashMap<>();
        transLocks = new HashMap<>();
    }
    
    public boolean getLock(String var,String trans,LockType type) {
        LockData l = locks.get(var);
        if (l == null)  {
            l = new LockData();
            l.count = 1;
            l.type = type;
            putLockinTransLock(var,trans);
            locks.put(var, l);
            return true;
        } else if (l.type == LockType.READ && type == LockType.READ) {
            l.count++;
            putLockinTransLock(var,trans);
            return true;
        };
        return false;
    }
    
    private void putLockinTransLock(String var, String trans) {
        ArrayList<String> exists = transLocks.get(trans);
        if(exists == null) {
            exists = new ArrayList<>();
        }
        exists.add(var);
        transLocks.put(trans,exists);
    }
    
    public void giveUpLocks(String trans) {
        ArrayList<String> exists = transLocks.get(trans);
        if(exists == null) {
            return;
        }
        for (String var : exists) {
            LockData l = locks.get(var);
            if (l != null) {
                if (l.type == LockType.WRITE) {
                    locks.remove(var);
                } else {
                    if (l.count == 1) {
                        locks.remove(var);
                    } else {
                        l.count--;
                    }
                }
            }
        }
    }
}
