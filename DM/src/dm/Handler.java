/*
 * Handler Class handles all interaction with the client
 * via XMLRPC library
 *
 */
package dm;

import java.util.*;

public class Handler {

    //everything must be static!
    public static Map<String, Variable> _variables = new HashMap<String, Variable>();
    public static Map<String, Map<String, Variable>> _varCopies = new HashMap<String, Map<String, Variable>>();
    public static List<String> _ordering = new ArrayList<>();
    public static LockManager _lm = new LockManager();
    public static boolean _isFailed = false;
    public static Map<String, Integer> _firstAccessed = new HashMap<>();
    public static Map<String, List<Write>> _transWrites = new HashMap<>();
    public static int up_since = 0;

    /**
     *
     * @param var A map used to contain the variable
     * @param time Time that the DM is initialized
     * @return "ok"
     * @author Alex Halter and Jacopo Cirrone Initializes the variable in the
     * database
     */
    public String initialize(HashMap<String, String> var, int time) {
        Variable v = new Variable(var.get("name"), var.get("value"), var.get("replicated").equals("true") ? true : false);
        v.setRecovered(var.get("recovered") == null ? false : true);
        _variables.put(var.get("name"), v);
        _ordering.add(var.get("name"));
        up_since = time;
        return "ok";
    }

    /**
     *
     * @param trans transaction name
     * @return "ok"
     *
     * Used to register a new RO transaction with the DM, creating a copy of all
     * the current committed values.
     *
     */
    public String registerRO(String trans) {
        Map<String, Variable> copy = new HashMap<>();
        for (Map.Entry<String, Variable> entry : _variables.entrySet()) {
            try {
                copy.put(entry.getKey(), (Variable) entry.getValue().clone());
            } catch (CloneNotSupportedException e) {

            }
        }
        _varCopies.put(trans, copy);

        return "ok";
    }

    /**
     *
     * @param trans transaction name
     * @param var variable name
     * @param time current time
     * @return either the value, "failed" if the site is failed, or "no" if the
     * site cannot get the lock or its not recovered.
     * @author Alex Halter
     */
    public String readVar(String trans, String var, Integer time) {
        Variable v = _variables.get(var);
        if (_isFailed) {
            return "failed";
        } else if ((!v.isReplicated() || v.isRecovered()) && _lm.getLock(var, trans, LockManager.LockType.READ)) {
            if (_lm.getLockType(var) == LockManager.LockType.WRITE) {
                String ret = null;
                List<Write> writes = _transWrites.get(trans);
                for (Write w : writes) {
                    if (w.var.equals(var)) {
                        ret = String.valueOf(w.val);
                    }
                }
                return ret;
            }
            checkTime(trans, time);
            return v.getValue();
        }
        return "no";
    }

    /**
     *
     * @param trans transaction name
     * @param var variable name
     * @return either the value, "failed" if the site is failed, or "no" if the
     * site cannot get the lock or its not recovered.
     * @author Alex Halter
     */
    public String readOnlyVar(String trans, String var) {
        if (_isFailed) {
            return "failed";
        }
        Map<String, Variable> copy = _varCopies.get(trans);
        if (copy == null) {
            return "no";
        }
        Variable v = copy.get(var);
        if (!v.isReplicated() || v.isRecovered()) {
            return v.getValue();
        }
        return "no";
    }

    /**
     *
     * @return List of all the variables in the DB and their current values.
     * Transported back to TM as hashmaps.
     * @author Alex Halter
     */
    public List<Map<String, String>> dump() {
        ArrayList<Map<String, String>> vars = new ArrayList<Map<String, String>>();
        if (!_isFailed) {
            for (String vname : _ordering) {
                Variable v = _variables.get(vname);
                Map<String, String> var = new HashMap<>();
                var.put("name", v.getName());
                var.put("value", v.getValue());
                var.put("availability", v.isReplicated() ? (v.isRecovered() ? "available: recovered":"not available: not recovered") :"available: not replicated");
                vars.add(var);
            }
        }
        return vars;
    }

    /**
     *
     * @param id
     * @return "ok" Used for debugging the DM state.
     */
    public String query(String id) {
        System.out.println("***DM " + id);
        _lm.printAll();
        this.printVariables();
        //add any other debug output here
        return "ok";
    }

    /**
     *
     * @param trans transaction name
     * @param var variable name
     * @param val new value
     * @param time current time
     * @return either the value, "failed" if the site is failed, or "no" if the
     * site cannot get the lock or its not recovered.
     *
     * @author Alex Halter and Jacopo Cirrone
     *
     *
     */
    public String writeVar(String trans, String var, Integer val, Integer time) {
        Variable v = _variables.get(var);
        if (_isFailed) {

            return "failed";
        }
        if (_lm.getLock(var, trans, LockManager.LockType.WRITE)) {
            checkTime(trans, time);
            List<Write> l = _transWrites.get(trans);
            Write w = new Write();
            w.val = val;
            w.var = var;
            if (l == null) {
                l = new ArrayList<>();
                _transWrites.put(trans, l);
            }
            l.add(w);
            return "yes";
        }
        return "no";
    }

