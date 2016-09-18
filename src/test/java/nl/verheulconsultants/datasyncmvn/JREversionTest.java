package nl.verheulconsultants.datasyncmvn;

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
public class JREversionTest {

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
    public JREversionTest() {
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
     * Test of checkVersion method, of class JREversion.
     */
    @Test
    public void testCheckVersion() {
        System.out.println("checkVersion against 1.4.2_03 or higher");
        JREversion instance = new JREversion();
        boolean expResult = true;
        boolean result = instance.checkVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class JREversion.
     */
    @Test
    public void testToString() {
        System.out.println("toString must be " + System.getProperty("java.version"));
        JREversion instance = new JREversion();
        String expResult = System.getProperty("java.version");
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}