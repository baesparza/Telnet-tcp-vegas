package aplicacion.Client;

import javax.swing.JTextArea;

/**
 *
 * @author bruno
 */
public class ConsoleLogger {

    private int logLevel;
    public static int LevelError = 0;
    public static int LevelWarning = 1;
    public static int LevelInfo = 2;

    private final JTextArea textArea;

    public ConsoleLogger(JTextArea textArea) {
        this.logLevel = ConsoleLogger.LevelInfo;
        this.textArea = textArea;
    }

    public void error(String message) {
        if (this.logLevel >= ConsoleLogger.LevelError) {
            this.textArea.append("[ERROR]: " + message + "\n");
        }
    }

    public void warning(String message) {
        if (this.logLevel >= ConsoleLogger.LevelWarning) {
            this.textArea.append("[WARNING]: " + message + "\n");
        }
    }

    public void info(String message) {
        if (this.logLevel >= ConsoleLogger.LevelInfo) {
            this.textArea.append("[INFO]: " + message + "\n");
        }
    }

}
