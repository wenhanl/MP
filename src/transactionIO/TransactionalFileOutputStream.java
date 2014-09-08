package transactionIO;

import java.io.*;

/**
 * Created by wenhanl on 14-9-2.
 */
public class TransactionalFileOutputStream extends OutputStream implements Serializable{
    private String fileName;
    private long offset;
    private transient RandomAccessFile fileHandler;
    private boolean migrated;

    public TransactionalFileOutputStream(String str, boolean right){
        fileName = str;
        offset = 0;
        migrated = right;
        try {
            fileHandler = new RandomAccessFile(fileName,"rws");
        } catch (FileNotFoundException e) {
            System.out.println("Cannot open file " + fileName + "!");
        }
    }

    public void write(int l) throws IOException{
        if (migrated) {
            fileHandler = new RandomAccessFile(fileName, "rws");
            migrated = false;
            fileHandler.seek((offset));
        }
        fileHandler.write(l);
        offset++;
    }

    @Override
    public void close() throws IOException{
        fileHandler.close();
    }

    public void setMigrated(boolean mig) {
        migrated = mig;
    }
}
