package nl.verheulconsultants.datasyncmvn;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.log4j.Logger;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import javax.swing.*;
import org.apache.log4j.RollingFileAppender;

/**
 * The GUI interface
 */
class MainFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
    //Use the console to select for serious debugging
    static boolean debugMode = false;
    static boolean overschrijfReadOnly = false;
    static boolean checkAccessToBronAndBestemming = true;
    static boolean stopOnmiddellijk = false;
    static boolean stopNaFile = false;
    static boolean stopNaDirectory = false;
    static boolean stopNaBron = false;
    private boolean validatieOK;
    private File mappingTabel = null;
    private Task task = null;
    private boolean doCompare = false;
    private boolean doSync = false;
    private boolean threadIsRunning = false;
    JPanel contentPane;
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenuFile = new JMenu();
    JMenuItem jMenuItemFileExit = new JMenuItem();
    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();
    JTextArea statusBar = new JTextArea();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanelActies = new JPanel();
    JTextField jTextFieldMappingTabel = new JTextField();
    JLabel jLabelMappingTabel = new JLabel();
    GridLayout grid4x1 = new GridLayout(4, 1);
    JCheckBox jCheckBoxVergelijk = new JCheckBox();
    JCheckBox jCheckBoxKopieer = new JCheckBox();
    JMenu jMenuOpties = new JMenu();
    JMenuItem jMenuItemLogFile = new JMenuItem();
    JMenuItem jMenuItemBekijkLog = new JMenuItem();
    JMenuItem jMenuItemLaadMappingTabel = new JMenuItem();
    JCheckBoxMenuItem jCheckBoxMenuItemCheckDebugMode = new JCheckBoxMenuItem();
    JButton startKnop = new JButton();
    JMenuItem jMenuItemDelimiter = new JMenuItem();
    JCheckBoxMenuItem jCheckBoxMenuItemCheckAccess = new JCheckBoxMenuItem();
    JMenuItem jMenuItemToelichting = new JMenuItem();
    JCheckBoxMenuItem jCheckBoxMenuItemOverschrijfReadOnly = new JCheckBoxMenuItem();
    String dialogFont = "Dialog";

    //Construct the frame
    MainFrame() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        jbInit();
    }

    //Component initialization
    private void jbInit() {
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(borderLayout1);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setSize(new Dimension(800, 600));
        this.setTitle("DataSync ");
        statusBar.setBackground(Color.pink);
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        statusBar.setDoubleBuffered(false);
        statusBar.setEditable(false);
        statusBar.setLineWrap(true);
        jMenuFile.setText("Bestand");
        jMenuItemFileExit.setActionCommand("File");
        jMenuItemFileExit.setText("Exit");
        jMenuItemFileExit.addActionListener(new MainFrame_jMenuItemFileExit_ActionAdapter(this));
        jMenuHelp.setText("Help");
        jMenuHelpAbout.setText("About");
        jMenuHelpAbout.addActionListener(new MainFrame_jMenuHelpAbout_ActionAdapter(this));
        jTextFieldMappingTabel.setFont(new java.awt.Font(dialogFont, 0, 12));
        jTextFieldMappingTabel.setPreferredSize(new Dimension(600, 20));
        jTextFieldMappingTabel.setText("niet geladen");
        jTextFieldMappingTabel.addActionListener(new MainFrame_jTextFieldMappingTabel_actionAdapter(this));
        jTextFieldMappingTabel.setEditable(true);
        jLabelMappingTabel.setFont(new java.awt.Font(dialogFont, 0, 20));
        jLabelMappingTabel.setText("Mapping tabel:");
        jPanelActies.setAlignmentX((float) 0.5);
        jPanelActies.setBorder(BorderFactory.createLoweredBevelBorder());
        jPanelActies.setMaximumSize(new Dimension(32_767, 150));
        jPanelActies.setMinimumSize(new Dimension(50, 150));
        jPanelActies.setOpaque(false);
        jPanelActies.setPreferredSize(new Dimension(800, 150));
        jPanelActies.setLayout(grid4x1);
        jCheckBoxVergelijk.setFont(new java.awt.Font(dialogFont, 0, 20));
        jCheckBoxVergelijk.setMinimumSize(new Dimension(50, 34));
        jCheckBoxVergelijk.setPreferredSize(new Dimension(300, 34));
        jCheckBoxVergelijk.setText(" Vergelijk Bronnen met Bestemmingen");
        jCheckBoxVergelijk.addActionListener(new MainFrame_jCheckBoxVergelijk_actionAdapter(this));
        jCheckBoxKopieer.setFont(new java.awt.Font(dialogFont, 0, 20));
        jCheckBoxKopieer.setMinimumSize(new Dimension(50, 34));
        jCheckBoxKopieer.setText(" Kopieer nieuwe files van Bronnen naar Bestemmingen");
        jCheckBoxKopieer.addActionListener(new MainFrame_jCheckBoxKopieer_actionAdapter(this));
        jMenuOpties.setText("Opties");
        jMenuItemLogFile.setToolTipText("Kies een andere naam en locatie");
        jMenuItemLogFile.setText("Log file...");
        jMenuItemLogFile.addActionListener(new MainFrame_jMenuItemLogFile_actionAdapter(this));
        jMenuItemLaadMappingTabel.setText("Laad mapping tabel...");
        jMenuItemLaadMappingTabel.addActionListener(new MainFrame_jMenuItemLaadMappingTabel_actionAdapter(this));
        jMenuItemBekijkLog.setText("Bekijk log file");
        jMenuItemBekijkLog.addActionListener(new MainFrame_jMenuItemBekijkLog_actionAdapter(this));
        jCheckBoxMenuItemCheckDebugMode.setToolTipText("Check voor informatie over iedere vergelijking");
        jCheckBoxMenuItemCheckDebugMode.setText("Debug mode");
        jCheckBoxMenuItemCheckDebugMode.setState(debugMode);
        jCheckBoxMenuItemCheckDebugMode.addActionListener(new MainFrame_jCheckBoxMenuItemCheckDebugMode_actionAdapter(this));
        startKnop.setEnabled(true);
        startKnop.setFont(new java.awt.Font(dialogFont, 0, 14));
        startKnop.setFocusPainted(true);
        startKnop.setMnemonic('0');
        startKnop.setText("Start acties");
        startKnop.addActionListener(new MainFrame_jButtonStart_actionAdapter(this));
        jMenuItemDelimiter.setToolTipText("Kies de zelfde delimiter als gebruikt door het spreadsheet programma");
        jMenuItemDelimiter.setText("Verander mapping tabel delimiter...");
        jMenuItemDelimiter.addActionListener(new MainFrame_jMenuItemDelimiter_actionAdapter(this));
        JScrollPane sp = new JScrollPane(statusBar);
        sp.setPreferredSize(new Dimension(800, 500));
        sp.setMinimumSize(new Dimension(50, 150));
        jCheckBoxMenuItemCheckAccess.setToolTipText("Uncheck om mapping tabel off-line te controleren");
        jCheckBoxMenuItemCheckAccess.setSelected(true);
        jCheckBoxMenuItemCheckAccess.setText("Controleer toegang tot Bron en Bestemming");
        jCheckBoxMenuItemCheckAccess.addActionListener(new MainFrame_jCheckBoxMenuItemCheckAccess_actionAdapter(this));
        jMenuItemToelichting.setText("Toelichting");
        jMenuItemToelichting.addActionListener(new MainFrame_jMenuItemToelichting_actionAdapter(this));
        jCheckBoxMenuItemOverschrijfReadOnly.setText("Overschrijf Read_only bestemmingen");
        jCheckBoxMenuItemOverschrijfReadOnly.addActionListener(new MainFrame_jCheckBoxMenuItemOverschrijfReadOnly_actionAdapter(this));
        jMenuFile.add(jMenuItemLaadMappingTabel);
        jMenuFile.add(jMenuItemBekijkLog);
        jMenuFile.add(jMenuItemFileExit);
        jMenuHelp.add(jMenuItemToelichting);
        jMenuHelp.add(jMenuHelpAbout);
        jMenuBar1.add(jMenuFile);
        jMenuBar1.add(jMenuOpties);
        jMenuBar1.add(jMenuHelp);
        this.setJMenuBar(jMenuBar1);

        JPanel twoItems = new JPanel();
        twoItems.setMaximumSize(new Dimension(32_767, 40));
        twoItems.setPreferredSize(new Dimension(745, 40));
        twoItems.add(jLabelMappingTabel);
        twoItems.add(jTextFieldMappingTabel);
        jPanelActies.add(twoItems);
        jPanelActies.add(jCheckBoxKopieer);
        jPanelActies.add(jCheckBoxVergelijk);
        jPanelActies.add(startKnop);
        contentPane.add(jPanelActies, BorderLayout.NORTH);
        contentPane.add(sp, BorderLayout.CENTER);
        jMenuOpties.add(jMenuItemLogFile);
        jMenuOpties.add(jCheckBoxMenuItemCheckDebugMode);
        jMenuOpties.add(jMenuItemDelimiter);
        jMenuOpties.add(jCheckBoxMenuItemCheckAccess);
        jMenuOpties.add(jCheckBoxMenuItemOverschrijfReadOnly);
    }

    //File | Exit action performed
    void jMenuItemFileExit_actionPerformed(ActionEvent e) {
        if (threadIsRunning) {
            JOptionPane.showMessageDialog(this,
                    "Kan het programma niet afsluiten omdat het nog "
                    + "\nbezig is een of meerdere taken uit te voeren die "
                    + "\neerst moeten worden be�indigd.",
                    "Programma is bezig",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            System.exit(0); //NOSONAR
        }
    }

    //Help | About action performed
    void jMenuHelpAbout_actionPerformed(ActionEvent e) {
        MainFrame_AboutBox dlg = new MainFrame_AboutBox(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.setVisible(true);
    }

    //Overridden so we can exit when window is closed
    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            jMenuItemFileExit_actionPerformed(null);
        }
    }

    void jMenuItemLaadMappingTabel_actionPerformed(ActionEvent e) {
        validatieOK = true;
        if (!threadIsRunning) {
            JFileChooser fs = new JFileChooser();
            fs.setApproveButtonText("Selecteer deze mapping tabel");
            fs.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fs.setMultiSelectionEnabled(false);
            fs.setDialogTitle("Selecteer een mapping tabel van disk (een .CSV file)");
            int returnVal = fs.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                mappingTabel = fs.getSelectedFile();
                jTextFieldMappingTabel.setText(mappingTabel.getPath());
                task = new Task(mappingTabel, statusBar);
                if (!task.leesTabel()) {
                    statusBar.append("Fout(en)in mapping tabel. Herstel en probeer opnieuw\n");
                    validatieOK = false;
                }
            }
        } else {
            statusBar.append("Kan geen nieuwe tabel laden terwijl vorige tabel wordt verwerkt.\n"
                    + "Wacht tot lopende taak is be�indigd.\n");
        }
    }

    void jMenuItemBekijkLog_actionPerformed(ActionEvent e) {
        if (DataSync.runningWindows) {
            try {
                Runtime.getRuntime().exec("notepad.exe" + " " + DataSync.logFile.getPath());
            } catch (java.io.IOException e2) {
                statusBar.append("Kan notepad niet starten" + " Fout = " + e2.toString());
            }
        } else {
            statusBar.append("Sorry, kan geen editor starten op een niet Windows OS");
        }
    }

    void jMenuItemLogFile_actionPerformed(ActionEvent e) {
        String name = JOptionPane.showInputDialog(this,
                "Vul de naam van logfile hier in :",
                DataSync.logFile.getName());
        //if cancel was not clicked
        if (name != null && (name.length()) > 0) {
            JFileChooser fs = new JFileChooser();
            fs.setApproveButtonText("Selecteer deze directory");
            fs.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fs.setMultiSelectionEnabled(false);
            fs.setDialogTitle("Selecteer de directory voor de log file");
            int returnVal = fs.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String newLogPath = fs.getSelectedFile().getPath() + System.getProperty("file.separator") + name;
                if (!Routines.stringsEqual(newLogPath, DataSync.logFile.getPath(), DataSync.runningWindows)) {
                    LOGGER.info("Log file veranderd van " + DataSync.logFile.getPath() + " naar " + newLogPath);
                    RollingFileAppender appndr = (RollingFileAppender)Logger.getRootLogger().getAppender("file");
                    appndr.setFile(newLogPath);
                    appndr.activateOptions();
                    DataSync.logFile = new File(newLogPath);
                    LOGGER.info("Start logging...");
                }
            }
        }
    }

    @SuppressWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    void jCheckBoxMenuItemCheckDebugMode_actionPerformed(ActionEvent e) {
        debugMode = jCheckBoxMenuItemCheckDebugMode.isSelected();
    }

    void jTextFieldMappingTabel_actionPerformed(ActionEvent e) {
        statusBar.append("Selecteer een nieuwe mapping tabel met Bestand|Laad mapping tabel\n");
    }

    void jCheckBoxVergelijk_actionPerformed(ActionEvent e) {
        doCompare = jCheckBoxVergelijk.isSelected();
    }

    void jCheckBoxKopieer_actionPerformed(ActionEvent e) {
        doSync = jCheckBoxKopieer.isSelected();
    }

    private void warning(String s) {
        LOGGER.warn(s);
        statusBar.append(s + "\n");
    }

    /**
     * Create the GUI and show it. For thread safety, this method is invoked
     * from the event-dispatching thread.
     *
     * @return progressWindow the JTextArea to show running actions
     */
    private JTextArea createAndShowRunningThread() {
        //Create and set up the window.
        JFrame threadFrame = new JFrame("DataSync running thread");
        JTextArea progressWindow = new MyJTextArea();
        threadFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Container contentContainer = threadFrame.getContentPane();
        contentContainer.setLayout(new BorderLayout());

        JPanel center = new JPanel(new FlowLayout());

        JButton stopOnmiddellijkButton = new JButton("STOP onmiddellijk");
        stopOnmiddellijkButton.setFont(new java.awt.Font(dialogFont, 0, 20));
        stopOnmiddellijkButton.setToolTipText("Stop onmiddellijk, ook als een file slechts deels gekopieerd is");
        stopOnmiddellijkButton.addActionListener(this::jStop1_actionPerformed);
        center.add(stopOnmiddellijkButton);
        JButton stopNaFileButton = new JButton("STOP na File");
        stopNaFileButton.setFont(new java.awt.Font(dialogFont, 0, 20));
        stopNaFileButton.setToolTipText("Stop nadat de file gekopieerd is");
        stopNaFileButton.addActionListener(this::jStop2_actionPerformed);
        center.add(stopNaFileButton);
        JButton stopNaDirectoryButton = new JButton("STOP na Directory");
        stopNaDirectoryButton.setFont(new java.awt.Font(dialogFont, 0, 20));
        stopNaDirectoryButton.setToolTipText("Stop nadat met de Bron subdirectory verwerkt is");
        stopNaDirectoryButton.addActionListener(this::jStop3_actionPerformed);
        center.add(stopNaDirectoryButton);
        JButton stopNaBronButton = new JButton("STOP na Bron");
        stopNaBronButton.setFont(new java.awt.Font(dialogFont, 0, 20));
        stopNaBronButton.setToolTipText("Stop nadat de Bron 'root' verwerkt is");
        stopNaBronButton.addActionListener(this::jStop4_actionPerformed);
        center.add(stopNaBronButton);
        contentContainer.add(center, BorderLayout.NORTH);
        progressWindow.setFont(new java.awt.Font(dialogFont, 0, 12));
        progressWindow.setText("Thread is gestart\n"
                + "In dit window worden maximaal 60.000 regels getoond met\n"
                + "een maximum van ongeveer 1000 regels per opdrachtregel (tuple)\n\n");
        JScrollPane sp = new JScrollPane(progressWindow);
        sp.setPreferredSize(new Dimension(800, 200));
        contentContainer.add(sp, BorderLayout.CENTER);
        threadFrame.pack();
        Routines.centerAndShowFrame(threadFrame);
        return progressWindow;
    }

    // show program stops is any; then show user stops.
    private void showProgramAndUserStops(Result r) {
        switch (r.stopByProgram) {
            case Result.STOP_IMMEDIATELY:
                statusBar.append("'STOP onmiddellijk' door programma in " + r.stopLocation + "\n");
                break;
            case Result.STOP_AFTER_FILE:
                statusBar.append("'STOP na File' door programma na " + r.stopLocation + "\n");
                break;
            case Result.STOP_AFTER_DIRECTORY:
                statusBar.append("'STOP na Directory' door programma na " + r.stopLocation + "\n");
                break;
            case Result.STOP_AFTER_SOURCE:
                statusBar.append("'STOP na bron' door programma na " + r.stopLocation + "\n");
                break;
            default:
                showUserStops(r);
                break;
        }
    }

    private void showUserStops(Result r) {
        switch (r.stopByUser) {
            case Result.STOP_IMMEDIATELY:
                statusBar.append("'STOP onmiddellijk' door gebruiker in " + r.stopLocation + "\n");
                break;
            case Result.STOP_AFTER_FILE:
                statusBar.append("'STOP na File' door gebruiker na " + r.stopLocation + "\n");
                break;
            case Result.STOP_AFTER_DIRECTORY:
                statusBar.append("'STOP na Directory' door gebruiker na " + r.stopLocation + "\n");
                break;
            case Result.STOP_AFTER_SOURCE:
                statusBar.append("'STOP na bron' door gebruiker na " + r.stopLocation + "\n");
                break;
            default:
                LOGGER.warn("Stop message expected but not found");
                break;
        }
    }

    private void logSync(Result r, JTextArea progress) {
        String msg = "Synchronisatie gereed. Aantal vergeleken files " + r.fileCount
                + ", gekopieerd volume " + Routines.makeVolumeStr(r.size) + ".";
        if (r.errorCount > 0) {
            msg = msg + " Er zijn " + r.errorCount + " fouten, zie de log";
        } else {
            msg += " Er zijn geen fouten";
        }
        String s1 = "Nieuwe bron files: " + r.fileCountNewFile
                + ", waarvan gesynchroniseerd: " + r.fileCountNewFileSynct;
        String s2 = "Bron files nieuwer dan bestemming: " + r.fileCountSourceNewer
                + ", waarvan gesynchroniseerd: " + r.fileCountSourceNewerSynct;
        String s3 = "Bestemming files nieuwer dan bron (niet gesynchroniseerd): " + r.fileCountDestNewer;
        String s4 = "Aantal verwijderde bron files: " + r.sourceFilesDeleted;
        LOGGER.info(msg);
        LOGGER.info(s1 + ", " + s2 + ", " + s3 + ", " + s4);
        progress.append(msg + "\n     " + s1 + "\n     " + s2 + "\n     " + s3 + "\n     " + s4 + "\n");
    }

    public void sync() {
        final JTextArea progress = createAndShowRunningThread();
        LOGGER.info("****** Synchronisatie is gestart");
        threadIsRunning = true;
        final SwingWorker worker = new SwingWorker() {
            Result r = null;
            @Override
            public Object construct() {
                r = task.sync(progress);
                //return value not used by this program
                return r;
            }
            @Override
            public void finished() {
                threadIsRunning = false;
                logSync(r, progress);
                if (r.stopByProgram == Result.NOT_STOPPED && r.stopByUser == Result.NOT_STOPPED) {
                    if (doCompare) {
                        //carefull, this is another thread
                        compare();
                    }
                } else {
                    showProgramAndUserStops(r);
                }
            }
        };
        worker.start();
    }

    private void logCompare(Result r, JTextArea progress) {
        String msg = "Vergelijking gereed. Aantal vergeleken files " + r.fileCount
                + ", te kopieren volume " + Routines.makeVolumeStr(r.size) + ".";
        if (r.errorCount > 0) {
            msg = msg + " Er zijn " + r.errorCount + " fouten, zie de log";
        } else {
            msg += " Er zijn geen fouten";
        }
        String s1 = "Nieuwe bron files: " + r.fileCountNewFile
                + ", waarvan mogelijk te synchroniseren: " + r.fileCountNewFileSynct;
        String s2 = "Bron files nieuwer dan bestemming: " + r.fileCountSourceNewer
                + ", waarvan mogelijk te synchroniseren: " + r.fileCountSourceNewerSynct;
        String s3 = "Bestemming files nieuwer dan bron (niet te synchroniseren): " + r.fileCountDestNewer;
        LOGGER.info(msg);
        LOGGER.info(s1 + ", " + s2 + ", " + s3);
        progress.append(msg + "\n     " + s1 + "\n     " + s2 + "\n     " + s3 + "\n");
    }

    public void compare() {
        final JTextArea progress = createAndShowRunningThread();
        LOGGER.info("****** Vergelijking is gestart");
        threadIsRunning = true;
        final SwingWorker worker = new SwingWorker() {
            Result r = null;
            @Override
            public Object construct() {
                r = task.vergelijk(progress);
                //return value not used by this program
                return r;
            }
            @Override
            public void finished() {
                threadIsRunning = false;
                logCompare(r, progress);
                if (!(r.stopByProgram == Result.NOT_STOPPED && r.stopByUser == Result.NOT_STOPPED)) {
                    showProgramAndUserStops(r);
                }
            }
        };
        worker.start();
    }

    /**
     * Starts a new thread with its own frame to execute for each chosen action
     *
     * @param e the press Start button event
     */
    void jButtonStart_actionPerformed(ActionEvent e) {
        if (task != null && validatieOK && !threadIsRunning) {
            //reset stops
            stopOnmiddellijk = false;
            stopNaFile = false;
            stopNaDirectory = false;
            stopNaBron = false;
            if (doSync) {
                //a thread
                sync();
            } else if (doCompare) {
                //a thread
                compare();
            } else {
                statusBar.append("Niets te doen; selecteer een of meerdere acties\n");
            }
        } else {
            if (task == null || !validatieOK) {
                statusBar.append("Eerst valide mapping tabel laden met Bestand|Laden mapping tabel\n"
                        + "Na succes wordt de 'Start' knop geactiveerd\n");
            }
            if (threadIsRunning) {
                statusBar.append("Wacht tot de lopende thread gereed is alvorens een nieuwe te starten\n");
            }
        }
    }

    void jMenuItemDelimiter_actionPerformed(ActionEvent e) {
        String d = JOptionPane.showInputDialog(this,
                "Verander de delimiter in :",
                DataSync.DELIMITER);
        if (d != null) {
            if (d.length() != 1) {
                JOptionPane.showMessageDialog(this,
                        "De delimiter moet 1 karakter lang zijn",
                        "Foute invoer",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                DataSync.DELIMITER = d;
            }
        }
    }

    void jStop1_actionPerformed(ActionEvent e) {
        stopOnmiddellijk = true;
        if (threadIsRunning && stopOnmiddellijk) {
            warning("Stop onmiddellijk geactiveerd");
        }
    }

    void jStop2_actionPerformed(ActionEvent e) {
        stopNaFile = true;
        if (threadIsRunning && stopNaFile) {
            warning("Stop na file geactiveerd");
        }
    }

    void jStop3_actionPerformed(ActionEvent e) {
        stopNaDirectory = true;
        if (threadIsRunning && stopNaDirectory) {
            warning("Stop na Directory geactiveerd");
        }
    }

    void jStop4_actionPerformed(ActionEvent e) {
        stopNaBron = true;
        if (threadIsRunning && stopNaBron) {
            warning("Stop na Bron geactiveerd");
        }
    }

    void jCheckBoxMenuItemCheckAccess_actionPerformed(ActionEvent e) {
        checkAccessToBronAndBestemming = jCheckBoxMenuItemCheckAccess.isSelected();
    }

    private void startBrowser(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException ex) {
            LOGGER.trace("Cannot show help file, exception = {0}", ex);
        }
    }

    private void showHelp() {
        ClassLoader cl = this.getClass().getProtectionDomain().getClassLoader();
        File file = new File("Help.html");
        InputStream link = cl.getResourceAsStream("Help.html");
        try {
            Files.copy(link, file.getAbsoluteFile().toPath(), REPLACE_EXISTING);
            URI uri = file.toURI();
            if (Desktop.isDesktopSupported()) {
                startBrowser(uri);
            } else {
                LOGGER.fatal("Cannot show help file, class Desktop not supported");
            }
        } catch (IOException ex) {
            LOGGER.trace("Cannot show help file, exception = ", ex);
        }
    }

    void jMenuItemToelichting_actionPerformed(ActionEvent e) {
        showHelp();
    }

    void jCheckBoxMenuItemOverschrijfReadOnly_actionPerformed(ActionEvent e) {
        overschrijfReadOnly = jCheckBoxMenuItemOverschrijfReadOnly.isSelected();
    }
}

