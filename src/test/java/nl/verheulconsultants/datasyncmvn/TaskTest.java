package nl.verheulconsultants.datasyncmvn;

import static nl.verheulconsultants.datasyncmvn.DataSync.loggerFileHandler;
import java.net.URISyntaxException;
import java.io.File;
import java.net.URL;
import javax.swing.JTextArea;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author erik
 */
public class TaskTest {

    public TaskTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        File logFile = Routines.openDefaultLogFile();
        //must initiate a logggerFileHandler when calling methods from class Routines which do logging
        loggerFileHandler = Routines.createFileHandler(logFile.getPath());
    }

    @After
    public void tearDown() {
    }

    private File openTestResourceFile(String name) {
        System.out.println("===================================================");
        URL url = TaskTest.class.getProtectionDomain().getClassLoader().getResource(name);
        System.out.println("URL found =" + url);
        File file;
        // see http://weblogs.java.net/blog/2007/04/25/how-convert-javaneturl-javaiofile
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        }
        return file;
    }

    /**
     * Test of leesTabel method, of class Task.
     */
    @Test
    public void testLeesTabel1() {
        System.out.println("leesTabel with invalid file");
        File testTabel = openTestResourceFile("Help.html");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testLeesTabel2() {
        System.out.println("leesTabel with valid file");
        File testTabel = openTestResourceFile("toegestaan.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = true;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeesTabel3() {
        System.out.println("leesTabel - NIET toegestaan: zelfde bron naar meerdere bestemmingen");
        File testTabel = openTestResourceFile("nietToegestaan1.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeesTabel4() {
        System.out.println("leesTabel - NIET toegestaan: bestemming is subdirectory van de bron");
        File testTabel = openTestResourceFile("nietToegestaan2.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeesTabel5() {
        System.out.println("leesTabel - Kies of voor file, of voor directory selectie, niet beide");
        File testTabel = openTestResourceFile("nietToegestaan3.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeesTabel6() {
        System.out.println("leesTabel - Filters op de zelfde bron mogen elkaar niet overlappen");
        File testTabel = openTestResourceFile("nietToegestaan4.csv");
        Task instance = new Task(testTabel, new JTextArea());
        MainFrame.checkAccessToBronAndBestemming = false;
        boolean expResult = false;
        boolean result = instance.leesTabel();
        assertEquals(expResult, result);
    }

    @Test
    public void testLeesTabel7() {
        System.out.println("leesTabel - bestemming zonder bron");
        File testTabel = openTestResourceFile("nietToegestaan5.csv");
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