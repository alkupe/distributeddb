/**
 * Variable class, stores the variable state
 *
 * @author Jacopo Cirrone
 */
package dm;

public class Variable implements Cloneable {

    private String name;

    private String value;

    private boolean replicated;

    private boolean recovered;

    public Variable(String name, String value, boolean replicated) {
        this.name = name;
        this.value = value;
        this.replicated = replicated;
        this.recovered = false;
    }

    public String toString() {
        return name + "," + value + "," + replicated + "," + recovered;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the replicated
     */
    public boolean isReplicated() {
        return replicated;
    }

    /**
     * @param replicated the replicated to set
     */
    public void setReplicated(boolean replicated) {
        this.replicated = replicated;
    }

    /**
     * @return the recovered
     */
    public boolean isRecovered() {
        return recovered;
    }

    /**
     * @param recovered the recovered to set
     */
    public void setRecovered(boolean recovered) {
        this.recovered = recovered;
    }

    /**
     *
     * For easy variable copying
     *
     * @return a new variable that is the same
     * @throws CloneNotSupportedException
     */
    public Variable clone() throws CloneNotSupportedException {
        return (Variable) super.clone();
    }

}
