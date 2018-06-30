package aplicacion.Server;

/**
 *
 * @author bruno
 */
public class Telnet {

    public static String getCommand(final String c) {
        switch (c) {
            case "date":
                return "fecha";
            case "time":
                return "Esta fecha";
            case "example":
                return "alicia txt";
            default:
                return "El comando no existe";
        }
    }
}
