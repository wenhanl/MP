import java.io.*;
import java.net.Socket;

/**
 * Created by wenhanl on 14-9-4.
 */
public class NodeInfo {
    public int status;
    private Socket sock;
    private DataInputStream input;
    private DataOutputStream output;

    NodeInfo(Socket sock, DataInputStream input, DataOutputStream output){
        this.sock = sock;
            this.input = input;
            this.output = output;
    }
    DataInputStream getinputstream(){
        return input;
    }
    DataOutputStream getoutputstream(){
        return output;
    }


}
