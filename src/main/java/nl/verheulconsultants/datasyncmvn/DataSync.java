package nl.verheulconsultants.datasyncmvn;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.Properties;
import javax.swing.*;

/**
 * Versie 1.6: geconverteerd naar Java 1.8
 *
 * Title: DataSync. Description: Data synchronisation tool. Copyright: Copyright
 * Verheul Consultants (c) 2004-2016. Contact erik@verheulconsultants.nl.
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
 * Overige functies: Kopieren van geselecteerde directories met inhoud. Kopieren
 * van files op filenaam met behulp van wildcard. Kopieren van files van
 * verschillende bron directories naar een bestemmingsdirectorie. Verwijderen
 * van gekopieerde of oudere bron files
 *
 * @author Erik Verheul <erik@verheulconsultants.nl> Verheul
 */
public class DataSync {

    static String DELIMITER = ";";
    static String COMMENT = "!";
    static String EMPTYFIELD = "_";
    static File logFile;
    static boolean runningWindows;
    private static final Logger LOGGER = Logger.getLogger(DataSync.class.getName());

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.warn("Kan systeem look-en-feel niet instellen. Fout: ", e);
        }
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(() -> {
            new DataSync();
        });
    }
    boolean packFrame = true;

    /**
     * Construct the application
     */
    @SuppressWarnings(value = {"DM_EXIT", "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
    public DataSync() {
        runningWindows = System.getProperty("os.name").startsWith("Windows");
        MainFrame frame = new MainFrame();
        //Validate frames that have preset sizes
        //Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            frame.pack();
        } else {
            frame.validate();
        }
        Routines.centerAndShowFrame(frame);

        // get the default logfile
        Properties prop = new Properties();

        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("log4j.properties")) {
            // load a properties file
            prop.load(input);
            // get the property value
            logFile = new File(prop.getProperty("log4j.appender.file.File"));
        } catch (IOException ex) {
            ex.printStackTrace(); //NOSONAR
        }
        LOGGER.info("Applicatie is gestart met default log file " + logFile.getPath());
    }
}
