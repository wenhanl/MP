import java.io.*;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by wenhanl on 14-9-4.
 */
public class NodeInfo {
    private SocketChannel socketChannel;
    private SelectionKey key;

    NodeInfo(SocketChannel sc, SelectionKey key){
        this.key = key;
        this.socketChannel = sc;
    }

    SocketChannel getSocketChannel()
    {
        return socketChannel;
    }

    SelectionKey getKey(){
        return key;
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
