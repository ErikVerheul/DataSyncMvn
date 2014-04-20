package nl.verheulconsultants.datasyncmvn;

import static nl.verheulconsultants.datasyncmvn.DataSync.loggerFileHandler;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class RoutinesTest {

    File tempFile;
    File secretFile;

    public RoutinesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    @SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        File logFile = Routines.openDefaultLogFile();
        //must initiate a logggerFileHandler when calling methods from class Routines which do logging
        loggerFileHandler = Routines.createFileHandler(logFile.getPath());
        tempFile = File.createTempFile("testfile", ".tmp");
    }

    @After
    @SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public void tearDown() {
        tempFile.delete();
        new File("unknown").delete();
    }

    @Test
    public void testSimpleUNCcheck() {
        String unc = "\\\\server1\\share1\\dir1";
        assertTrue(unc + " is ten onrechte afgekeurd als een valide UNC naam", Routines.simpleUNCcheck(unc));
        unc = "\\\\server1\\share1\\dir1";
        assertTrue(unc + " is ten onrechte afgekeurd als een valide UNC naam", Routines.simpleUNCcheck(unc));
        unc = "\\server1\\share1\\dir1";
        assertFalse(unc + " is ten onrechte goedgekeurd als een valide UNC naam", Routines.simpleUNCcheck(unc));
        unc = "\\\\server1/share1\\dir1\\";
        assertFalse(unc + " is ten onrechte goedgekeurd als een valide UNC naam", Routines.simpleUNCcheck(unc));
    }

    @Test
    public void testCanReadAccessFile() {
        assertTrue("Kan " + tempFile + " niet lezen", Routines.canReadFromFile(tempFile, "bron of bestemming"));
        assertFalse("Kan niet bestaande file w�l lezen?", Routines.canReadFromFile(new File("unknown"), "bron of bestemming"));
        secretFile = new File(System.getProperty("java.io.tmpdir"), "secret.txt");
        assertFalse("Toch leestoegang tot deze file van een andere eigenaar??", Routines.canReadFromFile(secretFile, "Bron of Bestemming"));
    }

    @Test
    // make sure an inaccessable file secret.txt is present in the java.io.tmpdir
    public void testCanWriteAccessFile() {
        assertTrue("Kan " + tempFile + " niet schrijven", Routines.canWriteToFile(tempFile, new Result(), false));
        assertTrue("Kan niet bestaande file schrijven?", Routines.canWriteToFile(new File("unknown"), new Result(), false));
        secretFile = new File(System.getProperty("java.io.tmpdir"), "secret.txt");
        assertFalse("Toch schrijftoegang tot deze file van een andere eigenaar?", Routines.canWriteToFile(secretFile, new Result(), false));
    }

    @Test
    public void testRemoveReadOnly() {
        assertTrue("Fout bij read-only attribuut verwijderen als dat attribuut niet is gezet", Routines.removeReadOnly(tempFile, "Bron of Bestemming"));
        if (tempFile.setReadOnly()) {
            assertTrue("Kan read-only attribuut niet verwijderen", Routines.removeReadOnly(tempFile, "Bron of Bestemming"));
        }
        assertFalse("Toch mogelijk read-only attribuut van een niet bestaande file te veranderen?", Routines.removeReadOnly(new File("unknown"), "Bron of Bestemming"));
        secretFile = new File(System.getProperty("java.io.tmpdir"), "secret.txt");
        assertFalse("Toch mogelijk read-only attribuut van deze file zonder toegang te veranderen?", Routines.removeReadOnly(secretFile, "Bron of Bestemming"));
    }

    @Test
    public void testReapplyReadOnly() {
        Routines.reapplyReadOnly(tempFile);
        assertFalse("tempFile is niet read-only gemaakt", tempFile.canWrite());
        Routines.reapplyReadOnly(tempFile);
        assertFalse("fout bij herhaald zetten read-only attribuut", tempFile.canWrite());
        secretFile = new File(System.getProperty("java.io.tmpdir"), "secret.txt");
        Routines.reapplyReadOnly(secretFile); //no exception should be thrown
    }

    @Test
    public void testCanAccessDir() {
        assertTrue("Kan directory niet bereiken die wel bestaat", Routines.canAccessDir(tempFile.getParentFile(), "Bron of Bestemming"));
        assertFalse("Kan directory bereiken die niet bestaat", Routines.canAccessDir(new File("unknown"), "Bron of Bestemming"));
        assertFalse("Kan directory bereiken die een file is?", Routines.canAccessDir(tempFile, "Bron of Bestemming"));
    }

    @Test
    public void testCanWriteDir() {
        assertTrue("Kan niet in directory schrijven die wel bestaat", Routines.canWriteInDir(tempFile.getParentFile(), "Bron of Bestemming"));
        assertFalse("Kan in directory schrijven die niet bestaat", Routines.canWriteInDir(new File("unknown"), "Bron of Bestemming"));
        assertFalse("Kan in directory schrijven die een file is?", Routines.canWriteInDir(tempFile, "Bron of Bestemming"));
    }

    @Test
    public void testCanRead() {
        assertTrue("Kan directory niet lezen", Routines.canAccessDirOrReadFromFile(tempFile.getParentFile(), "Bron of Bestemming"));
        assertTrue("Kan file niet lezen", Routines.canAccessDirOrReadFromFile(tempFile, "Bron of Bestemming"));
        assertFalse("Kan file lezen die niet bestaat", Routines.canAccessDirOrReadFromFile(new File("unknown"), "Bron of Bestemming"));
        secretFile = new File(System.getProperty("java.io.tmpdir"), "secret.txt");
        assertFalse("Toch leestoegang tot deze file zonder toegang?", Routines.canAccessDirOrReadFromFile(secretFile, "Bron of Bestemming"));
    }

    @Test
    public void testCanCleanDir() {
        File testDir = new File(System.getProperty("java.io.tmpdir"), "testDir8443639434");
        if (!testDir.exists()) {
            assertTrue("Kan geen testdirectory aanmaken", testDir.mkdir());
        }
        File testSubDir = new File(testDir, "subDir");
        if (!testSubDir.exists()) {
            assertTrue("Kan geen testsubdirectory aanmaken", testSubDir.mkdir());
        }
        File tempFile0 = null;
        File tempFile1 = null;
        try {
            tempFile0 = File.createTempFile("testfile", null, testDir);
            tempFile1 = File.createTempFile("testfile", null, testSubDir);
        } catch (IOException ex) {
            Logger.getLogger(RoutinesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertTrue("Test files are not created", tempFile0.exists() && tempFile1.exists());
        Result r = new Result();
        Routines.cleanDirectory(testDir, r);
        assertTrue("Test files are not deleted", !tempFile0.exists() && !tempFile1.exists() && !testSubDir.exists());
        assertTrue("The error count is not zero", r.errorCount == 0);
    }
}
