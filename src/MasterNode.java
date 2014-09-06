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
        while(true)
        {
            System.out.print("--> ");
            try {
                cmdInput = buffInput.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String args[]=cmdInput.split(" ");
            if(args.length==0)
                continue;
            else if(args.length==1){
                if(args[0].equals("list"))
                    printslaveList();
            }
            else if(args.length==2){
                if(args[0].equals("terminate")){
                    /* TODO */
                }
            }
            else if(args.length==3){
                if(args[0].equals("run")){
                    int slaveid = Integer.parseInt(args[2]);
                    NodeInfo curslave = slaveList.get(slaveid);
                    try {
                        //byte[] data = cmdInput.getBytes();
                        curslave.getoutputstream().writeInt((cmdInput.getBytes().length));
                        curslave.getoutputstream().write(cmdInput.getBytes());
                        curslave.getoutputstream().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else if(args[0].equals("migrate")){
                    /* TODO */
                }
            }
        }
    }
    public void printslaveList(){
        /* TODO */

    }

   /* public void start(){
        // Start socket sever Listen to slave connections
        try {
            socketServer = new ServerSocket(PORT);
            int count = 0;
            while(true){
                Socket sock = socketServer.accept();
                DataInputStream input = new DataInputStream(sock.getInputStream());
                DataOutputStream output = new DataOutputStream(sock.getOutputStream());
                NodeInfo slave = new NodeInfo(count, sock, input, output);
                slaveList.add(slave);
                count++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }*/
}
