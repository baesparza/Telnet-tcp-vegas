package aplicacion.Client;

import aplicacion.utils.Sender;
import aplicacion.utils.TCPPacket;
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

    // TODO: sort variables
    private final int serverPort;
    private final InetAddress serverAddess;
    private final DatagramSocket socket;
    private Thread thread;

    private final Sender sender;
    private final JTextArea textArea;
    private final ConsoleLogger console;
    private boolean connected;

    /**
     * Constructor, set data for connection, initialize variables. Create new
     * socket and add stream input/output that will let us read/write thought
     * the socket
     *
     * @param hostname ip address of server
     * @param port of server
     * @param textArea input textArea for write messages
     * @param txtConsole textArea to write logs
     * @throws java.lang.Exception when failed to create client
     */
    public Client(InetAddress hostname, int port, JTextArea textArea, JTextArea txtConsole) throws Exception {
        this.serverAddess = hostname;
        this.serverPort = port;
        // GUI text areas
        this.console = new ConsoleLogger(txtConsole);
        this.textArea = textArea;
        // init socket and other fields
        this.socket = new DatagramSocket();
        this.sender = new Sender();
        this.connected = false;
        // connect client to server
        this.handshake();
    }

    /**
     * Start this thread, and synchronize client
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
        while (this.connected) {
            byte[] receiveData = new byte[1024];
            try {
                DatagramPacket packetIN = new DatagramPacket(receiveData, receiveData.length);
                this.socket.receive(packetIN);
                TCPPacket packet = new TCPPacket(new String(packetIN.getData()));
                // get type of packet
                if (packet.checksum != 0) {
                    // packet has data

                } else if (packet.acknowledgementBit == 1) {
                    // it's an ACK
                    // tell package ack has arrived
                    console.info("Recieved ACK, sequence: " + packet.sequenceNumber);
                    this.sender.receivedACK(packet.sequenceNumber);
                }
            } catch (IOException ex) {
                System.out.println("Socket fail at receive");
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
        String[] array = message.split("");
        // split message into small packages, and add them to list
        for (int i = 0; i < array.length; i++) {
            if (!this.sender.addPackage(new TCPPacket(i, (i < array.length - 1) ? 1 : 0, array[i].equals(" ") ? "_" : array[i]))) {
                // packet was not added
                console.warning("Packet could not be added");
            }
        }
        //try {
        this.sender.sendPackages(this.socket, this.serverAddess, this.serverPort, this.console);
        //} catch (IOException e) {
        //    console.error("An error ocurred while sending messages");
        //}
    }

    /**
     * TODO: add timeout
     *
     * @throws java.lang.Exception if socket error
     */
    public void handshake() throws Exception {
        byte[] receiveData = new byte[1024];
        // create new synchronization packet and send it to server
        TCPPacket sendData = new TCPPacket();
        sendData.synchronizationBit = 1;
        byte[] data = sendData.getHeader().getBytes();
        DatagramPacket packetOUT = new DatagramPacket(data, data.length, this.serverAddess, this.serverPort);
        this.socket.send(packetOUT);
        // wait for a synchronization ACK packet from server
        DatagramPacket packetIN = new DatagramPacket(receiveData, receiveData.length);
        this.socket.receive(packetIN);
        TCPPacket receivedData = new TCPPacket(new String(packetIN.getData()));
        // validate if it's a synchronization ACK packet
        if (receivedData.acknowledgementBit == 1 && receivedData.synchronizationBit == 1) {
            // send confirmation to server
            sendData = new TCPPacket();
            sendData.synchronizationBit = 1;
            sendData.acknowledgementBit = 1;
            data = sendData.getHeader().getBytes();
            packetOUT = new DatagramPacket(data, data.length, this.serverAddess, this.serverPort);
            this.socket.send(packetOUT);
            this.console.info("Client connected, Server IP: " + this.serverAddess.getHostAddress() + ", Server Port: " + this.serverPort);
            this.connected = true;
        }
        // TODO: add when not connected
    }

    /**
     * TODO: add timeout ----------------------------------------------------
     * End connection with Server
     *
     * @throws java.io.IOException if socket error
     */
    public void disconnect() throws IOException {
        // send an disconnect package server
        byte[] receiveData = new byte[1024];
        TCPPacket sendData = new TCPPacket();
        sendData.finishBit = 1;
        byte[] data = sendData.getHeader().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.serverAddess, this.serverPort);
        this.socket.send(sendPacket);
        // wait for an finish ACK from server
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        this.socket.receive(receivePacket);
        TCPPacket receivedData = new TCPPacket(new String(receivePacket.getData()));
        // validate if it's a finish ACK
        if (receivedData.finishBit == 1 && receivedData.acknowledgementBit == 1) {
            // confirmation from client, close this connection
            this.connected = false;
            this.socket.close();
        }
        // TODO: add when other packet
    }

    /**
     * @return state of connection
     */
    public boolean isConnected() {
        return connected;
    }

}
