/**
 * Created by wenhanl on 14-9-4.
 */

import java.net.BindException;
import java.net.InetSocketAddress;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MasterNode {
    private HashMap<String,NodeInfo> slaveList;
    public static final int PORT = 15640;
    private ByteBuffer readBuffer;

    MasterNode(){
        slaveList = new HashMap<>();

        // Listen to slave connections
        int len = 500;
        readBuffer = ByteBuffer.allocate(len);
        startListener();

        // Start user console
        startConsole();
    }

    // Open a background thread to listen to slave connection and receive data.
    public void startListener(){
        Thread listening = new Thread(new Runnable() {
            public void run()
            {
                try {

                    ServerSocketChannel serverChannel = ServerSocketChannel.open();
                    serverChannel.socket().bind(new InetSocketAddress("localhost", PORT));
                    Selector selector = Selector.open();
                    serverChannel.configureBlocking(false);

                    serverChannel.register(selector, SelectionKey.OP_ACCEPT);

                    int connection = 0;
                    while(true){
                        int readyChannels = selector.select();

                        if(readyChannels == 0) continue;

                        Set<SelectionKey> keys = selector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = keys.iterator();

                        while(keyIterator.hasNext()){
                            SelectionKey key = keyIterator.next();
                            //incoming connection
                            if(key.isAcceptable()){
                                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                SocketChannel sc = ssc.accept();
                                sc.configureBlocking(false);
                                SelectionKey connKey = sc.register(selector, SelectionKey.OP_READ);
                                NodeInfo node = new NodeInfo(sc, connKey);
                                slaveList.put(Integer.toString(connection), node);
                                connection++;

                            }
                            else if(key.isReadable()){
                                SocketChannel sc = (SocketChannel) key.channel();
                                readBuffer.clear();
                                byte[] b = new byte[1];
                                for(int i = 0; i < 500; i++)
                                    readBuffer.put(i, b[0]);

                                int bufRead = sc.read(readBuffer);

                                if(bufRead < 0){

                                }

                                String cmdInput = new String(readBuffer.array(),"UTF-8");
                                String tmpBuf[] = cmdInput.split("\0");
                                String args[]=tmpBuf[0].split(" ");
                                if(args[0].equals("restore")){
                                    String slaveDes = args[3];
                                    NodeInfo slaveDesNode = slaveList.get(slaveDes);
                                    sc = slaveDesNode.getSocketChannel();
                                    byte[] out = tmpBuf[0].getBytes(Charset.forName("UTF-8"));
                                    ByteBuffer buffer= ByteBuffer.wrap(out);
                                    sc.write(buffer);
                                }
                            }
                            keyIterator.remove();

                        }
                    }

                } catch (BindException e){
                    System.out.println(e.getMessage());
                    System.exit(1);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });
        listening.start();
    }

    // Start User Console
    public void startConsole(){
        BufferedReader buffInput = new BufferedReader(new InputStreamReader(System.in));
        String cmdInput = "";
        while(true) {
            System.out.print("--> ");
            try {
                cmdInput = buffInput.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String args[] = cmdInput.split(" ");
            SocketChannel sc = null;
            //process input command
            if(args.length == 0){
                continue;
            }

            if (args.length == 1 && args[0].equals("list")) {
                printSlaveList();
            } else if (args.length == 2 && args[0].equals("terminate")) {
                byte[] bytes = cmdInput.getBytes(Charset.forName("UTF-8"));
                ByteBuffer buffer= ByteBuffer.wrap(bytes);
                String slaveId = args[1];
                if(!slaveList.keySet().contains(slaveId)) {
                    System.out.println("Wrong Node ID: no such node connected with master.");
                    continue;
                }
                NodeInfo curSlave = slaveList.get(slaveId);
                try {
                    sc = curSlave.getSocketChannel();
                    sc.write(buffer);
                    slaveList.remove(slaveId);
                    curSlave.getKey().cancel(); // deregister from socketChannel selector
                    sc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            //run process in the specific slave node
            } else if ( args.length >= 3 && args[0].equals("run")) {
                String slaveid = args[1];
                NodeInfo curSlave = slaveList.get(slaveid);
                try {
                    if (curSlave == null) {
                        System.out.println("error: slave not exists!");
                        continue;
                    }
                    sc = curSlave.getSocketChannel();
                    byte[] bytes = cmdInput.getBytes(Charset.forName("UTF-8"));
                    ByteBuffer buffer= ByteBuffer.wrap(bytes);
                    sc.write(buffer);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                //migrate process from one slave node to another
            } else if(args.length >= 3 && args[0].equals("migrate") ){
                String slaveSrc = args[2], slaveDst = args[3];
                if(!slaveList.keySet().contains(slaveSrc) || !slaveList.keySet().contains(slaveDst)){
                    System.out.println("Wrong src or dst ID: src and dst must all be connected with master");
                    continue;
                }
                NodeInfo slaveSrcNode = slaveList.get(slaveSrc);
                try {
                    String strans = "suspend" + " " + args[1] + " " + args[2] + " " + args[3];
                    sc = slaveSrcNode.getSocketChannel();
                    byte[] out = strans.getBytes(Charset.forName("UTF-8"));
                    ByteBuffer buffer= ByteBuffer.wrap(out);
                    sc.write(buffer);
                }
                catch (IOException e ){}
            } else {
                System.out.println("Invalid input");
            }
        }
    }

    public void printSlaveList(){
        Object[] arr = slaveList.keySet().toArray();
        for(int i=0; i<arr.length; i++)
            System.out.println("Slave ID: "+arr[i]+"\t "+slaveList.get(arr[i]).toString());
    }

}
