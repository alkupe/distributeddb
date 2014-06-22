/**
 * Unit Tests for the parser. Give it an example input, then test that the right
 * Commands are generated.
 */
package tm;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;

/**
 *
 * @author Alex Halter
 */
public class ParserTest {

    public ParserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of parser method, of class Parser.
     */
    @Test
    public void testParser() {
        System.out.println("parser");
        String command = "begin(T1)";
        Command expResult = new Command();
        expResult.cType = Command.commandType.BEGIN;
        expResult.tranName = "T1";
        ArrayList<Command> result = Parser.parse(command);
        assertEquals(expResult, result.get(0));
        command = "R(T2,x1)";
        result = Parser.parse(command);
        expResult = new Command();
        expResult.cType = Command.commandType.READ;
        expResult.tranName = "T2";
        expResult.varName = "x1";
        assertEquals(expResult, result.get(0));
        command = "beginRO(T3)";
        result = Parser.parse(command);
        expResult = new Command();
        expResult.cType = Command.commandType.BEGINRO;
        expResult.tranName = "T3";
        assertEquals(expResult, result.get(0));
        command = "W(T4, x5,34)";
        expResult = new Command();
        result = Parser.parse(command);
        expResult.cType = Command.commandType.WRITE;
        expResult.tranName = "T4";
        expResult.value = 34;
        expResult.varName = "x5";
        assertEquals(expResult, result.get(0));
        command = "W(T1, x3 , 33 ); R(T2,x3)";
        expResult = new Command();
        result = Parser.parse(command);
        expResult.cType = Command.commandType.WRITE;
        expResult.tranName = "T1";
        expResult.value = 33;
        expResult.varName = "x3";
        assertEquals(expResult, result.get(0));
        expResult = new Command();
        expResult.cType = Command.commandType.READ;
        expResult.tranName = "T2";
        expResult.varName = "x3";
        assertEquals(expResult, result.get(1));
        command = "recover(4); fail(3)";
        expResult = new Command();
        result = Parser.parse(command);
        expResult.cType = Command.commandType.RECOVER;
        expResult.site = 4;
        assertEquals(expResult, result.get(0));
        expResult = new Command();
        result = Parser.parse(command);
        expResult.cType = Command.commandType.FAIL;
        expResult.site = 3;
        assertEquals(expResult, result.get(1));
    }

}
