/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client_1;

/**
 *
 * @author pino
 */
public class Address_DM {
    
    private String IP;
    
    private String machine_name;
    
    private int port;
    
    public Address_DM(String IP, String machine_name, int port){
        this.IP = IP;
        this.machine_name = machine_name;
        this.port = port;
    }

    /**
     * @return the IP
     */
    public String getIP() {
        return IP;
    }

    /**
     * @param IP the IP to set
     */
    public void setIP(String IP) {
        this.IP = IP;
    }

    /**
     * @return the machine_name
     */
    public String getMachine_name() {
        return machine_name;
    }

    /**
     * @param machine_name the machine_name to set
     */
    public void setMachine_name(String machine_name) {
        this.machine_name = machine_name;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
    
}
