package nl.verheulconsultants.datasyncmvn;

import org.apache.log4j.Logger;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import javax.swing.*;

/**
 * A set of static methods used as routines.
 */
class Routines {

    private static final Logger LOGGER = Logger.getLogger(Routines.class.getName());

    /**
     * Open default logFile file in user.home
     *
     * @return the logFile File
     */
    static File openDefaultLogFile() {
        String separator = System.getProperty("file.separator");
        String userHome = System.getProperty("user.home");
        return new File(userHome + separator + "DataSync_default.log");
    }

    /**
     * Centers and shows (setVisable) a frame
     *
     * @param frame the frame
     */
    static void centerAndShowFrame(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
    }

    static File openResourceFile(String name) {
        URL url = Routines.class.getProtectionDomain().getClassLoader().getResource(name);
        File file;
        // see http://weblogs.java.net/blog/2007/04/25/how-convert-javaneturl-javaiofile
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        }
        return file;
    }

    /**
     * Convenience method for the startsWith function Takes the ignoreCase
     * setting into account
     *
     * @param s the string
     * @param start the start string to check
     * @param ignoreCase
     * @return true if S starts with start
     */
    static boolean startsWith(String s, String start, boolean ignoreCase) {
        if (ignoreCase) {
            return s.toLowerCase().startsWith(start.toLowerCase());
        } else {
            return s.startsWith(start);
        }
    }

    /**
     * Convenience method to compare to strings for equality
     *
     * @param s1 one string
     * @param s2 another string
     * @param ignoreCase
     * @return true if both strings are equal
     */
    static boolean stringsEqual(String s1, String s2, boolean ignoreCase) {
        if (ignoreCase) {
            return s1.compareToIgnoreCase(s2) == 0;
        } else {
            return s1.compareTo(s2) == 0;
        }
    }

    /**
     * Check if the name is a UNC name without a trailing \
     *
     * @param name the UNC name
     * @return true if OK
     */
    static boolean simpleUNCcheck(String name) {
        return name.startsWith("\\\\") && (name.indexOf("\\\\", 2) == -1)
                && !name.endsWith("\\");
    }

    /**
     * Check if two files are from the same server
     *
     * @param name1 a file UNC name
     * @param name2 a file UNC name
     * @param ignoreCase
     * @return true if from the same server
     */
    static boolean sameServer(String name1, String name2, boolean ignoreCase) {
        //position 0 and 1 hold \\
        int pos1 = name1.indexOf('\\', 2);
        //position 0 and 1 hold \\
        int pos2 = name2.indexOf('\\', 2);
        return stringsEqual(name1.substring(0, pos1), name2.substring(0, pos2), ignoreCase);
    }

    /**
     * Check if two files are from the same server
     *
     * @param file1 a file
     * @param file2 a file
     * @param ignoreCase
     * @return true if from the same server
     */
    static boolean sameServer(File file1, File file2, boolean ignoreCase) {
        return sameServer(file1.getPath(), file2.getPath(), ignoreCase);
    }

    /**
     * returns bytes under 10MB, MB (rounded) otherwise
     *
     * @param size the size value in bytes
     * @return a String of the value with a trailing MB or bytes
     */
    static String makeVolumeStr(long size) {
        String str;
        if (size > 10_000_000L) {
            str = " " + (size + 499_999L) / 1_000_000L + " MB";
        } else {
            str = " " + size + " bytes";
        }
        return str;
    }

    /**
     * Put this thread to sleep for ms miliseconds Log (warning) the event that
     * the sleep was interrupted
     *
     * @param ms the sleep time
     */
    static void waitMilis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LOGGER.warn("waitMilis is interrupted");
        }
    }

    /**
     * Reapply a Read-only attribute Log if successful and when failed (severe
     * error).
     *
     * @param file
     */
    static void reapplyReadOnly(File file) {
        if (file.setReadOnly()) {
            LOGGER.info("Read-only attribuut op file {0} is hersteld " + file.getPath());
        } else {
            LOGGER.fatal("Kan Read-only attribuut van file " + file.getPath() + " niet herstellen");
        }
    }

