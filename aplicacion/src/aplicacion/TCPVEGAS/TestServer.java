package aplicacion.TCPVEGAS;

/**
 *
 * @author bruno
 */
public class TestServer {

    public static void main(String[] args) throws Exception {

        System.out.println("Server running");
        Server s = new Server();
        s.start();
    }
}
