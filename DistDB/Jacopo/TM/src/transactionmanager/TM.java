/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client_1;
import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import json.*;
import transactionmanager.Command;
import transactionmanager.Parser;
import transactionmanager.Clock;
/**
 *
 * @author pino
 */
public class TM {
    
    public static final String FILE_XML_ADDRESS = "ListDMs.xml";
    
    private ArrayList<Address_DM> array_address;
    private HashMap<String,Command> transactionHistory;
    private HashMap<String,Boolean> transactions;
    private int currTime;
    public TM()
    {
        array_address = readXMLinfoAddress(FILE_XML_ADDRESS);
        transactionHistory = new HashMap<String,Command>();
        transactions = new HashMap<String,Boolean>();
    }

    public void setUpDMsVariable(){
        try{
            String message="";
            ArrayList<ArrayList<String>> sites_variables = new ArrayList<ArrayList<String>>(); 
            for(int i=0; i<10; i++){
                sites_variables.add(new ArrayList<String>());
            }
            
            boolean replicated = false;
            String var ="";
            int index=0;
            for (int i=1; i <= 20 ; i++){
                var = "x"+i+",10i,"+replicated;
                if((i%2)==0){
                    for(int j=0; j<10; j++){
                            sites_variables.get(j).add(var);
                        }
                    }
                else{
                    sites_variables.get(index).add(var);
                    index++;
                }
            }
            
            boolean bool =true;
            for(int i=1; i<=10; i++){
                JSONArray JSonVariables = new JSONArray(sites_variables.get(i-1));
                message = initialize(i,JSonVariables.toString(), "variables"+i+".txt");
                if(!(message.matches("fine"))){
                    System.err.println("Error during variables initialization in site "+i);
                    bool = false;
                }
            }
            if(bool)
                System.out.println("Initialization complete");
        }
        catch(java.lang.Throwable e)
        {
            System.err.println("Error during variables initialization"+e.getMessage());
        }
    }
    
        
    //Web service call, it's conceptually correct having different webservices, 
    //one for each site

    private static String initialize(java.lang.Integer site, java.lang.String variables, java.lang.String file_name) {
        
        switch(site){
            case 1:
                server.DM1Service service1 = new server.DM1Service();
                server.DM1 port1 = service1.getDM1Port();
                return port1.initialize(variables, file_name);
            case 2:
                server.DM2Service service2 = new server.DM2Service();
                server.DM2 port2 = service2.getDM2Port();
                return port2.initialize(variables, file_name);
            case 3:
                server.DM3Service service3 = new server.DM3Service();
                server.DM3 port3 = service3.getDM3Port();
                return port3.initialize(variables, file_name);
            case 4:
                server.DM4Service service4 = new server.DM4Service();
                server.DM4 port4 = service4.getDM4Port();
                return port4.initialize(variables, file_name);
            case 5:
                server.DM5Service service5 = new server.DM5Service();
                server.DM5 port5 = service5.getDM5Port();
                return port5.initialize(variables, file_name);
            case 6:
                server.DM6Service service6 = new server.DM6Service();
                server.DM6 port6 = service6.getDM6Port();
                return port6.initialize(variables, file_name);
            case 7:
                server.DM7Service service7 = new server.DM7Service();
                server.DM7 port7 = service7.getDM7Port();
                return port7.initialize(variables, file_name);
            case 8:
                server.DM8Service service8 = new server.DM8Service();
                server.DM8 port8 = service8.getDM8Port();
                return port8.initialize(variables, file_name);
            case 9:
                server.DM9Service service9 = new server.DM9Service();
                server.DM9 port9 = service9.getDM9Port();
                return port9.initialize(variables, file_name);
            case 10:
                server.DM10Service service10 = new server.DM10Service();
                server.DM10 port10 = service10.getDM10Port();
                return port10.initialize(variables, file_name);
            default: break;
        }
        return "";
    }

    
        private static void registerRO(java.lang.Integer site, java.lang.String transaction) {
        /*
        switch(site){
            case 1:
                server.DM1Service service1 = new server.DM1Service();
                server.DM1 port1 = service1.getDM1Port();
                port1. (variables, file_name);
            case 2:
                server.DM2Service service2 = new server.DM2Service();
                server.DM2 port2 = service2.getDM2Port();
                return port2.initialize(variables, file_name);
            case 3:
                server.DM3Service service3 = new server.DM3Service();
                server.DM3 port3 = service3.getDM3Port();
                return port3.initialize(variables, file_name);
            case 4:
                server.DM4Service service4 = new server.DM4Service();
                server.DM4 port4 = service4.getDM4Port();
                return port4.initialize(variables, file_name);
            case 5:
                server.DM5Service service5 = new server.DM5Service();
                server.DM5 port5 = service5.getDM5Port();
                return port5.initialize(variables, file_name);
            case 6:
                server.DM6Service service6 = new server.DM6Service();
                server.DM6 port6 = service6.getDM6Port();
                return port6.initialize(variables, file_name);
            case 7:
                server.DM7Service service7 = new server.DM7Service();
                server.DM7 port7 = service7.getDM7Port();
                return port7.initialize(variables, file_name);
            case 8:
                server.DM8Service service8 = new server.DM8Service();
                server.DM8 port8 = service8.getDM8Port();
                return port8.initialize(variables, file_name);
            case 9:
                server.DM9Service service9 = new server.DM9Service();
                server.DM9 port9 = service9.getDM9Port();
                return port9.initialize(variables, file_name);
            case 10:
                server.DM10Service service10 = new server.DM10Service();
                server.DM10 port10 = service10.getDM10Port();
                return port10.initialize(variables, file_name);
            default: break;
        }
        return "";
                */
    }


