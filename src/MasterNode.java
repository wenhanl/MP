/**
 * Created by wenhanl on 14-9-4.
 */

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MasterNode {
    private HashMap<Integer,NodeInfo> slaveList;
    public static final int PORT = 15640;
    private ByteBuffer readBuffer;

    MasterNode(){
        slaveList = new HashMap<>();

        // Listen to slave connections
        startListener();

        // Start user console
        startConsole();
    }

    // Open a background thread to listen to slave connection.
    public void startListener(){
        Thread listening = new Thread(new Runnable() {
            public void run()
            {
                try {

                    ServerSocketChannel serverChannel = ServerSocketChannel.open();
                    serverChannel.socket().bind(new InetSocketAddress(PORT));
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

                            if(key.isAcceptable()){
                                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                SocketChannel sc = ssc.accept();
                                sc.configureBlocking(false);
                                sc.register(selector, SelectionKey.OP_READ);
                                NodeInfo node = new NodeInfo(sc);
                                slaveList.put(connection, node);
                                connection++;

                            }
                            else if(key.isReadable()){

                            }

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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

            if (args.length == 0) {
                continue;
            } else if (args[0].equals("list") && args.length == 1) {
                printslaveList();
            } else if (args[0].equals("terminate") && args.length == 2) {

                int slaveId = Integer.parseInt(args[1]);
                NodeInfo curSlave = slaveList.get(slaveId);
                try {
                    sc = curSlave.getSocketChannel();
                    byte[] bytes = cmdInput.getBytes(Charset.forName("UTF-8"));
                    ByteBuffer buffer= ByteBuffer.wrap(bytes);
                    sc.write(buffer);
                    sc.close();
                    slaveList.put(slaveId, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                slaveList.remove(slaveId);

            } else if (args[0].equals("run") && args.length >= 3) {
                int slaveid = Integer.parseInt(args[1]);
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
            } else if(args[0].equals("migrate") && args.length >= 3){

            } else {
                System.out.println("Invalid input");
            }
        }
    }

    public void printslaveList(){
        Object[] arr = slaveList.keySet().toArray();
        for(int i=0;i<arr.length;i++)
            System.out.println("Slave ID: "+i+"\t "+slaveList.get(i).toString());
    }

}
