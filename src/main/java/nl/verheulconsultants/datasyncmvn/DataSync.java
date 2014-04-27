package nl.verheulconsultants.datasyncmvn;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * Title: DataSync. Description: Data synchronisation tool. Copyright: Copyright
 * Verheul Consultants (c) 2004-2014. Contact erik@verheulconsultants.nl.
 * Customer: Data synchronisation tool voor de ULC Groep b.v.
 *
 * Kopieer functie:
 *
 * Dit programma synchroniseert een bron met een bestemming. Files die in de
 * bestemming niet voorkomen of nieuwer zijn in de bron worden naar de
 * bestemming gekopieerd. Hierbij wordt de file in de bestemming, als die al
 * bestond, overschreven.
 *
 * Criteria: - de lokatie - de file naam - de "modified" datum/tijd.
 *
 * Als een file zich in de zelfde directory bevindt (relatief tot de bron en
 * bestemming roots) en de naam gelijk is, wordt de "modified" time-stamp
 * vergeleken. Is de bron file nieuwer dan wordt deze naar de bestemming
 * gekopieerd. Is de bestemming file nieuwer dan wordt deze alleen met de bron
 * overschreven als de "overschrijf nieuwer" tag in de mapping tabel op WAAR
 * staat. Files die niet in de bestemming voorkomen worden altijd gekopieerd.
 *
 * Vergelijk functie:
 *
 * Deze functie simuleert een kopie run maar kopieert niets. A.d.h.v. de
 * resultaten kan voor een kopieerslag worden bepaald of deze zal slagen en om
 * hoeveel files/data het gaat. Na een kopieerslag kan worden geverifieerd welke
 * files nog niet zijn gekopieerd.
 *
 * Overige functies: Kopieren van geselecteerde directories m�t inhoud Kopieren
 * van files op filenaam met behulp van wildcard Kopieren van files van
 * verschillende bron directories naar ��n bestemmingsdirectorie. Verwijderen
 * van gekopieerde of oudere bron files
 *
 * @author Erik Verheul
 * @version 1.40
 */
public class DataSync {

    boolean packFrame = true;
    static String DELIMITER = ";";
    static String COMMENT = "!";
    static String EMPTYFIELD = "_";
    static File logFile;
    static boolean runningWindows;
    private static final Logger LOGGER = Logger.getLogger(DataSync.class.getName());
    /**
     * the logFileging file handler
     */
    static FileHandler loggerFileHandler;

    //Construct the application
    @SuppressWarnings({"DM_EXIT", "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
    public DataSync() {
        logFile = Routines.openDefaultLogFile();
        //open or create a logFile file
        loggerFileHandler = Routines.createFileHandler(logFile.getPath());
        // Send logger output to our FileHandler.
        LOGGER.addHandler(loggerFileHandler);
        // Request that every detail gets logged.
        LOGGER.setLevel(Level.ALL);
        
        runningWindows = System.getProperty("os.name").startsWith("Windows");
        //test for the JRE to be "1.4.2_03" or higher
        JREversion jREfound = new JREversion();
        if (!jREfound.checkVersion()) {
            JOptionPane.showMessageDialog(null,
                    "U gebruikt de Java Runtime Environment " + jREfound.version
                    + "\nDit programma is getest op JRE versie 1.4.2_03"
                    + "\nGebruik JRE1.4.2_03 of hoger",
                    "Verkeerde Java Runtime Environment",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        MainFrame frame = new MainFrame();
        //Validate frames that have preset sizes
        //Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            frame.pack();
        } else {
            frame.validate();
        }

        Routines.centerAndShowFrame(frame);
        LOGGER.log(Level.INFO, "Applicatie is gestart met default log file {0}", logFile.getPath());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.log(Level.WARNING, "Kan systeem look-en-feel niet instellen. Fout:{0}", e);
        }
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new DataSync();
            }
        });
    }
}