class MainFrame_jMenuItemFileExit_ActionAdapter implements ActionListener {

    MainFrame adaptee;

    MainFrame_jMenuItemFileExit_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemFileExit_actionPerformed(e);
    }
}

class MainFrame_jMenuHelpAbout_ActionAdapter implements ActionListener {

    MainFrame adaptee;

    MainFrame_jMenuHelpAbout_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuHelpAbout_actionPerformed(e);
    }
}

class MainFrame_jMenuItemLaadMappingTabel_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jMenuItemLaadMappingTabel_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemLaadMappingTabel_actionPerformed(e);
    }
}

class MainFrame_jMenuItemBekijkLog_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jMenuItemBekijkLog_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemBekijkLog_actionPerformed(e);
    }
}

class MainFrame_jMenuItemLogFile_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jMenuItemLogFile_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemLogFile_actionPerformed(e);
    }
}

class MainFrame_jCheckBoxMenuItemCheckDebugMode_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jCheckBoxMenuItemCheckDebugMode_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jCheckBoxMenuItemCheckDebugMode_actionPerformed(e);
    }
}

class MainFrame_jTextFieldMappingTabel_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jTextFieldMappingTabel_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jTextFieldMappingTabel_actionPerformed(e);
    }
}

class MainFrame_jButtonStart_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jButtonStart_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jButtonStart_actionPerformed(e);
    }
}

class MainFrame_jCheckBoxVergelijk_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jCheckBoxVergelijk_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jCheckBoxVergelijk_actionPerformed(e);
    }
}

class MainFrame_jCheckBoxKopieer_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jCheckBoxKopieer_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jCheckBoxKopieer_actionPerformed(e);
    }
}

class MainFrame_jMenuItemDelimiter_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jMenuItemDelimiter_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemDelimiter_actionPerformed(e);
    }
}

class MainFrame_jCheckBoxMenuItemCheckAccess_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jCheckBoxMenuItemCheckAccess_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jCheckBoxMenuItemCheckAccess_actionPerformed(e);
    }
}

class MainFrame_jMenuItemToelichting_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jMenuItemToelichting_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemToelichting_actionPerformed(e);
    }
}

class MainFrame_jCheckBoxMenuItemOverschrijfReadOnly_actionAdapter implements java.awt.event.ActionListener {

    MainFrame adaptee;

    MainFrame_jCheckBoxMenuItemOverschrijfReadOnly_actionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.jCheckBoxMenuItemOverschrijfReadOnly_actionPerformed(e);
    }
}