    public ArrayList<Address_DM> readXMLinfoAddress(String nameFile)
    {
        
        array_address = new ArrayList<Address_DM>();
        Address_DM address;
        //legge da xml l'array di object file e imposta il valore int_idFile
        //che considera il numero dei file all'interno dell'arrayList
        try {
            //Creo un SAXBuilder e con esco costruisco un document
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new File(nameFile));
            //Prendo la radice
            Element root = document.getRootElement();
            //Estraggo i figli dalla radice
            java.util.List children = root.getChildren();
            Iterator iterator = children.iterator();
            String tmp_IP;
            String tmp_port;
            String tmp_name;
            String tmp_serviceName;
            String tmp_machineType;
            String tmp_fileName;
            int port;
            //Per ogni figlio
            while (iterator.hasNext())
            {
                //Aggiungo un elemento all'array di Address_Replication ,chiamato array_addres che contiene tutti gli IP delle macchine replicanti
                Element item = (Element) iterator.next();
                Element description = item.getChild("DESCR");
                tmp_IP = item.getAttributeValue("IP");
                tmp_port = item.getAttributeValue("port");
                tmp_name = item.getAttributeValue("name");
                tmp_serviceName = item.getAttributeValue("serviceName");
                tmp_machineType = item.getAttributeValue("machineType");
                //tmp_fileName = item.getAttributeValue("fileName");
                port = Integer.parseInt(tmp_port);
                String tmp = "http://" + tmp_IP + ":" + tmp_port + "/" + tmp_machineType;
                address = new Address_DM(tmp, tmp_serviceName, port);
                address.setMachine_name(tmp_name);
                array_address.add(address);
            }
        }
        catch (java.lang.Throwable e)
        {
            System.err.println("Error reading XML file containing DMs address" + e.getMessage());
            e.printStackTrace();
        }
        return array_address;
    }
    
    public void start() {
        try {
            BufferedReader br = 
                          new BufferedReader(new InputStreamReader(System.in));
            String input;
            while((input=br.readLine())!=null){
                currTime = Clock.tick();
                ArrayList<Command> result = Parser.parser(input);
                for (Command c : result) {
                    c.time = currTime;
                    execute(c);
                }
                //try queue here
            }
        } catch(IOException io){
            
	}	
    }
    
    public void execute(Command c) {
        
        switch(c.cType) {
            case BEGIN:
                beginHandler(c);
                break;
            case BEGINRO:
                beginROHandler(c);
                break;
            case WRITE:
                //send write to all DMs containing variable
                //enter in T table
                break;
            case FAIL:
                //send fail to correct DM
                break;
            case RECOVER:
                //send restore to correct DM
                break;
            case READ:
                //send read to a DM containing variable until success
                //enter in T table
                break;
            case END:
                //send end TM to all TMs used in T table
                //do 2PC
                //get the reads if any
                //cleanup
                break;
            case DUMP:
                //get all the database values from all the DMs and print them out
                break;
            case QUERYSTATE:
                //debug query--get all the states from all the databases and the variables
                break;
        }
    }
    
    private void beginHandler(Command c) {
        transactions.put(c.tranName, false);
    }
    
    private void beginROHandler(Command c) {
        transactions.put(c.tranName, true);
    }

}
