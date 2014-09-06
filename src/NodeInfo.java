import java.io.*;
import java.net.Socket;

/**
 * Created by wenhanl on 14-9-4.
 */
public class NodeInfo {
    public int status;
    private Socket sock;
    private BufferedReader input;
    private PrintWriter output;

    NodeInfo(Socket sock, DataInputStream input, DataOutputStream output){
        this.sock = sock;
        try {
            this.input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            this.output = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
        }catch(IOException e){e.printStackTrace();}
        //sock.getInputStream()
    }
    BufferedReader getBufferedReader(){
        return input;
    }
    PrintWriter getPrintWriter(){
        return output;
    }


}
