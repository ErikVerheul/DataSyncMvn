package nl.verheulconsultants.datasyncmvn;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
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
public class MappingTest {

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
    public MappingTest() {
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
     * Test of compareTo method, of class Mapping.
     */
    @Test
    public void testCompareTo1() {
        System.out.println("compareTo test equal Bron and Bestemming");
        boolean ignoreCase = true;
        Object o = new Mapping("\\\\bronserver\\dir\\abc.xyz", "\\\\bestemmingserver\\dir\\abc.xyz", false, false,10L, 100L, 0, false, ignoreCase);
        Mapping instance = new Mapping("\\\\bronserver\\dir\\abc.xyz", "\\\\bestemmingserver\\dir\\abc.xyz", false, false, 10L, 100L, 0, false, ignoreCase);
        int expResult = 0;
        int result = instance.compareTo(o);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testCompareTo2() {
        System.out.println("compareTo test ignoreCase");
        boolean ignoreCase = true;
        Object o = new Mapping("\\\\bronserver\\dir\\abc.xyz", "\\\\bestemmingserver\\dir\\abc.xyz", false, false, 10L, 100L, 0, false, ignoreCase);
        Mapping instance = new Mapping("\\\\bronserver\\dir\\abc.xyz", "\\\\bestemmingserver\\dir\\ABC.xyz", false, false, 10L, 100L, 0, false, ignoreCase);
        int expResult = 0;
        int result = instance.compareTo(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Mapping.
     */
    @Test
    public void testEquals1() {
        System.out.println("equals test ignoreCase");
        boolean ignoreCase = true;
        Object o = new Mapping("\\\\bronserver\\dir\\abc.xyz", "\\\\bestemmingserver\\dir\\abc.xyz", false, false, 10L, 100L, 0, false, ignoreCase);
        Mapping instance = new Mapping("\\\\bronserver\\dir\\abc.xyz", "\\\\bestemmingserver\\dir\\ABC.xyz", false, false, 10L, 100L, 0, false, ignoreCase);
        boolean expResult = true;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    @SuppressWarnings("EC_UNRELATED_TYPES")
    public void testEquals2() {
        System.out.println("equals test different types");
        boolean ignoreCase = true;
        Object o = "\\\\bronserver\\dir\\abc.xyz" + "\\\\bestemmingserver\\dir\\abc.xyz"; // a String
        Mapping instance = new Mapping("\\\\bronserver\\dir\\abc.xyz", "\\\\bestemmingserver\\dir\\abc.xyz", false, false, 10L, 100L, 0, false, ignoreCase);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }


    /**
     * Test of toString method, of class Mapping.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        boolean ignoreCase = true;
        Mapping instance = new Mapping("\\\\bronserver\\dir\\abc.xyz", "\\\\bestemmingserver\\dir\\abc.xyz", false, false, 10L, 100L, 0, false, ignoreCase);
        String expResult = "Mapping from Bron " + "\\\\bronserver\\dir\\abc.xyz" + " to Bestemming " + "\\\\bestemmingserver\\dir\\abc.xyz";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}