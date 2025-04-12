package nl.verheulconsultants.datasyncmvn;

import javax.swing.JTextArea;

/**
 * Subclass of javax.swing.JTextArea
 * Only re-implement the two basic constructors
 * Override append
 */
class MyJTextArea extends JTextArea {

    private static final int MAXLINES = 60_000;

    MyJTextArea() {
        super();
    }

    MyJTextArea(String text) {
        super(text);
    }

    /**
     * Override append with a version which stops appending text when the maximum number of 60000 lines is reached
     * Append 1 more line to warn the user about what happened.
     * Always append a new line after each written line.
     * @param str the text to be appended
     */
    @Override
    public void append(String str) {
        if (this.getLineCount() < MAXLINES) {
            super.append(str + "\n");
        } else if (this.getLineCount() == MAXLINES) {
            super.append("Maximum van " + MAXLINES + " aantal regels bereikt. Stop met weergeven in dit venster. Zie verder in de log\n");
        }
    }
}
