package aplicacion.utils;

import java.io.IOException;
import java.util.zip.CRC32;

/**
 *
 * @author bruno
 */
public class DATAPackage {

    public long sequenceNumber;
    public long checkSum;
    public byte[] data;

    public DATAPackage(long sequenceNumber, String data) throws IOException {
        this.sequenceNumber = sequenceNumber;
        this.data = data.getBytes();

        this.checkSum = DATAPackage.getChecksum(this.data);
    }

    public DATAPackage(long sequenceNumber, long checkSum, byte[] data) {
        this.sequenceNumber = sequenceNumber;
        this.checkSum = checkSum;
        this.data = data;
    }

    public byte[] getBytes() {
        return this.toString().getBytes();
    }

    @Override
    public String toString() {
        return sequenceNumber + " " + checkSum + " " + data;
    }

    public static DATAPackage getPackage(byte[] data) throws IOException {
        String[] array = (new String(data)).split(" ");
        System.out.println("incoming value " + array[2]);
        if (array.length == 3) {
            // this package has data
            // TODO: fix data[2] is already an byte[]
            return new DATAPackage(Long.valueOf(array[0]), Long.valueOf(array[1]),);
        }
        // this package is an ack
        return null;
    }

    public static long getChecksum(byte[] data) {
        System.out.println(data);
        CRC32 checksum = new CRC32();
        checksum.update(data, 0, data.length);
        return checksum.getValue();
    }

    public String getData() {
        return new String(this.data);
    }

    public static boolean validChecksum(Long checkSum, byte[] data) throws IOException {
        return (checkSum == DATAPackage.getChecksum(data));
    }
}
