/*
 * Class for the DM.
 * basically just a shell that starts the DM at a given port
 * all of the interaction with the client is done via the Handler class
 */
package dm;

import org.apache.xmlrpc.webserver.*;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.webserver.ServletWebServer;

public class DataManager {

    /*
     @param port to listen on
     Starts up a RPCXML Server on that port. 
     Maps "database" interaction to the Handler class
     @author Alex Halter
     */
    public DataManager(int port) {
        final int _port;
        try {
            _port = port;
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            WebServer server = new WebServer(port);
            XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
            phm.addHandler("database", Handler.class);
            xmlRpcServer.setHandlerMapping(phm);
            server.start();
        } catch (Exception exception) {
            System.err.println("Server: " + exception);
            exception.printStackTrace();
        }

    }

    public static void main(String[] args) {
        DataManager dm = new DataManager(Integer.parseInt(args[0]));
    }
}
