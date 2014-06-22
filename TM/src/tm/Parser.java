/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tm;

import java.util.ArrayList;
import java.util.regex.*;

/**
 *
 * @author alexh
 */
public class Parser {

    public static ArrayList<Command> parse(String command) {
        ArrayList<Command> commands = new ArrayList<Command>();

        String patternstring = "(\\w*)\\(([^\\)]*)\\)";
        Pattern pattern = Pattern.compile(patternstring);
        Matcher m = pattern.matcher(command);
        while (m.find()) {
            Command c = new Command();
            String type = m.group(1);
            String contents = m.group(2);
            String[] params = contents.split(",");
            switch (type) {
                case "begin":
                    c.cType = Command.commandType.BEGIN;
                    c.tranName = params[0].trim();
                    break;
                case "beginRO":
                    c.cType = Command.commandType.BEGINRO;
                    c.tranName = params[0].trim();
                    break;
                case "W":
                    c.cType = Command.commandType.WRITE;
                    c.tranName = params[0].trim();
                    c.varName = params[1].trim();
                    c.value = Integer.valueOf(params[2].trim());
                    break;
                case "R":
                    c.cType = Command.commandType.READ;
                    c.varName = params[1].trim();
                    c.tranName = params[0].trim();
                    break;
                case "fail":
                    c.cType = Command.commandType.FAIL;
                    c.site = Integer.valueOf(params[0].trim());
                    break;
                case "recover":
                    c.cType = Command.commandType.RECOVER;
                    c.site = Integer.valueOf(params[0].trim());
                    break;
                case "end":
                    c.cType = Command.commandType.END;
                    c.tranName = params[0].trim();
                    break;
                case "dump":
                    c.cType = Command.commandType.DUMP;
                    break;
                case "query":
                    c.cType = Command.commandType.QUERYSTATE;
                    break;
                case "quit":
                    System.exit(0);
            }
            commands.add(c);
        }
        return commands;
    }

}
