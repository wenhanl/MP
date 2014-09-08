import java.io.*;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Created by wenhanl on 14-9-4.
 */
public class NodeInfo {
    public int status;
    private SocketChannel socketChannel;

    NodeInfo(SocketChannel sc){
        this.socketChannel = sc;
    }

    SocketChannel getSocketChannel()
    {
        return socketChannel;
    }

    public String toString(){

        try {
            SocketAddress address = socketChannel.getRemoteAddress();
            return address.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
