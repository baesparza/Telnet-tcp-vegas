package aplicacion.Server;

/**
 *
 * @author bruno
 */
public class Telnet {

    public static String getCommand(final String c) {
        System.out.println(c);
        if ("date".equals(c)) {
            return "fecha";
        }
        if ("time".equals(c)) {
            return "Esta fecha";
        }
        if ("example".equals(c)) {
            return "alicia txt";
        }
        return "El comando no existe";
    }
}