    /**
     *
     * @param trans transaction name
     * @param time current time Used by each incoming read and write to insert
     * the current transaction and time in a _firstAccessed table. If its the
     * first time accessing, we keep to do 2PC on commit
     * @author Alex Halter and Jacopo Cirrone
     */
    private void checkTime(String trans, Integer time) {
        Integer i = _firstAccessed.get(trans);
        if (i == null) {
            _firstAccessed.put(trans, time);
        }
    }

    /**
     * Sets the DM to failed. Resets the lock state.
     *
     * @return "ok"
     *
     * @author Jacopo Cirrone
     */
    public String fail() {
        _isFailed = true;
        _lm = new LockManager();
        _transWrites = new HashMap<>();
        return "ok";
    }

    /**
     *
     * @param time current time
     * @return "ok" DM is set to recovered.
     * @author Jacopo Cirrone
     */
    public String recover(int time) {
        Variable v;
        for (Map.Entry<String, Variable> entry : _variables.entrySet()) {
            v = (Variable) entry.getValue();
            v.setRecovered(false);
        }
        up_since = time;
        _isFailed = false;
        return "ok";
    }

    /**
     * The first phase of 2PC
     *
     * @param trans current transaction
     * @return "ok" if this DM votes yes for commit, "failed" if the DM is
     * failed, and "no" if it votes no
     * @author Jacopo Cirrone
     *
     */
    public String endready(String trans) {
        if (_isFailed) {
            return "failed";
        }
        Integer first_time_access = _firstAccessed.get(trans);
        if (first_time_access == null || first_time_access >= up_since) {
            return "ok";
        } else {
            return "no";
        }
    }

    /**
     * Transaction successfully commits
     *
     * @param trans
     * @return "ok" if it was successful, otherwise returns "failed"
     * @author Jacopo Cirrone
     */
    public String end(String trans) {
        if (_isFailed) {
            return "failed";
        }
        if (_transWrites.containsKey(trans)) {
            List<Write> l = _transWrites.get(trans);
            for (int i = 0; l != null && i < l.size(); i++) {
                Write w = l.get(i);
                Variable v = _variables.get(w.var);
                v.setValue(String.valueOf(w.val));
                if (v.isReplicated()) {
                    v.setRecovered(true);
                }
                _variables.put(v.getName(), v);

            }
        }
        abort_internal_method(trans);
        //System.out.println("Transaction "+trans+" committed at this site, which is up since "+up_since);
        return "ok";
    }

    /**
     * RO transactions do not need to participate in 2PC.
     *
     * @param trans
     * @return "ok" (RO transactions always successfully commit)
     * @author Jacopo Cirrone
     */
    public String endRO(String trans) {
        if (_firstAccessed.containsKey(trans)) {
            _firstAccessed.remove(trans);
        }

        _varCopies.remove(trans);
        return "ok";
    }

    /**
     * Called by TM to do wait-die. Returns the current lock holders for a given
     * variable.
     *
     * @param var
     * @return list of current lock holders
     * @auther Alex Halter
     */
    public List<String> getLockHolders(String var) {
        List<String> lockHolders;
        lockHolders = _lm.getLockHolders(var);
        return lockHolders;
    }

    /**
     * Aborts a transaction, giving up all locks and cleaning up
     *
     * @param trans
     * @return "ok"
     * @author Jacopo Cirrone
     */
    public String abort(String trans) {
        if (_isFailed) {
            return "failed";
        }
        abort_internal_method(trans);
        //System.out.println("Transaction "+trans+" aborted at this site,which is up since "+up_since);
        return "ok";
    }

    /**
     * Internal helper to clean up for an aborting or committing transaction
     *
     * @param trans
     * @author Jacopo Cirrone
     */
    private void abort_internal_method(String trans) {
        if (_firstAccessed.containsKey(trans)) {
            _firstAccessed.remove(trans);
        }
        _lm.giveUpLocks(trans);
        if (_transWrites.containsKey(trans)) {
            _transWrites.remove(trans);
        }
    }

    private void printVariables() {
        System.out.println("Distribution of variables values");
        for (Map.Entry<String, Variable> entry : _variables.entrySet()) {
            Variable v = (Variable) entry.getValue();
            System.out.println(v.getName() + " " + v.getValue());
        }
    }
}
