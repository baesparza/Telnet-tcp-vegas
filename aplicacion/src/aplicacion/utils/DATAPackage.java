package aplicacion.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

/**
 *
 * @author bruno
 */
public class DATAPackage {

    public long sequenceNumber;
    public long checkSum;
    public byte[] data;

    public DATAPackage(long sequenceNumber, byte[] data) throws IOException {
        this.sequenceNumber = sequenceNumber;
        this.data = data;

        this.checkSum = DATAPackage.getChecksum(data);
    }

    public byte[] getBytes() {
        return this.toString().getBytes();
    }

    @Override
    public String toString() {
        return sequenceNumber + "|" + checkSum + "|" + data;
    }

    public static DATAPackage getPackage() {
        return null;
    }

    public static long getChecksum(byte[] data) throws IOException {
        CheckedInputStream chekedInput = new CheckedInputStream(new ByteArrayInputStream(data), new Adler32());
        chekedInput.read(data);
        return chekedInput.getChecksum().getValue();
    }
}
