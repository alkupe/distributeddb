/**
 * Transaction Manager class Opens client connection to all the DMs Then blocks
 * on standard input, parses commands and dispatches to the DMs.
 *
 * @author Alex Halter and Jacopo Cirrone
 */
package tm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class TransactionManager {

    List<Integer> _ports;
    List<XmlRpcClient> _clients;
    int _countDMs;
    static final int COUNT_VARS = 20;
    private int currTime;
    private List<String> _toDelete;
    private Map<String, TransactionData> _transactions;
    private Map<String, List<Integer>> _varLocations;
    private Map<String, Command.commandType> _waitingWrites;
    int commandCount = 0;

    /**
     *
     * @param ports a list of all the ports the DMs are running on. Initiates
     * client to connect to all the DMs.
     * @author Alex Halter and Jacopo Cirrone
     */
    TransactionManager(List<Integer> ports) {
        //number of DMs is configurable, based on number of ports given
        _countDMs = ports.size();
        _ports = ports;
        System.out.println("Starting TM with " + _countDMs + " DMs");
        //master list of the DM client connections
        //everywhere, we will refer to a DM by its index in _clients
        //dm1 aka 1 is _clients.get(0);
        //don't forget its off by one
        _clients = new ArrayList<>();

        //list of current running transactions, with associated TransactionData
        _transactions = new HashMap<>();

        //locations (DMs) of each variable
        _varLocations = new HashMap<>();

        //during wait-die, keep a list of aborting ransactions
        _toDelete = new ArrayList<>();

        //A list of waiting writes, to ensure no live lock on reads
        _waitingWrites = new HashMap<>();

        for (int port : _ports) {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setEnabledForExceptions(true);
            String serverURL = "http://localhost:" + port;
            try {
                config.setServerURL(new URL(serverURL));
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);
            _clients.add(client);
        }
    }

    /**
     * Gives each DM the variable and values it should hold. Stores the
     * locations of each variable
     *
     * @author Jacopo Cirrone
     */
    public void setUpDMsVariable() {
        try {
            String message = "";
            ArrayList<ArrayList<Map<String, String>>> sites_variables = new ArrayList<ArrayList<Map<String, String>>>();
            for (int i = 1; i <= _countDMs; i++) {
                sites_variables.add(new ArrayList<Map<String, String>>());
            }

            for (int i = 1; i <= COUNT_VARS; i++) {
                List<Integer> l = new ArrayList<>();
                //var = "x"+i+",10i,"+replicated;
                Map<String, String> v = new HashMap<>();
                v.put("name", "x" + i);
                v.put("value", "" + 10 * i);
                if ((i % 2) == 0) {
                    for (int j = 0; j < _countDMs; j++) {
                        v.put("replicated", "true");
                        v.put("recovered", "true");
                        sites_variables.get(j).add(v);
                        l.add(j);
                    }
                } else {
                    int put = (i % _countDMs) + 1;
                    v.put("replicated", "false");
                    sites_variables.get(put - 1).add(v);
                    l.add(put - 1);
                }
                _varLocations.put("x" + i, l);
            }

            for (int i = 0; i < _countDMs; i++) {
                // System.out.println(sites_variables.get(i).toArray()[0]);

                ArrayList<Map<String, String>> vars = sites_variables.get(i);
                for (Map<String, String> m : vars) {
                    Vector params = new Vector();
                    params.add(m);
                    params.add(currTime);
                    Object o = _clients.get(i).execute("database.initialize", params);
                }

            }

        } catch (java.lang.Throwable e) {
            System.err.println("Error during variables initialization" + e.getMessage());
        }
    }

    /**
     * Starts the TM reading in commands Blocked on the command line Each read
     * in increments time, Then the command is executed
     *
     * @author Alex Halter
     */
    public void start() {
        try {
            BufferedReader br
                    = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = br.readLine()) != null) {
                currTime = Clock.tick();
                ArrayList<Command> result = Parser.parse(input);
                for (Command c : result) {
                    commandCount++;
                    c.commandID = commandCount;
                    c.time = currTime;
                    //if it a read and there's a write waiting for this variable,
                    //do wait die immediately to prevent live lock
                    if (c.cType == Command.commandType.READ && _waitingWrites.get(c.varName) != null) {
                        if (doWaitDie(c, _varLocations.get(c.varName))) {
                            System.out.println("Read by " + c.tranName + " is waiting. A write is in the queue.");
                            //it is older and should wait
                            _transactions.get(c.tranName).waiting = c;
                        }

                    } else {
                        //otherwise, try to execute currect command
                        execute(c);
                    }
                }

                //build the queue -- go through all the transactions
                // get the waiting commands, then put them in a list
                //and sort by the time they were read
                _waitingWrites.clear();
                List<Command> waitingQueue = new ArrayList<>();
                for (TransactionData td : _transactions.values()) {
                    if (td.waiting != null) {
                        waitingQueue.add(td.waiting);
                        if (td.waiting.cType == Command.commandType.WRITE) {
                            if (_waitingWrites.get(td.waiting.varName) == null) {
                                _waitingWrites.put(td.waiting.varName, Command.commandType.WRITE);
                            }
                        }
                    }
                }

                CommandComparator com = new CommandComparator();
                Collections.sort(waitingQueue, com);

                //execute in order
                for (Command c : waitingQueue) {
                    //if the command is the most recent read command,
                    //no reason to retry now
                    if (c.commandID != commandCount) {
                        execute(c);
                    }
                }

                //remove any transactions that were aborted
                for (String trans : _toDelete) {
                    _transactions.remove(trans);
                }
                _toDelete.clear();
            }
        } catch (IOException io) {

        }
    }

    /**
     * Execute the command
     *
     * @param c
     * @author Alex Halter and Jacopo Cirrone
     */
    public void execute(Command c) {

        switch (c.cType) {
            case BEGIN:
                beginHandler(c);
                break;
            case BEGINRO:
                beginROHandler(c);
                break;
            case WRITE:
                if (errorHandler(c)) {
                    System.out.println("Error: transaction does not exist.");
                    return;
                }
                writeHandler(c);
                break;
            case FAIL:
                failHandler(c);
                break;
            case RECOVER:
                recoverHandler(c);
                break;
            case READ:
                if (errorHandler(c)) {
                    System.out.println("Error: transaction does not exist.");
                    return;
                }
                readHandler(c);
                break;
            case END:
                if (errorHandler(c)) {
                    System.out.println("Error: transaction does not exist.");
                    return;
                }
                endHandler(c);
                break;
            case DUMP:
                dumpHandler();
                break;
            case QUERYSTATE:
                queryHandler();
                break;
        }
    }

    /**
     * For a begin transaction, just record the start time, and enter in
     * transactions table
     *
     * @param c
     * @author Alex Halter
     */
    private void beginHandler(Command c) {
        TransactionData d = new TransactionData();
        d.isRO = false;
        d.startTime = c.time;
        d.name = c.tranName;
        _transactions.put(c.tranName, d);
    }

    /**
     * Being a read only transaction by registering the transaction With all the
     * DBs
     *
     * @param c
     * @author Alex Halter
     */
    private void beginROHandler(Command c) {
        TransactionData d = new TransactionData();
        d.isRO = true;
        d.startTime = c.time;
        d.name = c.tranName;
        _transactions.put(c.tranName, d);
        Vector params = new Vector();
        params.add(c.tranName);
        for (int i = 0; i < _countDMs; i++) {
            try {
                Object o = _clients.get(i).execute("database.registerRO", params);
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tries the write with all the available DMs Only successful if all DMs can
     * do the write (grant the lock)
     *
     * @param c
     * @author Alex Halter
     */
    private void writeHandler(Command c) {
        List<Integer> dbs = (c.waitingFor != null) ? c.waitingFor : _varLocations.get(c.varName);
        List<Integer> waitingFor = new ArrayList<>();
        boolean allFailed = true;
        for (int db : dbs) {
            Vector params = new Vector();
            params.add(c.tranName);
            params.add(c.varName);
            params.add(c.value);
            params.add(c.time);
            try {

                String s = (String) _clients.get(db).execute("database.writeVar", params);
                if (s.equals("failed")) {
                    continue;
                    //nothing   
                } else if (s.equals("yes")) {
                    allFailed = false;
                    //include this dm in 2PC or aborts
                    _transactions.get(c.tranName).accessed.add(db);
                } else {
                    allFailed = false;
                    waitingFor.add(db);
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }
        }

        if (allFailed) {
            System.out.println("Write by " + c.tranName + " of variable " + c.varName + " is waiting. All sites down");
            _transactions.get(c.tranName).waiting = c;
        } else if (waitingFor.size() == 0) {
            _transactions.get(c.tranName).waiting = null;
            System.out.println("Write by " + c.tranName + " of variable " + c.varName + " is successful");
        } else if (doWaitDie(c, waitingFor)) {
            System.out.println("Write by " + c.tranName + " of variable " + c.varName + " is waiting");
            c.waitingFor = waitingFor;
            _transactions.get(c.tranName).waiting = c;
        } else if (_transactions.get(c.tranName) != null) {
            _transactions.get(c.tranName).waiting = null;
        }

    }

    /**
     * Tries the read with the DMs that have that variable Successful if any
     * give the read
     *
     * @author Alex Halter and Jacopo Cirrone
     * @param c
     */
    private void readHandler(Command c) {
        String s;
        List<Integer> waitingFor = new ArrayList<>();
        boolean readOnly = _transactions.get(c.tranName).isRO;
        List<Integer> dbs = _varLocations.get(c.varName);
        boolean allFailed = true;
        for (int db : dbs) {
            Vector params = new Vector();
            params.add(c.tranName);
            params.add(c.varName);

            try {
                if (readOnly) {
                    s = (String) _clients.get(db).execute("database.readOnlyVar", params);
                } else {
                    params.add(c.time);
                    s = (String) _clients.get(db).execute("database.readVar", params);
                }

                if (s.equals("failed")) {
                    continue;
                } else if (s.equals("no")) {
                    allFailed = false;
                    waitingFor.add(db);
                    break;
                    //we can break because if it can't get a read lock, there must be a write to all locations
                } else {
                    allFailed = false;
                    System.out.println(c.tranName + " reads value " + s + " for variable " + c.varName + " from site " + (db +1));
                    //if its not readonly, we need to include this dm in 2PC or aborts
                    if (!readOnly) {
                        _transactions.get(c.tranName).accessed.add(db);
                    }
                    break;
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }
        }
        //if all sites are failed, it should wait, but no reason to do wait-die
        if (allFailed) {
            System.out.println("Read by " + c.tranName + " is waiting. All sites down");
            _transactions.get(c.tranName).waiting = c;
            //do wait die
        } else if (doWaitDie(c, waitingFor)) {
            System.out.println("Read by " + c.tranName + " is waiting");
            _transactions.get(c.tranName).waiting = c;
            //not waiting
        } else {
            _transactions.get(c.tranName).waiting = null;

        }
    }

    /**
     * does wait-die algorithm between the incoming command and any DMs it is
     * waiting on
     *
     * @param c incoming command
     * @param waitingFor the DMs that replied for it to wait
     * @return true if the command should wait, false if it aborted or isn't
     * waiting
     * @author Alex Halter
     */
    private boolean doWaitDie(Command c, List<Integer> waitingFor) {
        boolean stillWaiting = true;
        for (int db : waitingFor) {
            Vector params = new Vector();
            params.add(c.varName);
            try {
                //get the current lockholder(s) of that variable, at that DM
                Object[] trans = (Object[]) _clients.get(db).execute("database.getLockHolders", params);
                //do wait die between current transaction and any it is waiting for
                //until it either aborts or reaches end of all the transactions it is waiting for
                for (Object tran : trans) {
                    String name = (String) tran;
                    TransactionData td = _transactions.get(name);
                    //abort
                    if (td.startTime < _transactions.get(c.tranName).startTime) {
                        System.out.println(c.tranName + " aborts because is younger than " + td.name);
                        abortHandler(c.tranName);
                        stillWaiting = false;
                        break;
                    }

                }
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }
        }
        //if its still waiting return true
        return stillWaiting && waitingFor.size() > 0;
    }

    private boolean errorHandler(Command c) {

        return _transactions.get(c.tranName) == null;
    }

    /**
     * gets all the variables and values from all the DMs print them out (no
     * side effects)
     *
     * @author Alex Halter and Jacopo Cirrone
     */
    private void dumpHandler() {
        for (int j = 0; j < _countDMs; j++) {
            Vector params = new Vector();
            try {
                Object[] vars = (Object[]) _clients.get(j).execute("database.dump", params);
                if (vars.length > 0) {
                    int dm = j + 1;
                    System.out.println("DM " + dm + " dump:");
                    for (Object o : vars) {
                        Map<String, String> v = (Map<String, String>) o;
                        System.out.println(v.get("name") + " " + v.get("value") + " " + v.get("availability"));
                    }
                } else {
                    System.out.println("DM " + (j+1) + " is failed");
                }
            } catch (XmlRpcException e) {
                e.printStackTrace();

            }
        }
    }

    /**
     * for debugging the current state no side effects
     *
     * @author Alex Halter and Jacopo Cirrone
     */
    private void queryHandler() {
        System.out.println("***Transactions:");
        for (Map.Entry<String, TransactionData> entry : _transactions.entrySet()) {
            TransactionData td = (TransactionData) entry.getValue();
            System.out.println("-" + entry.getKey() + " ;start time: " + td.startTime);
            String accessed = "";
            for (Integer i : td.accessed) {
                accessed = accessed + i + ",";
            }
            System.out.println("DMs accessed: " + accessed);
            if (td.waiting != null) {
                System.out.println("It's waiting for " + td.waiting.varName);
            }
        }

        for (int j = 0; j < _countDMs; j++) {
            Vector params = new Vector();
            params.add(String.valueOf(j + 1));
            try {
                Object vars = (Object) _clients.get(j).execute("database.query", params);
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tells the DM it is failed
     *
     * @param c
     * @author Jacopo Cirrone
     */
    private void failHandler(Command c) {
        try {
            Vector params = new Vector();
            String s = (String) _clients.get((c.site - 1)).execute("database.fail", params);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }

    }

    /**
     * Tells the DM it is recovered
     *
     * @param c
     * @author Jacopo Cirrone
     */
    private void recoverHandler(Command c) {
        try {
            Vector params = new Vector();
            params.add(currTime);
            _clients.get(c.site - 1).execute("database.recover", params);
        } catch (XmlRpcException e) {
            e.printStackTrace();
        }

    }

    /**
     * Aborts a transaction Adds it to _toDelete for final deletion
     *
     * @param trans transaction to abort
     */
    private void abortHandler(String trans) {
        TransactionData td = _transactions.get(trans);
        Set<Integer> dbs = td.accessed;
        for (int db : dbs) {
            Vector params = new Vector();
            params.add(trans);
            try {
                _clients.get(db).execute("database.abort", params);
            } catch (XmlRpcException e) {
                e.printStackTrace();
            }
        }
        _toDelete.add(trans);
    }

    /**
     * Begins 2PC with all the DMs a transaction has accessed Asks each DM it
     * has accessed if it should commit
     *
     * @param c
     * @author Jacopo Cirrone
     */
    private void endHandler(Command c) {
        TransactionData td = _transactions.get(c.tranName);
        Set<Integer> dbs = td.accessed;
        Map<Integer, String> dms_cause = new HashMap<>();
        if (!td.isRO) {
            boolean ok = true;
            for (int db : dbs) {
                Vector params = new Vector();
                params.add(c.tranName);
                String s = null;
                try {
                    s = (String) _clients.get(db).execute("database.endready", params);
                } catch (XmlRpcException e) {
                    e.printStackTrace();
                }
                if (s.equals("no") || s.equals("failed")) {
                    ok = false;
                    if (s.equals("no")) {
                        dms_cause.put(db + 1, "was failed");
                    } else {
                        dms_cause.put(db + 1, "is failed");
                    }
                    break;
                }
            }
            if (ok) {
                if (dbs.size() == 0) {
                    System.out.println(c.tranName + " commits");
                } else {
                    for (int db : dbs) {
                        Vector params = new Vector();
                        params.add(c.tranName);
                        try {
                            String s = (String) _clients.get(db).execute("database.end", params);

                        } catch (XmlRpcException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(c.tranName + " commits");
                }
            }
            if (!ok) {
                for (int db : dbs) {
                    Vector params = new Vector();
                    params.add(c.tranName);
                    try {
                        String s = (String) _clients.get(db).execute("database.abort", params);

                    } catch (XmlRpcException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(c.tranName + " aborts because:");
                for (Map.Entry<Integer, String> entry : dms_cause.entrySet()) {
                    System.out.println(entry.getKey() + " " + entry.getValue());
                }
            }
        } else {
            System.out.println(c.tranName + " RO commits");
            for (int db : dbs) {
                Vector params = new Vector();
                params.add(c.tranName);
                try {
                    String s = (String) _clients.get(db).execute("database.endRO", params);
                } catch (XmlRpcException e) {
                    e.printStackTrace();
                }

            }
        }
        _toDelete.add(c.tranName);
    }

    public static void main(String[] args) {
        List<Integer> ports = new ArrayList<>();
        for (String s : args) {

            ports.add(Integer.parseInt(s));
        }
        TransactionManager tc = new TransactionManager(ports);
        tc.setUpDMsVariable();
        tc.start();

    }

    /**
     * Comparator class for sorting the queue
     *
     * @author Alex Halter
     */
    public class CommandComparator implements Comparator<Command> {

        @Override
        public int compare(Command c1, Command c2) {

            if (c1.time < c2.time) {
                return -1;
            }
            return 0;
        }
    }

}
