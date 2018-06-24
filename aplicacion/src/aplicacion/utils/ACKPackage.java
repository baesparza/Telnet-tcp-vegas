package aplicacion.utils;

/**
 *
 * @author bruno
 */
public class ACKPackage extends FTPPackage {

    /**
     * Package with id to validate specific package
     *
     * @param id of package
     */
    public ACKPackage(String id) {
        super(id);
    }

    /**
     * Structured id of a package
     *
     * @return string
     */
    @Override
    public String toString() {
        return this.id;
    }

}
