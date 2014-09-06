import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by wenhanl on 14-9-4.
 */
public class SlaveNode {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    SlaveNode(){
        try {
            socket = new Socket("localhost", 15640);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            while(true){
                int length = in.readInt();
                byte[] data = new byte[length];
                if(in.read(data,0,length)>0) {
                    String cmdInput = new String(data);
                    System.out.println(cmdInput);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
