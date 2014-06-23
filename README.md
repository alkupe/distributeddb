distributeddb
=============
A distributed database, complete
with multiversion concurrency control, deadlock avoidance, replication, and failure recovery. Written in Java, using XML-RPC libaray for Advanced Databases class.

Run runDatabases from the command line to run the database on a given number of nodes. Currently runs on a single machine, but could be easily extended to run on mulptiple machines.
Takes input and uses some basic algorithms to maintain the the correctness of the database. Some basics of the input:
A basic overview:
T1 is transaction 1.
W is write, R is read
For simplicity, there are 20 variables in the database, numbered x1 to x20. The even-numbered variables are replicated at multiple sites. 

Writes occur at all live databases, reads occur from any valid site. The database implements two-phase locking to ensure correctness. It also supports read only transactions that acquire no locks.
begin(T1) says that T1 begins
beginRO(T3) says that T3 begins and is read-only
R(T1, x4) says transaction 1 wishes to read x4 (provided it can get the
locks or provided it doesn’t need the locks (if T1 is a read-only transaction)).
It should read any up (i.e. alive) copy and return the current value.
W(T1, x6,v) says transaction 1 wishes to write all available copies of x6 (provided it can get the locks) with the value v. If it can get the locks on
only some sites, it should get them.
