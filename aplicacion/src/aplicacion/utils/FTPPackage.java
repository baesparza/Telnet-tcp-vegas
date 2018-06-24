package aplicacion.utils;

import java.io.IOException;
import java.util.zip.CRC32;

/**
 *
 * @author bruno
 */
public abstract class FTPPackage {

    public String id;

    /**
     * Abstract class for different type of packages
     *
     * @param id of package
     */
    public FTPPackage(String id) {
        this.id = id;
    }

    /**
     * get package in bytes to be send
     *
     * @return bytes of package
     */
    public byte[] getBytes() {
        return this.toString().getBytes();
    }

    /**
     * Abstract to be implemented by child
     *
     * @return composed package
     */
    @Override
    public abstract String toString();

    /**
     * Turn data into DATAPackage or ACKPackage
     *
     * @param data in stringify way
     * @return object
     */
    public static FTPPackage getPackage(String data) {
        String[] array = data.split(";");
        if (array.length == 4) {
            // this package has data
            return new DATAPackage(array[0], array[1], array[2], array[3]);
        }
        // this package is an ack
        return new ACKPackage(array[0]);
    }

    /**
     * Algorithm to gen checksum with given data
     *
     * @param data input
     * @return checksum algorithm output
     */
    public static long getChecksum(byte[] data) {
        CRC32 checksum = new CRC32();
        checksum.update(data, 0, data.length);
        return checksum.getValue();
    }

    /**
     * Validate a checksums and checksum of data
     *
     * @param checkSum value
     * @param data to be proceed and compared
     * @return
     * @throws IOException
     */
    public static boolean validChecksum(String checkSum, byte[] data) throws IOException {
        return checkSum.equals(String.valueOf(DATAPackage.getChecksum(data)));
    }

    public static boolean isACK(String data) {
        String[] array = data.split(";");
        return array.length == 1;
    }
}
