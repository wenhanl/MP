import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;

import transactionIO.TransactionalFileInputStream;
import transactionIO.TransactionalFileOutputStream;

public class GrepProcess implements MigratableProcess
{
    private TransactionalFileInputStream  inFile;
    private TransactionalFileOutputStream outFile;
    private String query;

    private volatile boolean suspending;
    private boolean finished = false;

    public GrepProcess(String args[]) throws Exception
    {
        if (args.length != 3) {
            System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
            throw new Exception("Invalid Arguments");
        }

        query = args[0];
        inFile = new TransactionalFileInputStream(args[1]);
        outFile = new TransactionalFileOutputStream(args[2], false);
    }

    public void run()
    {
        DataInputStream in = new DataInputStream(inFile);
        PrintStream out = new PrintStream(outFile);
        byte[] buf = new byte[256];
        try {
            while (!suspending) {
                String line = in.readLine();

                if (line == null) break;

                if (line.contains(query)) {

                    out.println(line);
                }
                System.out.println(line);

                // Make grep take longer so that we don't require extremely large files for interesting results
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // ignore it
                }
            }
        } catch (EOFException e) {
            finished = true;
            //End of File
        } catch (IOException e) {
            System.out.println ("GrepProcess: Error: " + e);
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
        while (suspending && !finished);
    }

}

