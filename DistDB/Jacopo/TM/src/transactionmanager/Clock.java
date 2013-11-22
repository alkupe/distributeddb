/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package transactionmanager;

/**
 *
 * @author alexh
 */
public class Clock {
    private static int time = 0;
    public static int tick() {
        return time++;
    }
}
