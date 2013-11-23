/**
 * This class implements web service logic Server side
 */

package Server;
import java.util.*;
import java.io.*;
import javax.jws.WebMethod;
import javax.jws.WebService;
import java.net.MalformedURLException;
import javax.jws.WebParam;
import json.*;
import json.JSONArray.*;
import javax.swing.*;
import Server.LockManager;


@WebService()
public class DM {

     /*Attributes*/
    
    public Map<String,Variable> variables = new HashMap<String,Variable>();
    public Map<String,Map<String,Variable>> varCopies = new HashMap<String,Map<String,Variable>>();    
    public LockManager lm = new LockManager();
    
    @WebMethod(operationName = "Initialize")
    public String Initialize(String variables_str, String file_name) {
       try{
            JSONArray Json_variables = new JSONArray(variables_str);
            for (int i=0; i < Json_variables.length(); i++){
                String tmp_str = Json_variables.get(i).toString();
                String[] arr_str = tmp_str.split(",");
                Variable var = new Variable(arr_str[0], arr_str[1], Boolean.valueOf(arr_str[2]));
                this.variables.put(arr_str[0], var);
            }
            
            File file = new File (System.getProperty("user.dir")+"/"+"DM"+"/"+file_name);
            BufferedWriter bw = null;
            FileWriter outFile=new FileWriter(file);
            bw = new BufferedWriter(outFile);
            for (Map.Entry entry : this.variables.entrySet()) {

                bw.write(entry.getKey().toString() + ", " + ((Variable)entry.getValue()).toString()+"\n");  
            }
            bw.close();
            return "fine";
       }
       catch(Throwable e){System.err.println(e);}
       
       return null;
    }
    
    @WebMethod(operationName = "registerRO")
    public void registerRO(String transaction) throws CloneNotSupportedException {
            Map<String,Variable> copy = new HashMap<String,Variable>();
            for (Map.Entry<String, Variable> entry : variables.entrySet()) {
                copy.put(entry.getKey(), (Variable) entry.getValue().clone());
            }
            varCopies.put(transaction, copy);

    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "Write")
    public Boolean Write(@WebParam(name = "variable") String variable, @WebParam(name = "value") int value) {
        //TODO write your implementation code here:
        return null;
    }

    



}
