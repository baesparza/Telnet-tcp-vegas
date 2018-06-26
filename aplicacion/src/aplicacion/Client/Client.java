package aplicacion.Client;

import aplicacion.utils.ACKPackage;
import aplicacion.utils.ConsoleLogger;
import aplicacion.utils.DATAPackage;
import aplicacion.utils.FTPPackage;
import aplicacion.utils.OutputList;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.JTextArea;

/**
 *
 * @author bruno
 */
public class Client implements Runnable {

    private final int destinationPort;
    private final InetAddress hostname;
    private DatagramSocket socket;
    private Thread thread;

    private OutputList outputList;
    private final JTextArea textArea;
    private ConsoleLogger console;
    private boolean connection;

    /**
     * Constructor, set data for connection, initialize variables. Create new
     * socket and add stream input/output that will let us read/write to the
     * socket
     *
     * @param hostname ip address
     * @param port of socket
     * @param textArea input textArea for write messages
     * @param txtConsole textArea to write logs
     */
    public Client(InetAddress hostname, int port, JTextArea textArea, JTextArea txtConsole) {
        this.hostname = hostname;
        this.destinationPort = port;

        this.console = new ConsoleLogger(txtConsole);
        this.textArea = textArea;
        this.outputList = new OutputList();
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
                // validate if type of package is ACK or a response
                if (FTPPackage.isACK(new String(packetIN.getData()))) {
                    // tell package ack has arrived
                    ACKPackage ack = (ACKPackage) FTPPackage.getPackage(new String(packetIN.getData()));
                    console.info("Recieved ACK, ID: " + ack.id);
                    this.outputList.receivedACK(ack.id);
                    continue;
                }
                // package is a response from server
            } catch (IOException ex) {
                console.error("Socket recieve packet in");
            }
        }
    }

    /**
     * Transform outputData into packages, and are sent to destination. Send UDP
     * datagrams with checksum, sequence and each word as data
     *
     * @param message to be sent
     */
    public void sendMessage(String message) {
        try {
            String[] array = message.split("");
            for (int i = 0; i < array.length; i++) {
                // gen package and add it to the list
                if (!this.outputList.addPackage(new DATAPackage(i, array[i], (i + 1) < array.length ? 1 : 0))) {
                    // package was not added
                    this.console.warning("Package could not be added to list, packages are still being sent");
                }
            }
            this.outputList.sendPackages(this.socket, this.hostname, this.destinationPort, this.console);
        } catch (IOException e) {
            console.error("An error ocurred while sending message");
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
