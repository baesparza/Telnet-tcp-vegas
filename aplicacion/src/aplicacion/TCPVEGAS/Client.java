/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aplicacion.TCPVEGAS;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.zip.CheckedInputStream;
import java.util.Scanner;
import java.util.zip.Adler32;

/**
 *
 * @author bruno
 */
public class Client {

    Scanner input = new Scanner(System.in);
    private DatagramSocket socket; // socket para conectarse al servidor

    public Client() {

        try // crea y envía un paquete
        {
            socket = new DatagramSocket();// crar un nuevo socket
            // obtiene mensaje a enviar
            System.out.print("Ingrese un mensaje: ");
            String mensaje = input.next();
            System.out.println("\nEnviando paquete que contiene: " + mensaje + "\n");

            byte datos[] = mensaje.getBytes(); // convierte en bytes

            // crea objeto sendPacket
            DatagramPacket paqueteEnviar = new DatagramPacket(datos, datos.length, InetAddress.getLocalHost(), 5000);
            socket.send(paqueteEnviar); // envía el paquete
            System.out.println("Paquete enviado\n");
            byte buffer[] = mensaje.getBytes();

            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            CheckedInputStream cis = new CheckedInputStream(bais, new Adler32());
            byte readBuffer[] = new byte[5];
            while (cis.read(readBuffer) >= 0) {
                long value = cis.getChecksum().getValue();
                System.out.println("The value of checksum is " + value + "\n");
            }

        } // fin de try
        catch (IOException excepcionES) {
            System.err.println(excepcionES.toString() + "\n");
            excepcionES.printStackTrace();
        } // fin de catch

    }

// espera a que lleguen los paquetes del servidor, muestra el contenido de éstos
    public void esperarPaquetes() {
        while (true) {
            try // recibe paquete y muestra su contenido
            {
                byte datos[] = new byte[100]; // establece el paquete
                DatagramPacket paqueteRecibir = new DatagramPacket(
                        datos, datos.length);

                socket.receive(paqueteRecibir); // espera el paquete

                // muestra el contenido del paquete
                System.out.println("\nPaquete recibido:"
                        + "\nDe host: " + paqueteRecibir.getAddress()
                        + "\nPuerto host: " + paqueteRecibir.getPort()
                        + "\nLongitud: " + paqueteRecibir.getLength()
                        + "\nContiene:\n\t" + new String(paqueteRecibir.getData(),
                                0, paqueteRecibir.getLength()));
            } // fin de try
            catch (IOException excepcion) {
                System.err.println(excepcion.toString() + "\n");
                excepcion.printStackTrace();
            } // fin de catch
        } // fin de while
    } // fin del método esperarPaquetes
}
