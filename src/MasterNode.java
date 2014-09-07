/**
 * Created by wenhanl on 14-9-4.
 */

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.HashMap;

public class MasterNode {
    private HashMap<Integer,NodeInfo> slaveList;
    private ServerSocket socketServer;
    public static final int PORT = 15640;

    MasterNode(){
        slaveList = new HashMap<>();

        // Listen to slave connections
        Thread listening = new Thread(new Runnable() {
            public void run()
            {
                try {
                    socketServer = new ServerSocket(PORT);
                    int count = 0;
                    while(true){
                        Socket sock = socketServer.accept();
                        DataInputStream input = new DataInputStream(sock.getInputStream());
                        DataOutputStream output = new DataOutputStream(sock.getOutputStream());
                        NodeInfo slave = new NodeInfo(sock, input, output);
                        slaveList.put(count,slave);
                        count++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listening.start();
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

            /* TODO */
            // Arguments error handling
            if (args.length == 0)
                continue;
            else if (args.length == 1) {
                if (args[0].equals("list"))
                    printslaveList();
            } else if (args.length == 2) {
                if (args[0].equals("terminate")) {
                    int slaveid = Integer.parseInt(args[1]);
                    NodeInfo curslave = slaveList.get(slaveid);
                    try {
                        curslave.getoutputstream().writeChars(cmdInput);
                        curslave.getsocket().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    slaveList.remove(slaveid);
                }
            } else if (args.length >= 3) {
                if (args[0].equals("run")) {
                    int slaveid = Integer.parseInt(args[1]);
                    NodeInfo curslave = slaveList.get(slaveid);
                    try {
                        if (curslave == null) {
                            System.out.println("error: slave not exists!");
                            continue;
                        }
                        curslave.getoutputstream().writeChars(cmdInput);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (args[0].equals("migrate")) {
                    /* TODO */
                }
            }
        }

    }

    public void printslaveList(){
        Object[] arr = slaveList.keySet().toArray();
        for(int i=0;i<arr.length;i++)
            System.out.println("Slave ID: "+i+"\t "+slaveList.get(i).toString());
    }

}
