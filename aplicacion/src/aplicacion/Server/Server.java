package aplicacion.Server;

import aplicacion.utils.Receiver;
import aplicacion.utils.Sender;
import aplicacion.utils.TCPPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 *
 * @author bruno
 */
public final class Server implements Runnable {

    /**
     * Default port for sever
     */
    public static final int PORT = 5000;

    private DatagramSocket socket;
    private Thread thread;
    private Receiver receiver;
    private final Sender sender;

    /**
     * Initialize server and start listening for a handShake
     */
    public Server() {
        this.receiver = new Receiver();
        this.sender = new Sender();
        try {
            this.socket = new DatagramSocket(Server.PORT);
            this.handShake();
        } catch (SocketException ex) {
            System.out.println("Can't initialize socket");
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("Can't initailize handshake");
            System.exit(1);
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
        while (true) {
            byte[] receiveData = new byte[1024];
            try {
                DatagramPacket packetIN = new DatagramPacket(receiveData, receiveData.length);
                this.socket.receive(packetIN);
                TCPPacket packet = new TCPPacket(new String(packetIN.getData()));
                // get type of packet
                if (packet.checksum != 0) {
                    // packet has data
                    if (this.receiver.add(packet)) {
                        this.sendACK(packet.sequenceNumber, packetIN.getAddress(), packetIN.getPort());
                        // veryfy if all packages have been receibed
                        if (this.receiver.hasEnded()) {
                            // pass data to application
                            sendMessage(this.receiver.getMessage(), packetIN.getAddress(), packetIN.getPort());
                            this.receiver.clear();
                        }
                    } else {
                        System.out.println("Invalid package have been deleted");
                    }
                } else if (packet.finishBit == 1) {
                    // client wants to disconnect
                    this.disconnect(packetIN.getAddress(), packetIN.getPort());
                    System.exit(0);
                }
            } catch (IOException ex) {
                System.out.println("Socket fail at receive");
            }
        }
    }

    private void sendACK(int sequenceNumber, InetAddress hostname, int destPort) {
        try {
            // generate ack TCPPackage 
            TCPPacket packet = new TCPPacket();
            packet.acknowledgementBit = 1;
            packet.sequenceNumber = sequenceNumber;
            // Send packet
            byte[] data = packet.getHeader().getBytes();
            DatagramPacket pack = new DatagramPacket(data, data.length, hostname, destPort);
            socket.send(pack);
            System.out.println("Sending ACK, sequence: " + sequenceNumber);
        } catch (IOException ex) {
            System.out.println("ERROR while sending ACK, sequence: " + sequenceNumber);
        }
    }

    /**
     * Transform outputData into packages, and are sent to destination. Send UDP
     * datagrams with checksum, sequence and each word as data
     *
     * @param message to be sent
     */
    public void sendMessage(String message, InetAddress serverAddess, int serverPort) {
        String[] array = message.split("");
        // split message into small packages, and add them to list
        for (int i = 0; i < array.length; i++) {
            if (!this.sender.addPackage(new TCPPacket(i, (i < array.length - 1) ? 1 : 0, array[i].equals(" ") ? "_" : array[i]))) {
                // packet was not added
                System.out.println("Packet could not be added");
            }
        }
        //try {
        this.sender.sendPackages(this.socket, serverAddess, serverPort, null);
        //} catch (IOException e) {
        //    console.error("An error ocurred while sending messages");
        //}
    }

    /**
     * Server handshake with a client
     *
     * @throws IOException when socket fails
     */
    public void handShake() throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket packetIN = new DatagramPacket(receiveData, receiveData.length);
        // Whait for client to connect
        this.socket.receive(packetIN);
        InetAddress clientAddress = packetIN.getAddress();
        int clientPort = packetIN.getPort();
        TCPPacket receivePacket = new TCPPacket(new String(packetIN.getData()));
        // check if is a synchronization package
        if (receivePacket.synchronizationBit == 1) {
            // Send synchronization ACK
            TCPPacket packet = new TCPPacket();
            // TODO: create sync packet and sync ack
            packet.synchronizationBit = 1;
            packet.acknowledgementBit = 1;
            // get bytes of package with empty body
            byte[] data = packet.getHeader().getBytes();
            DatagramPacket packetOUT = new DatagramPacket(data, data.length, clientAddress, clientPort);
            this.socket.send(packetOUT);
            // wait for a confirmation from the client
            packetIN = new DatagramPacket(receiveData, receiveData.length);
            this.socket.receive(packetIN);
            packet = new TCPPacket(new String(packetIN.getData()));
            // validate if conection was made
            if (packet.acknowledgementBit == 1 && packet.synchronizationBit == 1) {
                System.out.println("Client conected, Client IP: " + clientAddress.getHostAddress() + ", Client PORT: " + clientPort);
            }
        }
        // TODO: add when package fails
    }

    /**
     * TODO: add timer when trigger started-----------------------------------
     * Disconnect and free socket
     *
     * @param clientAddress of client
     * @param clientPort of client
     */
    public void disconnect(InetAddress clientAddress, int clientPort) {
        try {
            // disconnection triggered, send finish ACK
            TCPPacket sendData = new TCPPacket();
            sendData.finishBit = 1;
            sendData.acknowledgementBit = 1;
            byte[] data = sendData.getHeader().getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, clientAddress, clientPort);
            this.socket.send(sendPacket);
            this.socket.close();
            System.out.println("Client disconnected");
        } catch (IOException ex) {
            System.out.println("Can't send finish ACK");
        }
    }

}
