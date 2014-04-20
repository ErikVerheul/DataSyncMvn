/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.verheulconsultants.datasyncmvn;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
public class FindHelpFileTest {

    public FindHelpFileTest() {
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

    @Test
    public void findHelpFile() throws URISyntaxException {
        // get the directory where the application was started
        // cd to the app directory first, then invoke
        File directory = new File(".");
        try {
            String path = directory.getCanonicalPath();
            System.out.println("Current directory's canonical path: " + path);
            // check if on Jenkins build server
            if (path.endsWith("workspace")) path = path + System.getProperty("file.separator") + "dist";
            path = path + System.getProperty("file.separator") + "Help.html";
            assertTrue("The help file does not exist on this location", new File(path).exists());
            if (new File(path).exists()) {
                URI uri = new File(path).toURI();
                System.out.println("URI = " + uri);
            }
        } catch (IOException e) {
            System.out.println("Exception = " + e.getMessage());
        }
    }
}
