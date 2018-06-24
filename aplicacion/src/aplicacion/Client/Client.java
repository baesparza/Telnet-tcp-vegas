package aplicacion.Client;

import aplicacion.utils.ConsoleLogger;
import aplicacion.utils.DATAPackage;
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

    private final int destinationPort;
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
        this.destinationPort = port;

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
            // TODO: recieve ack or response
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
     * Transform outputData into packages, and are sent to destination. Send UDP
     * datagrams with checksum, sequence and each word as data
     *
     *
     * @param message
     */
    public void sendMessage(String message) {
        try {
            String[] array = message.split(" ");
            for (int i = 0; i < array.length; i++) {
                // generate package with usefull data for tcp-vegas
                DATAPackage dataPackage = new DATAPackage(i, array[i]);

                DatagramPacket pack = new DatagramPacket(
                        dataPackage.getBytes(),
                        dataPackage.getBytes().length,
                        this.hostname,
                        this.destinationPort
                );
                // send package with data, checksum, seq number, and datagramPacketdata
                socket.send(pack);

                console.info("Send pack N: " + i + " Length: " + pack.getLength() + " Checksum: " + dataPackage.checkSum);
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
