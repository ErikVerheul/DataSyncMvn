/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.verheulconsultants.datasyncmvn;

import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Erik Verheul <erik@verheulconsultants.nl>
 */
public class MainFrame_AboutBoxTest {

    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     *
     */
    public MainFrame_AboutBoxTest() {
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
     * Test if the url to the image file of MainFrame_AboutBox can be obtained.
     */
    @Test
    public void testReadImage() {
        System.out.println("testReadImage");
        ClassLoader cl = this.getClass().getProtectionDomain().getClassLoader();
        URL url = cl.getResource("ocean.png");
        assertFalse("The url could not be found", url == null);
    }

}
