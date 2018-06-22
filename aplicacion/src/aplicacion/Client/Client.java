package aplicacion.Client;

import aplicacion.utils.ConsoleLogger;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
    private PrintWriter out;
    private BufferedReader buffer;
    private Thread thread;

    private final JTextArea textArea;
    private ConsoleLogger console;
    private boolean connection;

    /**
     * Constructor, set data for connection, initialize variables
     *
     * @param hostname address of TODO
     * @param port of TODO
     * @param textArea input textArea for write messages
     */
    public Client(InetAddress hostname, int port, JTextArea textArea, JTextArea txtConsole) {
        this.hostname = hostname;
        this.port = port;

        this.console = new ConsoleLogger(txtConsole);
        this.textArea = textArea;

        this.connection = false;
        this.open();
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
            this.socket = new DatagramSocket();

            this.connection = true;
        } catch (IOException ex) {
            console.error("Could't initialize socket");
        }
    }

    /**
     * Main loop for application, always listening TODO
     */
    @Override
    public void run() {
        while (true) {
            try {
                byte data[] = new byte[100];
                DatagramPacket packetIN = new DatagramPacket(data, data.length);
                socket.receive(packetIN); // wait for package
                this.receiveMessage(packetIN);
            } catch (IOException ex) {
                console.error("Socket recieve packet in");
            }
        }
    }

    /**
     * TODO TODO: InetAddress
     *
     * @param message
     */
    public void sendMessage(String message) {
        try {

            byte datos[] = message.getBytes(); // turn into bytes
            // crea objeto sendPacket
            DatagramPacket paqueteEnviar = new DatagramPacket(datos, datos.length, this.hostname, this.port);
            socket.send(paqueteEnviar); // envía el paquete
            console.info("Paquete enviado\n");

            ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
            CheckedInputStream cis = new CheckedInputStream(bais, new Adler32());
            byte readBuffer[] = new byte[5];
            while (cis.read(readBuffer) >= 0) {
                long value = cis.getChecksum().getValue();
                console.info("The value of checksum is " + value + "\n");
            }
        } catch (IOException e) {
            console.error("Ha ocurrido un error TODO");
        }
    }

    /**
     * TODO
     *
     * @param packet
     */
    public void receiveMessage(DatagramPacket packet) {
        this.textArea.append("\nPaquete recibido:"
                + "\nDe host: " + packet.getAddress()
                + "\nPuerto host: " + packet.getPort()
                + "\nLongitud: " + packet.getLength()
                + "\nContiene:\n\t" + new String(packet.getData(), 0, packet.getLength()));
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
            console.error("Couldn't end application safely");
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
