/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class FilterSpecTest {

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
    public FilterSpecTest() {
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
     * Test of overlap method, of class FilterSpec.
     */
    @Test
    public void testOverlap1() {
        System.out.println("overlap abc.* and abc.*");
        String pathWildCard1 = "abc.*";
        String pathWildCard2 = "abc.*";
        boolean filterDirectories = false;
        boolean filterFiles = true;
        boolean filterSubdirectories = true;
        boolean ignoreCase = true;       
        FilterSpec instance = new FilterSpec(pathWildCard1, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        FilterSpec aFileWildcard = new FilterSpec(pathWildCard2, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        boolean expResult = true;
        boolean result = instance.overlap(aFileWildcard);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testOverlap2() {
        System.out.println("overlap abc.* and *c.*");
        String pathWildCard1 = "abc.*";
        String pathWildCard2 = "*c.*";
        boolean filterDirectories = false;
        boolean filterFiles = true;
        boolean filterSubdirectories = true;
        boolean ignoreCase = true;
        FilterSpec instance = new FilterSpec(pathWildCard1, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        FilterSpec aFileWildcard = new FilterSpec(pathWildCard2, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        boolean expResult = true;
        boolean result = instance.overlap(aFileWildcard);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testOverlap3() {
        System.out.println("no overlap abc.* and ?X.*");
        String pathWildCard1 = "abc.*";
        String pathWildCard2 = "?X.*";
        boolean filterDirectories = false;
        boolean filterFiles = true;
        boolean filterSubdirectories = true;
        boolean ignoreCase = false;
        FilterSpec instance = new FilterSpec(pathWildCard1, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        FilterSpec aFileWildcard = new FilterSpec(pathWildCard2, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        boolean expResult = false;
        boolean result = instance.overlap(aFileWildcard);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testOverlap4() {
        System.out.println("overlap abc.* and pqr.*");
        String pathWildCard1 = "abc.*";
        String pathWildCard2 = "pqr.*";
        boolean filterDirectories = false;
        boolean filterFiles = true;
        boolean filterSubdirectories = true;
        boolean ignoreCase = true;
        FilterSpec instance = new FilterSpec(pathWildCard1, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        FilterSpec aFileWildcard = new FilterSpec(pathWildCard2, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        boolean expResult = false;
        boolean result = instance.overlap(aFileWildcard);
        assertEquals(expResult, result);
    }

    /**
     * Test of match method, of class FilterSpec.
     */
    @Test
    public void testMatch1() {
        System.out.println("match abc.* and \\\\server\\sub1\\sub2\\abc.xyz");
        String pathWildCard = "abc.*";
        boolean filterDirectories = false;
        boolean filterFiles = true;
        boolean filterSubdirectories = true;
        boolean ignoreCase = true;
        FilterSpec instance = new FilterSpec(pathWildCard, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        String pathFileName = "\\\\server\\sub1\\sub2\\abc.xyz";
        boolean expResult = true;
        boolean result = instance.match(pathFileName);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testMatch2() {
        System.out.println("match abc.* and \\\\server\\sub1\\sub2\\abX.xyz");
        String pathWildCard = "abc.*";
        boolean filterDirectories = false;
        boolean filterFiles = true;
        boolean filterSubdirectories = true;
        boolean ignoreCase = true;
        FilterSpec instance = new FilterSpec(pathWildCard, filterDirectories, filterFiles, filterSubdirectories, ignoreCase);
        String pathFileName = "\\\\server\\sub1\\sub2\\abX.xyz";
        boolean expResult = false;
        boolean result = instance.match(pathFileName);
        assertEquals(expResult, result);
    }

}