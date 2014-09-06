import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by wenhanl on 14-9-4.
 */
public class MasterNode {
    private ArrayList<NodeInfo> slaveList;
    private ServerSocket socketServer;
    public static final int PORT = 15640;


    MasterNode(){
        slaveList = new ArrayList<>();
    }

    public void start(){
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

    }
}
