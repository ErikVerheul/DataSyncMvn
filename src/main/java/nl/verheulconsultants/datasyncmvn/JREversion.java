package nl.verheulconsultants.datasyncmvn;

/**
 * @author erik
 * Helper class to detail the java.version
 */
class JREversion {
    //e.g. "1.4.2_03"
    String version; 
    int major;
    int medium;
    int minor;
    int patch;

    JREversion() {
        String majorS;
        String mediumS;
        String minorS;
        String patchS;
        version = System.getProperty("java.version");
        int firstDot = version.indexOf('.');
        int secDot = version.lastIndexOf('.');
        int undersc = version.lastIndexOf('_');
        majorS = version.substring(0, firstDot).trim();
        mediumS = version.substring(firstDot + 1, secDot).trim();
        if (undersc > -1) {
            minorS = version.substring(secDot + 1, undersc).trim();
            patchS = version.substring(undersc + 1).trim();
        } else {
            minorS = version.substring(secDot + 1).trim();
            patchS = "0";
        }
        major = Integer.valueOf(majorS);
        medium = Integer.valueOf(mediumS);
        minor = Integer.valueOf(minorS);
        patch = Integer.valueOf(patchS);
    }

    /**
     * Test for the JRE to be "1.4.2_03" or higher
     * @return true if correct
     */
    public boolean checkVersion() {
        return !(major < 1
                || medium < 4
                || medium == 4 && minor < 2
                || medium == 4 && minor == 2 && patch < 3);
    }

    @Override
    public String toString() {
        return version;
    }
}
