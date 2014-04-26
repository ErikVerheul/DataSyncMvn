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
 * @author erik
 */
public class MainFrame_AboutBoxTest {

    public MainFrame_AboutBoxTest() {
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
     * Test if the url to the image file of MainFrame_AboutBox can be obtained.
     */
    @Test
    public void testReadImage() {
        System.out.println("testReadImage");
        ClassLoader cl = MainFrame_AboutBox.class.getProtectionDomain().getClassLoader();
        URL url = cl.getResource("resources/ocean.png");
        assertFalse("The url could not be found", url == null);
    }

//    /**
//     * Test of cancel method, of class MainFrame_AboutBox.
//     */
//    @Test
//    public void testCancel() {
//        System.out.println("cancel");
//        MainFrame_AboutBox instance = null;
//        instance.cancel();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of actionPerformed method, of class MainFrame_AboutBox.
//     */
//    @Test
//    public void testActionPerformed() {
//        System.out.println("actionPerformed");
//        ActionEvent e = null;
//        MainFrame_AboutBox instance = null;
//        instance.actionPerformed(e);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
