package aplicacion.Client;

import aplicacion.utils.ConsoleLogger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import javax.swing.JTextArea;

/**
 * Source code for telnet client
 *
 * @author bruno
 */
public class Client implements Runnable {

    private final int port;
    private final InetAddress hostname;
    private DatagramSocket socket;
    private Thread thread;

    private final JTextArea textArea;
    private ConsoleLogger console;
    private boolean connection;

    /**
     * Constructor, set data for connection, initialize variables. Create new
     * socket and add stream input/output that will let us read/write to the
     * socket
     *
     * @param hostname address of TODO
     * @param port of TODO
     * @param textArea input textArea for write messages
     * @param txtConsole TODO
     */
    public Client(InetAddress hostname, int port, JTextArea textArea, JTextArea txtConsole) {
        this.hostname = hostname;
        this.port = port;

        this.console = new ConsoleLogger(txtConsole);
        this.textArea = textArea;

        this.connection = false;
        try {
            this.socket = new DatagramSocket();

            this.connection = true;
        } catch (IOException ex) {
            console.error("Could't initialize socket");
        }
    }

    /**
     * Start this thread
     */
    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Main loop for application, always listening for packages
     */
    @Override
    public void run() {
        while (this.connection) {
            try {
                byte data[] = new byte[100];
                DatagramPacket packetIN = new DatagramPacket(data, data.length);
                socket.receive(packetIN); // wait for packagethis.textArea.append("\nPaquete recibido:"
                this.textArea.append("\nPaquete recibido:"
                        + "\nDe host: " + packetIN.getAddress()
                        + "\nPuerto host: " + packetIN.getPort()
                        + "\nLongitud: " + packetIN.getLength()
                        + "\nContiene:\n\t" + new String(packetIN.getData(), 0, packetIN.getLength()));
            } catch (IOException ex) {
                console.error("Socket recieve packet in");
            }
            System.out.println("running");
        }
    }

    /**
     * TODO
     *
     * @param message
     */
    public void sendMessage(String message) {
        try {

            byte data[] = message.getBytes(); // turn into bytes
            // create package
            DatagramPacket pack = new DatagramPacket(data, data.length, this.hostname, this.port);
            socket.send(pack);
            console.info("Package length is " + pack.getLength() + " Bytes");
            ByteArrayInputStream inStreamBuffer = new ByteArrayInputStream(message.getBytes());
            CheckedInputStream chekedInput = new CheckedInputStream(inStreamBuffer, new Adler32());
            byte readBuffer[] = new byte[5];
            while (chekedInput.read(readBuffer) >= 0) {
                long value = chekedInput.getChecksum().getValue();
                console.info("The value of checksum is " + value);
            }
        } catch (IOException e) {
            console.error("An error ocurred while sending package");
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
