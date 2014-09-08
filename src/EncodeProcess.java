import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;
import transactionIO.TransactionalFileInputStream;
import transactionIO.TransactionalFileOutputStream;
/**
 * Created by CGJ on 14-9-8.
 *
 * This program use Caesar cipher to encode a text file
 */
public class EncodeProcess implements MigratableProcess{
    private TransactionalFileInputStream  inFile;
    private TransactionalFileOutputStream outFile;
    private volatile boolean suspending;


    public EncodeProcess (String args[]) throws Exception
    {
        if (args.length != 2) {
            System.out.println("usage: EncodeProcess <inputFile> <outputFile>");
            throw new Exception("Invalid Arguments");
        }
        inFile = new TransactionalFileInputStream(args[0]);
        outFile = new TransactionalFileOutputStream(args[1], false);
    }

    public void run()
    {
        System.out.println("Job start on this slave!");
        DataOutputStream out = new DataOutputStream(outFile);
        DataInputStream in = new DataInputStream(inFile);
        //encode here
        try {
            while(!suspending) {
                int x = in.readChar();
                out.writeChar((char)(x+3));
                System.out.println((char)x);
                Thread.sleep(500);
            }
        }
        catch (InterruptedException e) {}
        catch (IOException e){
            e.printStackTrace();
        }

        try {
            in.close();
            inFile.setMigrated(true);
            out.close();
            outFile.setMigrated(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        suspending = false;
    }

    public void suspend()
    {
        suspending = true;
        System.out.println("Job done on this slave!");
        while (suspending);
    }

}
