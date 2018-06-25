package aplicacion.utils;

import java.io.IOException;
import java.util.zip.CRC32;

/**
 *
 * @author bruno
 */
public abstract class FTPPackage {

    public int id;

    /**
     * Abstract class for different type of packages
     *
     * @param id of package
     */
    public FTPPackage(int id) {
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
            return new DATAPackage(
                    Integer.valueOf(array[0].trim()),
                    Long.valueOf(array[1].trim()),
                    array[2],
                    Integer.valueOf(array[3].trim())
            );
        }
        // this package is an ack
        return new ACKPackage(Integer.valueOf(array[0].trim()));
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
    public static boolean validChecksum(long checkSum, byte[] data) throws IOException {
        return checkSum == DATAPackage.getChecksum(data);
    }

    /**
     * Validate if package is an ACK
     *
     * @param data from datagram
     * @return true if is an ACK
     */
    public static boolean isACK(String data) {
        String[] array = data.split(";");
        return array.length == 1;
    }
}
