package aplicacion.Client;

import aplicacion.utils.ConsoleLogger;
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

    // TODO: sort variables
    private final int serverPort;
    private final InetAddress serverAddess;
    private DatagramSocket socket;
    private Thread thread;
    private byte[] receiveData;

    private OutputList outputList;
    private final JTextArea textArea;
    private ConsoleLogger console;
    private boolean connected;

    /**
     * Constructor, set data for connection, initialize variables. Create new
     * socket and add stream input/output that will let us read/write to the
     * socket
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
        //  this.outputList = new OutputList();
        this.receiveData = new byte[1024];
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
        // TODO: add valid while
        /*
        
        
        
        
        
        while (true) {
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
        this.socket.close();
    }

    /**
     * Transform outputData into packages, and are sent to destination. Send UDP
     * datagrams with checksum, sequence and each word as data
     *
     * @param message to be sent
     */
    public void sendMessage(String message) throws IOException {
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
        TCPPacket sendData = new TCPPacket();
        sendData.body = message;
        byte[] data = sendData.getHeader().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.serverAddess, this.serverPort);
        this.socket.send(sendPacket);

        System.out.println("------------Sent-----------");
        sendData.getHeader();
        System.out.println("---------------------------\n");

    }

    /**
     * TODO: add timeout
     */
    public void handshake() throws Exception {
        // create new synchronization packet and send it to server
        TCPPacket sendData = new TCPPacket();
        sendData.synchronizationBit = 1;
        byte[] data = sendData.getHeader().getBytes();
        DatagramPacket packetOUT = new DatagramPacket(data, data.length, this.serverAddess, this.serverPort);
        this.socket.send(packetOUT);
        // wait for a synchronization ACK packet from server
        DatagramPacket packetIN = new DatagramPacket(this.receiveData, this.receiveData.length);
        this.socket.receive(packetIN);
        TCPPacket receivedData = new TCPPacket(new String(packetIN.getData()));
        // validate if it's a synchronization ACK packet
        if (receivedData.acknowledgementBit == 1 && receivedData.synchronizationBit == 1) {
            // send confirmation to server
            sendData = new TCPPacket();
            sendData.synchronizationNumber = 1;
            sendData.acknowledgementNumber = 1;
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
     * TODO: add timeout
     *
     * @return
     * @throws Exception
     */
    public boolean disconnect() throws Exception {
        // send 
        System.out.println("Requested to disconnect.");
        TCPPacket sendData = new TCPPacket();
        byte[] data = sendData.getHeader().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.serverAddess, this.serverPort);
        this.socket.send(sendPacket);
//////////////
        DatagramPacket receivePacket = new DatagramPacket(this.receiveData, receiveData.length);
        this.socket.receive(receivePacket);
        TCPPacket receivedData = new TCPPacket(new String(receivePacket.getData()));

        if (receivedData.finishBit == 1) {
            sendData = new TCPPacket();
            sendData.acknowledgementBit = 1;
            data = sendData.getHeader().getBytes();
            sendPacket = new DatagramPacket(data, data.length, this.serverAddess, this.serverPort);
            this.socket.send(sendPacket);

            sendData = new TCPPacket();
            sendData.finishBit = 1;
            data = sendData.getHeader().getBytes();
            sendPacket = new DatagramPacket(data, data.length, this.serverAddess, this.serverPort);
            this.socket.send(sendPacket);
        }

        receivePacket = new DatagramPacket(this.receiveData, this.receiveData.length);
        this.socket.receive(receivePacket);
        receivedData = new TCPPacket(new String(receivePacket.getData()));

        return (receivedData.acknowledgementBit == 1);
    }

    /**
     * @return state of connection
     */
    public boolean isConnected() {
        return connected;
    }

}
