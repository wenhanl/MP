/**
 * Created by CGJ on 14-9-8.
 *
 * This program calculate the total number of words as well as
 * the number of words in one line in a text file
 */
import transactionIO.TransactionalFileInputStream;
import transactionIO.TransactionalFileOutputStream;
import java.io.*;

public class SumWordProcess implements MigratableProcess {

    private TransactionalFileInputStream inFile;
    private TransactionalFileOutputStream outFile;
    private volatile boolean suspending;
    int total = 0;
    int lineNum = 1;
    private volatile boolean finished;

    public SumWordProcess(String[] args) throws Exception{
        if (args.length != 2 ) {
            System.out.println("usage: SumWordProcess <inputfile> <outputfile>");
            throw new Exception("Invalid arguments");
        }
        inFile = new TransactionalFileInputStream(args[0]);
        outFile = new TransactionalFileOutputStream(args[1],false);
        finished = false;
    }
    public void run(){
        DataInputStream br = new DataInputStream(inFile);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(outFile));
        try {
            while(!suspending) {
                //calculate the number of words in each line
                String line = br.readLine();
                String arr[] = line.split(" ");
                String re = "Line " + lineNum++ + ": " + arr.length;
                total+=arr.length;
                pw.write(re);
                pw.println();
                System.out.println(re);
                Thread.sleep(1000);
            }
            pw.write("Total: ");
            pw.println(total);
        }
        catch (InterruptedException e) {}
        catch(NullPointerException e){
            pw.write("Total: ");
            pw.println(total);
        }

        catch(EOFException e){
            finished = true;
        }
        catch(IOException e){
            e.printStackTrace();
        }
        try {
            br.close();
            inFile.setMigrated(true);
            pw.close();
            outFile.setMigrated(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        suspending = false;

    }
    public void suspend(){
        suspending = true;
        while (suspending && !finished);
    }


}

