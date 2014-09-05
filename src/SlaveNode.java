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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
