/**
 * Very simple class for incrementing time.
 *
 * @author Alex Halter
 *
 */
package tm;

public class Clock {

    private static int time = 0;

    public static int tick() {
        return time++;
    }
}
