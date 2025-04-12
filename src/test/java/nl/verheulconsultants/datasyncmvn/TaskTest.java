package nl.verheulconsultants.datasyncmvn;

import java.io.File;
import javax.swing.JTextArea;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Erik Verheul <erik@verheulconsultants.nl>
 */
public class TaskTest {

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     *
     */
    public TaskTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of leesTabel method, of class Task.
     */
    @Test
    public void testLeesTabel1() {
        System.out.println("leesTabel with invalid file");
        File testTabel = Routines.openResourceFile("Help.html");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of leesTabel method, of class Task.
     */
    @Test
    public void testLeesIcon() {
        
        // xxxxxxxxxxxxx tets test xxxxxxxxxxxxxxxxx
        String home = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replaceAll("%20", " ");
        System.out.println("home = " + home);
        
        System.out.println("leesTabel with invalid file");
        // the Help file is not a valid input file
        File testTabel = Routines.openResourceFile("Help.html");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }
    
    /**
     *
     */
    @Test
    public void testLeesTabel2() {
        System.out.println("leesTabel with valid file");
        File testTabel = Routines.openResourceFile("toegestaan.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = true;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testLeesTabel3() {
        System.out.println("leesTabel - NIET toegestaan: zelfde bron naar meerdere bestemmingen");
        File testTabel = Routines.openResourceFile("nietToegestaan1.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testLeesTabel4() {
        System.out.println("leesTabel - NIET toegestaan: bestemming is subdirectory van de bron");
        File testTabel = Routines.openResourceFile("nietToegestaan2.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testLeesTabel5() {
        System.out.println("leesTabel - Kies of voor file, of voor directory selectie, niet beide");
        File testTabel = Routines.openResourceFile("nietToegestaan3.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testLeesTabel6() {
        System.out.println("leesTabel - Filters op de zelfde bron mogen elkaar niet overlappen");
        File testTabel = Routines.openResourceFile("nietToegestaan4.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testLeesTabel7() {
        System.out.println("leesTabel - bestemming zonder bron");
        File testTabel = Routines.openResourceFile("nietToegestaan5.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }


    /**
     * Test of vergelijk method, of class Task.
     *
    @Test
    public void testVergelijk() {
        System.out.println("vergelijk");
        JTextArea progressWindow = null;
        Task instance = null;
        Result expResult = null;
        Result result = instance.vergelijk(progressWindow);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sync method, of class Task.
     *
    @Test
    public void testSync() {
        System.out.println("sync");
        JTextArea progressWindow = null;
        Task instance = null;
        Result expResult = null;
        Result result = instance.sync(progressWindow);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */

}