
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.Arrays;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by wenhanl on 14-9-4.
 */
public class SlaveNode {
    private SocketChannel sc;
    private HashMap<String, MigratableProcess> processList = new HashMap<>();
    private ByteBuffer readBuffer;
    private int bufLen = 500;

    SlaveNode(){
        // Open a socket channel connecting to master node on port 15640
        try {
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(ClusterConfig.HOSTNAME, ClusterConfig.PORT));
            sc.configureBlocking(false);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        readBuffer = ByteBuffer.allocate(bufLen);

        // Start a selector to listen to incoming data
        try {
            start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void start() throws IOException{

        // Create selector and bind socketChannel to it
        Selector selector = Selector.open();
        sc.register(selector, SelectionKey.OP_READ);

        boolean closed = false;

        while(!closed){
            // Use select to block until something readable in channel
            selector.select();

            Set<SelectionKey> keySet = selector.selectedKeys();

            Iterator<SelectionKey> keyIterator = keySet.iterator();

            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if(key.isReadable()){

                    readBuffer.clear();
                    byte[] b = new byte[1];
                    for(int i = 0; i < bufLen; i++)
                        readBuffer.put(i, b[0]);
                    int bufRead = sc.read(readBuffer);

                    if(bufRead == -1){
                        System.out.println("Remote socket closed");
                        closed = true;
                        continue;
                    }

                    String cmdInput = new String(readBuffer.array(), "UTF-8");
                    String tmpBuf[] = cmdInput.split("\0"); // Trim
                    String args[]=tmpBuf[0].split(" ");
                    //process command sent from master node
                    if(args[0].equals("run")){
                        Object[] mpArgs = { Arrays.copyOfRange(args, 3, args.length) };
                        runProcess(args[2], mpArgs);
                    }
                    else if(args[0].equals("terminate"))
                        System.exit(1);
                    else if(args[0].equals("plist"))
                        sendProcessInfo();
                    // Suspend process and store it in obj file
                    else if(args[0].equals("suspend")){
                        boolean success = suspendProcess(args[1], args[2], args[3]);
                        if(!success) continue;
                    }
                    // Resume process from obj file
                    else if(args[0].equals("restore")){
                        resumeProcess(args[1]);
                    }
                }
            }
        }

    }

    // Run process
    private void runProcess(String name, Object[] args){
        MigratableProcess mpProcess = null;
        try {
            Class<MigratableProcess> mpClass = (Class<MigratableProcess>) Class.forName(name);
            Constructor<?> mpConstructor = mpClass.getConstructor(String[].class);
            mpProcess = (MigratableProcess) mpConstructor.newInstance(args);
        } catch (ClassNotFoundException e) {
            System.err.format("Class Not Found: Can't find class with provided class name", e.getMessage());
            return;
        } catch (NoSuchMethodException e) {
            System.err.format("No such method: No such constructor", e.getMessage());
            return;
        } catch (InstantiationException e) {
            System.err.format("Instantiation Error:", e.getMessage());
            return;
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
            return;
        } catch (InvocationTargetException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("\nJob "+name+" start on this slave!");
        Thread tp = new Thread(mpProcess);
        tp.start();
        processList.put(name, mpProcess);
    }

    // Suspend Process
    private boolean suspendProcess(String name, String src, String dst) throws IOException{
        MigratableProcess proSuspend = processList.get(name);
        if(proSuspend == null) {
            System.out.println("No such process exists!");
            return false;
        }
        System.out.println("\nJob " + name + " done on this slave!");
        proSuspend.suspend();
        processList.remove(name);
        FileOutputStream outObjStream = new FileOutputStream(name+".obj");
        ObjectOutputStream outObj = new ObjectOutputStream(outObjStream);
        outObj.writeObject(proSuspend);
        outObj.flush();
        outObj.close();
        outObjStream.close();

        String str = "restore" + " " + name + " " + src + " " + dst;
        byte[] out = str.getBytes(Charset.forName("UTF-8"));
        ByteBuffer buffer= ByteBuffer.wrap(out);
        sc.write(buffer);
        return true;
    }

    // Resume Process
    private void resumeProcess(String name) throws IOException{
        FileInputStream inObjStream = new FileInputStream(name+".obj");
        ObjectInputStream inObj = new ObjectInputStream(inObjStream);
        MigratableProcess proRestore = null;
        try {
            proRestore = (MigratableProcess) inObj.readObject();
        } catch (ClassNotFoundException e) {
            System.err.format("Cannot read object!", e.getMessage());
        }
        inObj.close();
        inObjStream.close();

        // Delete the obj file
        File objFile = new File(name + ".obj");
        if(!objFile.delete()){
            System.out.println("Fail to delete obj file");
        }
        System.out.println("\nJob " + name + " start on this slave!");
        Thread rt = new Thread(proRestore);
        rt.start();
        processList.put(name,proRestore);
    }

    // Send process info back to Master in respond for plist command
    private void sendProcessInfo (){
        Object[] arr = processList.keySet().toArray();
        StringBuilder sendStr = new StringBuilder();
        sendStr.append("pinfo");
        for(int i=0;i<arr.length;i++)
            sendStr.append(" " + arr[i]);
        byte[] out = sendStr.toString().getBytes(Charset.forName("UTF-8"));
        ByteBuffer buffer= ByteBuffer.wrap(out);
        try {
            sc.write(buffer);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
