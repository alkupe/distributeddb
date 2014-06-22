/**
 * Flexible class, to store all the possible data associated with a command
 *
 * @author Alex Halter and Jacopo Cirrone
 */
package tm;

import java.util.Objects;
import java.util.List;

public class Command {

    public enum commandType {

        BEGIN, BEGINRO, END, FAIL, RECOVER, WRITE, READ,
        DUMP, QUERYSTATE, QUIT
    }
    public String varName;
    public int site;
    public commandType cType;
    public String tranName;
    public int value;
    public int time;
    public List<Integer> waitingFor;
    public int commandID;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.varName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Command other = (Command) obj;
        if (!Objects.equals(this.varName, other.varName)) {
            return false;
        }
        if (this.cType != other.cType) {
            return false;
        }
        if (!Objects.equals(this.tranName, other.tranName)) {
            return false;
        }
        if (this.value != other.value) {
            return false;
        }
        return true;
    }

}
