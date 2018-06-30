package aplicacion.Client;

import aplicacion.utils.Receiver;
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
public final class Client implements Runnable {

    private final int port; // server port
    private final InetAddress address; // server ip_v4 address
    private final DatagramSocket socket; // transfort
    private Thread thread;

    private final Sender sender; // manage out packets
    private final Receiver receiver; // manage incoming packets
    private final JTextArea textArea; // response window
    private final ConsoleLogger console; // app console logger
    private boolean connected; // state of connection

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
    public Client(final InetAddress hostname, final int port, final JTextArea textArea, final JTextArea txtConsole) throws Exception {
        this.address = hostname;
        this.port = port;
        this.console = new ConsoleLogger(txtConsole);
        this.textArea = textArea;
        this.socket = new DatagramSocket();
        this.sender = new Sender();
        this.receiver = new Receiver();
        this.connected = false;
        // connect to server
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
            byte[] buffer = new byte[1024]; // buffer
            try {
                // wait for a packet to be received
                DatagramPacket packetIN = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packetIN);
                TCPPacket tcpPacket = new TCPPacket(new String(packetIN.getData()));
                // get type of packet
                if (tcpPacket.checksum != 0) {
                    // packet is a response, verify content, send ack if valid
                    if (this.receiver.add(tcpPacket)) {
                        this.sendACK(tcpPacket.sequence, packetIN.getAddress(), packetIN.getPort());
                        // veryfy if all packages have been receibed
                        if (this.receiver.hasEnded()) {
                            // manage data, present to app, and clear buffer
                            console.info("Received full response");
                            this.textArea.append(this.receiver.getMessage() + "\n\n\n");
                            this.receiver.clear();
                        }
                    } else {
                        console.error("Invalid package have been deleted");
                    }
                } else if (tcpPacket.acknowledgementFlag == 1) {
                    // packet is an ACK, tell outout manager and ACK arrived
                    console.info("Recieved ACK, Sequence: " + tcpPacket.sequence);
                    this.sender.receivedACK(tcpPacket.sequence);
                }
            } catch (IOException ex) {
                console.warning("Socket fail at receive");
            }
        }
        // close connection
        this.socket.close();
    }

    /**
     * Send ACK to specific destination::port
     *
     * @param sequenceNumber of ACKed packet
     * @param hostname ip address of server
     * @param port of server
     */
    private void sendACK(int sequenceNumber, InetAddress hostname, int port) {
        try {
            // generate Ack TCPPackage and send it
            byte[] data = TCPPacket.ACKPacket(sequenceNumber).getHeader().getBytes();
            DatagramPacket pack = new DatagramPacket(data, data.length, hostname, port);
            socket.send(pack);
            console.info("Sending ACK, sequence: " + sequenceNumber);
        } catch (IOException ex) {
            console.error("ERROR while sending ACK, sequence: " + sequenceNumber);
        }
    }

    /**
     * Trigger from APP to send command to server
     *
     * @param command to be sent
     */
    public void sendMessage(String command) {
        // split message into small packages, and add them to list
        String[] array = command.split("");
        for (int i = 0; i < array.length; i++) {
            if (!this.sender.addPackage(new TCPPacket(
                    i, // sequense number
                    (i < array.length - 1) ? 1 : 0, // fragment flag
                    array[i].equals(" ") ? "_" : array[i] // packet data
            ))) {
                // Packet could not be added
                console.warning("Packet # " + i + " could not be added to sender manager");
            }
        }
        this.sender.sendPackages(this.socket, this.address, this.port, this.console);
    }

    /**
     * Three way hand shake to connect to client
     *
     * @throws java.lang.Exception if socket fails
     */
    public void handshake() throws Exception {
        // TODO: add timeout
        // create new synchronization packet and send it to server
        byte[] buffer = new byte[1024];
        byte[] data = TCPPacket.SYNCPacket().getHeader().getBytes();
        this.socket.send(new DatagramPacket(data, data.length, this.address, this.port));
        // wait for a synchronization ACK packet from server
        DatagramPacket packetIN = new DatagramPacket(buffer, buffer.length);
        this.socket.receive(packetIN);
        TCPPacket packet = new TCPPacket(new String(packetIN.getData()));
        // validate if it's a synchronization ACK packet
        if (packet.acknowledgementFlag == 1 && packet.synchronizationFlag == 1) {
            // send confirmation to server
            data = TCPPacket.SYNCACKPacket().getHeader().getBytes();
            this.socket.send(new DatagramPacket(data, data.length, this.address, this.port));
            this.console.info("Connected to server, IP: " + this.address.getHostAddress() + ", Port: " + this.port);
            this.connected = true;
        }
        // TODO: add when not connected
    }

    /**
     * Start events to disconnect from server, end demo
     *
     * @throws java.io.IOException if socket fails
     */
    public void disconnect() throws IOException {
        // TODO: add timeout
        // send an disconnect package server
        byte[] buffer = new byte[1024];
        byte[] data = TCPPacket.FINPacket().getHeader().getBytes();
        this.socket.send(new DatagramPacket(data, data.length, this.address, this.port));
        // wait for an finish ACK from server
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        this.socket.receive(receivePacket);
        TCPPacket packet = new TCPPacket(new String(receivePacket.getData()));
        // validate if it's a finish ACK
        if (packet.finishFlag == 1 && packet.acknowledgementFlag == 1) {
            // confirmation from server, close this connection
            this.connected = false;
            this.socket.close();
        }
        // TODO: add when other packet or not receive sync ack
    }

    /**
     * @return state of connection
     */
    public boolean isConnected() {
        return this.connected;
    }

}
