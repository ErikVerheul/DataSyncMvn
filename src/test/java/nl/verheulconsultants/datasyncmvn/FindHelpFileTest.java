/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.verheulconsultants.datasyncmvn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
    public void findHelpFile() {
        System.out.println("findHelpFile");
        ClassLoader cl = this.getClass().getProtectionDomain().getClassLoader();
        
        File file = new File("help.html");
        InputStream link = (cl.getResourceAsStream("Help.html"));
        try {
            Files.copy(link, file.getAbsoluteFile().toPath());
            assertTrue("The help file does not exist", file.exists());
        } catch (IOException ex) {
            System.err.println("Cannot show help file, exception = " + ex.getMessage());
        }
    }
}
