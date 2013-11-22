/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package transactionmanager;

import java.util.Objects;

/**
 *
 * @author alexh
 */
public class Command {
    public enum commandType {
        BEGIN,BEGINRO,END,FAIL,RECOVER,WRITE,READ,
        DUMP,QUERYSTATE
    }
    public String varName;
    public int site;
    public commandType cType;
    public String tranName;
    public int value;
    public int time;
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
