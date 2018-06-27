package aplicacion.Client;

import aplicacion.utils.ACKPackage;
import aplicacion.utils.ConsoleLogger;
import aplicacion.utils.DATAPackage;
import aplicacion.utils.FTPPackage;
import aplicacion.utils.OutputList;
import aplicacion.utils.TCPPacket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author bruno
 */
public class Client implements Runnable {

    private final int port;
    private final InetAddress hostname;
    private DatagramSocket socket;
    private Thread thread;
    private byte[] receiveData;

    private OutputList outputList;
    private final JTextArea textArea;
    private ConsoleLogger console;

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
    public Client(InetAddress hostname, int port, JTextArea textArea, JTextArea txtConsole) throws SocketException {
        this.socket = new DatagramSocket();
        this.hostname = hostname;
        this.port = port;

        this.console = new ConsoleLogger(txtConsole);
        this.textArea = textArea;
        this.outputList = new OutputList();

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        receiveData = new byte[1024];
    }

    /**
     * Start this thread, and synchronize client
     */
    public synchronized void start() {
        thread = new Thread(this);
        thread.start();

        this.handshake();
    }

    /**
     * Main loop for application, always listening for packages
     */
    @Override
    public void run() {
        /*
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
         */
        boolean quit = false;
        while (!quit) {
            System.out.print("Enter string to send (\"quit\" to exit): ");
            String input = inFromUser.readLine();

            if (input.indexOf("quit") != -1) {
                System.out.println("Requested to disconnect.");
                TCPPacket sendData = new TCPPacket();
                byte[] data = sendData.getHeader(input).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostname, this.port);
                this.socket.send(sendPacket);
                quit = disconnect();
            } else {
                TCPPacket sendData = new TCPPacket();
                sendData.body = input;
                byte[] data = sendData.getHeader().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostname, this.port);
                this.socket.send(sendPacket);

                System.out.println("------------Sent-----------");
                sendData.printContent();
                System.out.println("---------------------------\n");
            }

        }

        this.socket.close();
    }

    /**
     * Transform outputData into packages, and are sent to destination. Send UDP
     * datagrams with checksum, sequence and each word as data
     *
     * @param message to be sent
     */
    public void sendMessage(String message) {
        /*
        try {
            String[] array = message.split("");
            for (int i = 0; i < array.length; i++) {
                // gen package and add it to the list
                if (!this.outputList.addPackage(new DATAPackage(i, array[i], (i + 1) < array.length ? 1 : 0))) {
                    // package was not added
                    this.console.warning("Package could not be added to list, packages are still being sent");
                }
            }
            this.outputList.sendPackages(this.socket, this.hostname, this.port, this.console);
        } catch (IOException e) {
            console.error("An error ocurred while sending message");
        }
         */
        String[] array = message.split("");
        for (int i = 0; i < array.length; i++) {
            try {
                TCPPacket sendData = new TCPPacket();

                sendData.synchronizationNumber = 1;
                sendData.acknowledgementNumber = 1;
                sendData.synchronizationBit = 1;
                sendData.acknowledgementBit = 1;

                byte[] data = sendData.getHeader(array[i]).getBytes();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostname, this.port);
                this.socket.send(sendPacket);
                this.console.warning("sended");
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * TODO: add timeout
     *
     * @throws Exception
     */
    public void handshake() throws Exception {
        TCPPacket sendData = new TCPPacket();
        sendData.synchronizationBit = 1;

        byte[] data = sendData.getHeader().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostname, this.port);
        this.socket.send(sendPacket);

        DatagramPacket receivePacket = new DatagramPacket(this.receiveData, this.receiveData.length);
        this.socket.receive(receivePacket);
        TCPPacket receivedData = new TCPPacket(new String(receivePacket.getData()));

        if (receivedData.acknowledgementBit == 1) {
            sendData = new TCPPacket();
            sendData.synchronizationNumber = 1;
            sendData.acknowledgementNumber = 1;
            sendData.synchronizationBit = 1;
            sendData.acknowledgementBit = 1;

            data = sendData.getHeader().getBytes();
            sendPacket = new DatagramPacket(data, data.length, this.hostname, this.port);
            this.socket.send(sendPacket);
            this.console.info("Three-way Handshake Complete, Client is connected");
        }
    }

    /**
     * TODO: add timeout
     *
     * @return
     * @throws Exception
     */
    public boolean disconnect() throws Exception {
        DatagramPacket receivePacket = new DatagramPacket(this.receiveData, receiveData.length);
        this.socket.receive(receivePacket);
        TCPPacket receivedData = new TCPPacket(new String(receivePacket.getData()));

        if (receivedData.finishBit == 1) {
            TCPPacket sendData = new TCPPacket();
            sendData.acknowledgementBit = 1;
            byte[] data = sendData.getHeader().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.hostname, this.port);
            this.socket.send(sendPacket);

            sendData = new TCPPacket();
            sendData.finishBit = 1;
            data = sendData.getHeader().getBytes();
            sendPacket = new DatagramPacket(data, data.length, this.hostname, this.port);
            this.socket.send(sendPacket);
        }

        receivePacket = new DatagramPacket(this.receiveData, this.receiveData.length);
        this.socket.receive(receivePacket);
        receivedData = new TCPPacket(new String(receivePacket.getData()));

        return (receivedData.acknowledgementBit == 1);
    }
}
