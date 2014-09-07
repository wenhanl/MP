package transactionIO;

import java.io.*;

/**
 * Created by wenhanl on 14-9-2.
 */
public class TransactionalFileInputStream extends InputStream implements Serializable{
    private String fileName;
    private long offset;
    private transient RandomAccessFile fileHandler;
    private boolean migrated;

    public TransactionalFileInputStream(String str){
        fileName = str;
        offset = 0;
        migrated = false;
        try {
            fileHandler = new RandomAccessFile(fileName,"r");
        } catch (FileNotFoundException e) {
            System.out.println("Cannot open file " + fileName + "!");
        }

    }
    @Override
    public int read() throws IOException{
        if(migrated){
            try {
                fileHandler = new RandomAccessFile(fileName, "r");
            } catch (FileNotFoundException e) {
                System.out.println("Cannot find file " + fileName + "!");
            }
            migrated = false;
            fileHandler.seek(offset);
        }
        int res = 0;
        res = fileHandler.read();
        if(res != -1)offset++;
        return res;
    }

    @Override
    public void close() throws IOException{
        fileHandler.close();
    }

    public void setMigrated(boolean mig) {
        migrated = mig;
    }
}
