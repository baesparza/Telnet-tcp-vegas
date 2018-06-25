package aplicacion.TCPVEGAS;

import aplicacion.utils.ACKPackage;
import aplicacion.utils.DATAPackage;
import aplicacion.utils.FTPPackage;
import aplicacion.utils.ListPackages;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bruno
 */
public class Server implements Runnable {

    private DatagramSocket socket;
    private Thread thread;
    private ListPackages listPackages;

    public Server() {
        try {
            socket = new DatagramSocket(5000);
            listPackages = new ListPackages();
        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
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
            try {
                // set a incoming package size, and wait for incoming package
                DatagramPacket packageIN = new DatagramPacket(new byte[100], 100);
                socket.receive(packageIN);
                // new package in, get type
                if (FTPPackage.isACK(new String(packageIN.getData()))) {
                    // package is an ACK from client
                    continue;
                }
                // This package is not an ACK, it has data
                // if valid, store this data, and wait for full message
                DATAPackage dataPackage = (DATAPackage) FTPPackage.getPackage(new String(packageIN.getData()));
                // if after validations is valid, add package 
                if (listPackages.add(dataPackage)) {
                    this.sendACK(dataPackage.id, packageIN.getAddress(), packageIN.getPort());
                    // veryfy if all packages have been receibed
                    if (listPackages.hasEnded()) {
                        // pass data to application
                        System.out.println(listPackages.getMessage());
                        listPackages.clear();
                    }
                } else {
                    System.out.println("Invalid package");
                }

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void sendACK(int id, InetAddress hostname, int destPort) {
        try {
            // generate package with usefull data for tcp-vegas
            ACKPackage dataPack = new ACKPackage(id);

            DatagramPacket pack = new DatagramPacket(
                    dataPack.getBytes(),
                    dataPack.getBytes().length,
                    hostname,
                    destPort
            );
            // send package with data, checksum, seq number, and datagramPacketdata
            socket.send(pack);
            System.out.println("Sending ACK, id: " + id);
        } catch (IOException ex) {
            System.out.println("ERROR while sending ACK, id: " + id);
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // repite el paquete al cliente
    private void enviarPaqueteAlCliente(DatagramPacket paqueteRecibir) throws IOException {
        System.out.println("\n\nRepitiendo datos al cliente...");

        // crea paquete para enviar
        DatagramPacket paqueteEnviar = new DatagramPacket(
                paqueteRecibir.getData(), paqueteRecibir.getLength(),
                paqueteRecibir.getAddress(), paqueteRecibir.getPort());

        socket.send(paqueteEnviar); // envía paquete al cliente
        System.out.println("Paquete enviado\n");
    } // fin del método enviarPaqueteAlCliente

}
