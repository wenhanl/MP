import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by wenhanl on 14-9-4.
 */
public class NodeInfo {
    public int status;
    public Socket sock;
    public DataInputStream input;
    public DataOutputStream output;

    NodeInfo(Socket sock, DataInputStream input, DataOutputStream output){
        this.sock = sock;
        this.input = input;
        this.output = output;
    }
}
