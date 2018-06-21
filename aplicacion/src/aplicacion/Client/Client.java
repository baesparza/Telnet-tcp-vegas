package aplicacion.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 * Source code for telnet client
 *
 * @author bruno
 */
public class Client implements Runnable {

    private int port;
    private String hostname;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader buffer;
    private Thread thread;
    private String encode;

    private JTextArea textArea;
    private boolean connection;

    /**
     * Constructor, set data for connection, initialize variables
     *
     * @param hostname address of TODO
     * @param port of TODO
     * @param encode language for the communication
     * @param textArea input textArea for write messages
     */
    public Client(String hostname, int port, String encode, JTextArea textArea) {
        this.hostname = hostname;
        this.port = port;
        this.encode = encode;
        this.textArea = textArea;

        this.connection = false;
    }

    /**
     * Start this thread
     */
    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Create new socket and add stream input/output that will let us read/write
     * to the socket
     */
    public void open() {
        try {
            this.socket = new Socket(this.hostname, this.port);
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.buffer = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), this.encode));
            
            this.connection = true;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Main loop for application, always listening TODO
     */
    @Override
    public void run() {
        String readLine = "";
        while (true) {
            try {
                readLine = this.buffer.readLine();
                this.receiveMessage(readLine);
                System.out.println(readLine);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * TODO
     *
     * @param message
     */
    public void writeMessage(String message) {
        this.out.println(message + "\n");
    }

    /**
     * TODO
     *
     * @param message
     */
    public void receiveMessage(String message) {
        this.textArea.append(message + "\n");

    }

    /**
     * Safely end application
     */
    public synchronized void stop() {
        try {
            thread.join();
            this.socket.close();
            this.out.close();
            this.buffer.close();
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Getter
     *
     * @return state of connection
     */
    boolean getConnection() {
        return this.connection;
    }

}
