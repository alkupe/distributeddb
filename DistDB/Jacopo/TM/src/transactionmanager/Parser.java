/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package transactionmanager;

import java.util.ArrayList;
import java.util.regex.*;
/**
 *
 * @author alexh
 */
public class Parser {
    public static ArrayList<Command> parser(String command) {
        ArrayList<Command> commands = new ArrayList<Command>();

        String patternstring = "(\\w*)\\(([^\\)]*)\\)";
        Pattern pattern = Pattern.compile(patternstring);
        Matcher m = pattern.matcher(command);
        while (m.find()) {
            Command c = new Command();
            String type = m.group(1);
            String contents = m.group(2);
            String[] params = contents.split(",");

            switch(type) {
                case "begin":
                    c.cType = Command.commandType.BEGIN;
                    c.tranName = params[0];
                    break;
                case "beginRO":
                    c.cType = Command.commandType.BEGINRO;
                    c.tranName = params[0];
                    break;
                case "W" :
                    c.cType = Command.commandType.WRITE;
                    c.tranName = params[0];
                    c.varName = params[1];
                    c.value = Integer.valueOf(params[2]);
                    break;
                case "R" :
                    c.cType = Command.commandType.READ;
                    c.varName = params[1];
                    c.tranName = params[0];
                    break;
                case "fail":
                    c.cType = Command.commandType.FAIL;
                    c.site = Integer.valueOf(params[0]);
                    break;
                case "recover":
                    c.cType = Command.commandType.RECOVER;
                    c.site = Integer.valueOf(params[0]);
                    break;
                case "end":
                    c.cType = Command.commandType.END;
                    c.tranName = params[0];
                    break;
                case "dump":
                    c.cType = Command.commandType.DUMP;
                    break;
                case "query":
                    c.cType = Command.commandType.QUERYSTATE;
                    break;
            }
            commands.add(c);
        }
        return commands;
    }
    
    
}
