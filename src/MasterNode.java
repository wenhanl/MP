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
                    serverChannel.socket().bind(new InetSocketAddress(ClusterConfig.PORT));
                    Selector selector = Selector.open();
                    serverChannel.configureBlocking(false);

                    serverChannel.register(selector, SelectionKey.OP_ACCEPT);

                    int connection = 0;
                    while(true){
                        int readyChannels = selector.select();

                        //if(readyChannels == 0) continue;

                        Set<SelectionKey> keys = selector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = keys.iterator();

                        while(keyIterator.hasNext()){
                            SelectionKey key = keyIterator.next();

                            // Incoming connection
                            if(key.isAcceptable()){
                                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                SocketChannel sc = ssc.accept();
                                sc.configureBlocking(false);
                                SelectionKey connKey = sc.register(selector, SelectionKey.OP_READ);
                                NodeInfo node = new NodeInfo(sc, connKey);
                                slaveList.put(Integer.toString(connection), node);
                                connection++;

                            }
                            // Incoming data
                            else if(key.isReadable()){
                                SocketChannel sc = (SocketChannel) key.channel();
                                readBuffer.clear();
                                byte[] b = new byte[1];
                                for(int i = 0; i < 500; i++)
                                    readBuffer.put(i, b[0]);

                                int bufRead = sc.read(readBuffer);

                                if(bufRead < 0){
                                    System.out.println("Connection lost from one slave");
                                    // Remove from select list
                                    key.cancel();
                                    // Remove from slave list
                                    Object[] set = slaveList.keySet().toArray();
                                    for(int i = 0; i < set.length; i++){
                                        if(slaveList.get(set[i]).getKey().equals(key)){
                                            slaveList.remove(set[i]);
                                        }
                                    }
                                    continue;
                                }

                                String cmdInput = new String(readBuffer.array(),"UTF-8");
                                String tmpBuf[] = cmdInput.split("\0");
                                if(tmpBuf.length == 0) continue;
                                if(tmpBuf[0].equals("NameError")){
                                    System.out.println("Wrong Process Name or process finished running");
                                    System.out.print("--> ");
                                    continue;
                                }
                                if(tmpBuf[0].equals("RunningError")){
                                    System.out.println("Process Already Running");
                                    System.out.print("--> ");
                                    continue;
                                }
                                String args[]=tmpBuf[0].split(" ");
                                if(args[0].equals("restore")){
                                    String slaveDes = args[3];
                                    NodeInfo slaveDesNode = slaveList.get(slaveDes);
                                    sc = slaveDesNode.getSocketChannel();
                                    byte[] out = tmpBuf[0].getBytes(Charset.forName("UTF-8"));
                                    ByteBuffer buffer= ByteBuffer.wrap(out);
                                    sc.write(buffer);
                                }
                                else if(args[0].equals("pinfo")){
                                    System.out.println("---------------");
                                    for(int i=1;i<args.length;i++)
                                        System.out.println(args[i]);
                                    System.out.println("--> ---------------");
                                    System.out.print("-->");
                                }

                            }
                            keyIterator.remove();

                        }
                    }

                } catch (BindException e){
                    System.out.println(e.getMessage());
                    System.exit(1);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
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
                if(cmdInput == null) break;
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            String args[] = cmdInput.split(" ");
            SocketChannel sc = null;
            //process input command
            if(args.length == 0){
                continue;
            }

            if (args.length == 1 && args[0].equals("list")) {
                printSlaveList();
            } else if ((args[0].equals("terminate") || args[0].equals("plist")) && args.length == 2) {
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
                    if(args[0].equals("terminate")) {  // deregister from socketChannel selector
                        slaveList.remove(slaveId);
                        curSlave.getKey().cancel();
                        sc.close();
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            //run process in the specific slave node
            } else if ( args.length >= 3 && args[0].equals("run")) {
                String slaveid = args[1];
                NodeInfo curSlave = slaveList.get(slaveid);
                if (curSlave == null) {
                    System.out.println("error: slave not exists!");
                    continue;
                }
                try {
                    sc = curSlave.getSocketChannel();
                    byte[] bytes = cmdInput.getBytes(Charset.forName("UTF-8"));
                    ByteBuffer buffer= ByteBuffer.wrap(bytes);
                    sc.write(buffer);

                } catch (Exception e) {
                    System.out.println(e.getMessage());
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
                catch (IOException e ){
                    System.out.println(e.getMessage());
                }
            } else if(args[0].equals("help") && args.length == 1) {
                printHelp();

            }else {
                System.out.println("Invalid input");
                printHelp();
            }
        }
    }

    private void printSlaveList(){
        Object[] arr = slaveList.keySet().toArray();
        for(int i=0; i<arr.length; i++)
            System.out.println("Slave ID: "+arr[i]+"\t "+slaveList.get(arr[i]).toString());
    }

    private void printHelp(){
        System.out.println("\tUsage:");
        System.out.println("\t List: list");
        System.out.println("\t Run: run <slave id> <process name> <process arguments...>");
        System.out.println("\t \t process list are <input file> <output file> in our examples");
        System.out.println("\t Migrate: migrate <process name> <src id> <dst id>");
        System.out.println("\t Terminate: terminate <slave id>");
        System.out.println("\t Print Process list: plist <slave id>");
    }

}
