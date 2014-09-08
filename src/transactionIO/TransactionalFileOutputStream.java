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
    public void write(byte[] b) throws IOException {
        if (migrated) {
            fileHandler = new RandomAccessFile(fileName, "rws");
            migrated = false;
            fileHandler.seek(offset);
        }

        fileHandler.write(b);
        offset += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (migrated) {
            fileHandler = new RandomAccessFile(fileName, "rws");
            migrated = false;
            fileHandler.seek(offset);
        }

        fileHandler.write(b, off, len);
        offset += len;
    }

    @Override
    public void close(){
        try {
            fileHandler.close();
        } catch (IOException e) {
            System.out.println("Error in closing input file");
            e.printStackTrace();
        }
    }

    public void setMigrated(boolean mig) {
        migrated = mig;
    }
}
