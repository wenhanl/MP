/**
 * Created by wenhanl on 14-9-4.
 */

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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


public class SlaveNode {
    private SocketChannel sc;
    private HashMap<String, MigratableProcess> processList = new HashMap<>();
    private ByteBuffer readBuffer;

    SlaveNode(){
        try {

            // Open a socket channel connecting to master node on port 15640
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress("localhost", 15640));
            sc.configureBlocking(false);

            int len = 500;

            // Create selector and bind socketChannel to it
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ);


            readBuffer = ByteBuffer.allocate(len);


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
                        for(int i = 0; i < len; i++)
                            readBuffer.put(i, b[0]);
                        int bufRead = sc.read(readBuffer);

                        if(bufRead == -1){
                            System.out.println("Remote socket closed");
                            closed = true;
                            continue;
                        }

                        String cmdInput = new String(readBuffer.array(),"UTF-8");
                        String tmpBuf[] = cmdInput.split("\0");
                        String args[]=tmpBuf[0].split(" ");
                        //process command sent from master node
                        if(args[0].equals("run")){

                            MigratableProcess mpProcess = null;
                            try {
                                Class<MigratableProcess> mpClass = (Class<MigratableProcess>) Class.forName(args[2]);
                                Constructor<?> mpConstructor = mpClass.getConstructor(String[].class);
                                Object[] mpArgs = { Arrays.copyOfRange(args, 3, args.length) };
                                mpProcess = (MigratableProcess) mpConstructor.newInstance(mpArgs);
                            } catch (ClassNotFoundException e) {
                                System.err.format("Class Not Found: Can't find class with provided class name", e.getMessage());
                            } catch (NoSuchMethodException e) {
                                System.err.format("No such method: No such constructor", e.getMessage());
                            } catch (InstantiationException e) {
                                System.err.format("Instantiation Error:", e.getMessage());
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            Thread tp = new Thread(mpProcess);
                            tp.start();
                            processList.put(args[2],mpProcess);
                        }
                        else if(args[0].equals("terminate"))
                            System.exit(1);
                        else if(args[0].equals("suspend")){
                            MigratableProcess proSuspend = processList.get(args[1]);
                            if(proSuspend == null) {
                                System.out.println("No such process exists!");
                                continue;
                            }
                            proSuspend.suspend();
                            processList.remove(args[1]);
                            FileOutputStream outObjStream = new FileOutputStream(args[1]+".obj");
                            ObjectOutputStream outObj = new ObjectOutputStream(outObjStream);
                            outObj.writeObject(proSuspend);
                            outObj.flush();
                            outObj.close();
                            outObjStream.close();

                            String str = "restore" + " " + args[1] + " " + args[2] + " " + args[3];
                            byte[] out = str.getBytes(Charset.forName("UTF-8"));
                            ByteBuffer buffer= ByteBuffer.wrap(out);
                            sc.write(buffer);
                        }
                        else if(args[0].equals("restore")){
                            FileInputStream inObjStream = new FileInputStream(args[1]+".obj");
                            ObjectInputStream inObj = new ObjectInputStream(inObjStream);
                            MigratableProcess proRestore = null;
                            try {
                                proRestore = (MigratableProcess) inObj.readObject();
                            } catch (ClassNotFoundException e) {
                                System.err.format("Cannot read object!", e.getMessage());
                            }
                            inObj.close();
                            inObjStream.close();
                            Thread rt = new Thread(proRestore);
                            rt.start();
                            processList.put(args[1],proRestore);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
