package aplicacion.Client;

import javax.swing.JTextArea;

/**
 *
 * @author bruno
 */
public class ConsoleLogger {

    private int logLevel;

    public static final int LEVEL_ERROR = 0; // Flag for logging error messages
    public static final int LEVEL_WARNING = 1; // Flag for logging warning and error messages
    public static final int LEVEL_INFO = 2; // Flag for logging all kind of messages

    private final JTextArea textArea; // text area for logging messages

    /**
     * Set's log level
     *
     * @param logLevel kind of log level
     */
    public void setLevel(final int logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * INIT component, by default log level is info
     *
     * @param textArea output
     */
    public ConsoleLogger(JTextArea textArea) {
        this.logLevel = ConsoleLogger.LEVEL_INFO;
        this.textArea = textArea;
    }

    /**
     * Log info messages
     *
     * @param message
     */
    public void info(String message) {
        if (this.logLevel >= ConsoleLogger.LEVEL_INFO) {
            this.textArea.append("[INFO]: " + message + "\n");
        }
    }

    /**
     * Log error messages
     *
     * @param message
     */
    public void warning(String message) {
        if (this.logLevel >= ConsoleLogger.LEVEL_WARNING) {
            this.textArea.append("[WARNING]: " + message + "\n");
        }
    }

    /**
     * Log warning messages
     *
     * @param message
     */
    public void error(String message) {
        if (this.logLevel >= ConsoleLogger.LEVEL_ERROR) {
            this.textArea.append("[ERROR]: " + message + "\n");
        }
    }

}