//file & directory access check routines =============================================================================
    /**
     * Replacement of File.canAccessDirOrReadFromFile() which can not be trusted
     * on NTFS. Checks read-access only by opening the file for read access,
     * then closing. Bug fixed in JRE 1.4.2_03, files with no access permission
     * are no longer deleted under NTFS! Logs (as severe) errors when reading is
     * not possible
     *
     * @param file the File to test;
     * @param subject the File usage (Bron or Bestemming)
     * @return true if read access possible
     */
    static boolean canReadFromFile(File file, String subject) {
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.close();
            return true;
        } catch (IOException e) {
            LOGGER.fatal("Geen leestoegang tot " + subject + " file " + file.getPath() + " ,error = " + e.toString());
            return false;
        }
    }

    /**
     * Tries to remove the Read-only bit from file.
     *
     * @param file file to remove read-only bit
     * @return true if successful
     */
    static boolean removeReadOnly(File file, String subject) {
        if (!canReadFromFile(file, subject)) {
            return false;
        }

        if (!DataSync.runningWindows) {
            if (file.setReadable(true)) {
                LOGGER.info("Read-only attribuut van file " + file.getPath() + " is verwijderd");
                return true;
            }
            return false;
        } else {
            try {
                Process proc = Runtime.getRuntime().exec("attrib.exe " + file.getPath() + " -r");
                proc.waitFor();
                LOGGER.info("Read-only attribuut van file " + file.getPath() + " is verwijderd met attrib.exe");
                return true;
            } catch (IOException | InterruptedException e) {
                LOGGER.fatal("Kan read-only attribuut niet verwijderen omdat attrib.exe " + file.getPath() + " -r" + " niet start. Fout = " + e.toString());
                return false;
            }
        }
    }

    /**
     * Simple test to check if a file can be written. Checks write-access only
     * by opening the file for write (append) access, then closing.
     *
     * @param file
     * @return null if successful or if the file does not exist, the exception
     * otherwise
     */
    private static Exception canWriteTest(File file) {
        if (!file.exists()) {
            return null;
        }
        if (file.isFile()) {
            try {
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.close();
                return null;
            } catch (IOException e) {
                return e;
            }
        } else {
            return new Exception("The file is a directory");
        }
    }

    private static void logWriteToFileError(File file, Exception e) {
        LOGGER.fatal("Geen schrijftoegang tot Bestemming file " + file.getPath() + ", error = " + e.toString());
    }

    /**
     * Checks write-access only by opening the file for write (append) access,
     * then closing. Tries to remove the Read-only attribute if
     * overschrijfReadOnly is set(Windows only). Mark in the Result instance if
     * the Read-only attribute was removed. Logs (as severe) errors when writing
     * is not possible. Logs (as info) the event that the read-only attribute
     * was removed
     *
     * @param file the File to test
     * @param r the Result instance
     * @param overschrijfReadOnly
     * @return true is the file does not exists or does exists and can be
     * written
     */
    static boolean canWriteToFile(File file, Result r, boolean overschrijfReadOnly) {
        String subject = "Bestemming";
        Exception e = canWriteTest(file);
        if (e == null) {
            return true;
        } else {
            if (overschrijfReadOnly) {
                if (removeReadOnly(file, subject)) {
                    r.readOnlyWasReset = true;
                    Exception e2 = canWriteTest(file);
                    if (e2 == null) {
                        return true;
                    } else {
                        logWriteToFileError(file, e2);
                        r.errorCount++;
                        return false;
                    }
                } else {
                    logWriteToFileError(file, e);
                    r.errorCount++;
                    return false;
                }
            } else {
                logWriteToFileError(file, e);
                r.errorCount++;
            }
            return false;
        }
    }

    /**
     * Checks if a file list can be retrieved from a directory Logs failure
     * (severe)
     *
     * @param dir the directory to test
     * @param subject the usage of the directory (Bron or Bestemming)
     * @return true if a file list can be retrieved
     */
    static boolean canAccessDir(File dir, String subject) {
        if (dir.list() != null) {
            return true;
        } else {
            LOGGER.fatal("Geen toegang tot " + subject + " directory " + dir.getPath());
            return false;
        }
    }

    /**
     * Checks if a file can be created under the directory (creates a dummy .tmp
     * file, then removes it) Logs failure (severe)
     *
     * @param dir directory under test
     * @param subject the usage of the directory (Bron or Bestemming)
     * @return true if a file can be created under the directory
     */
    static boolean canWriteInDir(File dir, String subject) {
        try {
            File tmpFile = java.io.File.createTempFile("gaklFbA6FsWU", null, dir);
            if (!tmpFile.delete()) {
                LOGGER.fatal("Kon tijdelijke file " + tmpFile + " niet verwijderen");
            }
            return true;
        } catch (IOException e) {
            LOGGER.fatal("Kan niet schrijven in " + subject + " directory " + dir.getPath() + ", error = " + e.toString());
            return false;
        }
    }

    /**
     * Convenience method to test is an directory can be accessed or a file can
     * be read. note: file.exists() is implicitly tested by file.isDirectory()
     * en file.isFile()
     *
     * @param file A directory or file
     * @param subject The usage of the directory of file (source or target)
     * @return True if the directory can be accessed or the file can be read
     */
    static boolean canAccessDirOrReadFromFile(File file, String subject) {
        if (file.isDirectory() && canAccessDir(file, subject)) {
            return true;
        }
        return file.isFile() && canReadFromFile(file, subject);
    }

    /**
     * Remove all files and subdirectories from a directory. Count the warnings
     * when a file of subdirectory can not be removed as error.
     *
     * @param dir
     * @param r the result with the error count updated field.
     * @return false if the directory is a file or an I/O error occurs. True
     * otherwise.
     */
    static boolean cleanDirectory(File dir, Result r) {
        if (dir.isFile()) {
            LOGGER.fatal(dir + " is geen directory, maar een file.");
            return false;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            LOGGER.fatal("Kan de directory " + dir + " niet openen.");
            return false;
        }
        LOGGER.info(files.length + " Bestanden en subdirectories worden verwijderd van directory " + dir);
        for (File file : files) {
            if (file.isDirectory()) {
                cleanDirectory(file, r);
                if (file.delete()) {
                    r.targetFilesDeleted++;
                } else {
                    LOGGER.warn("Kan de subdirectory " + file + " niet verwijderen.");
                    r.errorCount++;
                }
            } else {
                if (file.delete()) {
                    r.targetFilesDeleted++;
                } else {
                    LOGGER.warn("Kan file " + file + " niet verwijderen.");
                    r.errorCount++;
                }
            }
        }
        return true;
    }

    /**
     * Calculate the delay to needed to limit the transfer speed to
     * maxBandWidth. Assume the actual transfer of the data takes 200 ms
     * (Windows OS).
     *
     * @param maxBandWidth the maximum transfer average speed in Bytes/sec or 0
     * when unlimited.
     * @param blockSize the size of the file or part of it.
     * @return the calculated delay in ms. to limit the speed.
     */
    static long calcDelay(long maxBandWidth, long blockSize) {
        long assumedTranferTime = 200;
        long delay = blockSize * 1_000 / maxBandWidth;
        if (delay > assumedTranferTime) {
            return delay - assumedTranferTime;
        } else {
            return 0;
        }
    }

    /**
     * Fast copy using NIO. This action can be interrupted by the user.
     *
     * @param thisTask
     * @param source the file to be copied
     * @param target the destination file
     * @param maxBandWidth
     * @return true if no error
     */
    static boolean doFastCopy(File source, File target, long maxBandWidth) {
        long orgDate = source.lastModified();
        // Limit the copy size to 5 MB to prevent the 'Cannot map' or 'Insufficient system resources' exception.
        // 5 MB turned out a good value for a smooth copying under Windows.
        // Change this value for opimal results in your environment.
        long bufSize = 5_000_000L;
        long position = 0L;
        long blockSize;
        try (FileChannel in = new FileInputStream(source).getChannel(); FileChannel out = new FileOutputStream(target).getChannel()) {
            // Getting file channels                     
            if (in.size() <= bufSize) {
                blockSize = in.size();
            } else {
                blockSize = bufSize;
            }
            while (!MainFrame.stopOnmiddellijk && position < in.size()) {
                // Note that if the given position is greater than the file's current size then no bytes are transferred.
                in.transferTo(position, blockSize, out);
                position += blockSize;
                // Limit the bandwidth if requested (> 0).
                if (maxBandWidth > 0) {
                    waitMilis(calcDelay(maxBandWidth, blockSize));
                }
            }
            in.close();
            out.close();
            if (MainFrame.stopOnmiddellijk) {
                // Delete what is written as target.
                if (target.delete()) {
                    return true;
                } else {
                    LOGGER.fatal("Kan de target file " + target + " niet verwijderen");
                    return false;
                }
            } else {
                // Retain the 'Last modified' date.
                if (target.setLastModified(orgDate)) {
                    return true;
                } else {
                    LOGGER.fatal("Kan 'Last Modified' datum van file " + target + " niet terugzetten");
                    return false;
                }
            }
        } catch (IOException e) {
            LOGGER.fatal("Fout bij kopieren " + source.getPath() + " naar " + target.getPath() + " error  " + e.toString());
            return false;
        }
    }

    // Prevent this class from being instantiated
    private Routines() {

    }
}
