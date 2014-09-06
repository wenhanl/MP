import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by wenhanl on 14-9-4.
 */
public class NodeInfo {
    public int status;
    private Socket sock;
    private DataInputStream input;
    private DataOutputStream output;
    private int id;

    NodeInfo(int id, Socket sock, DataInputStream input, DataOutputStream output){
        this.sock = sock;
        this.input = input;
        this.output = output;
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
