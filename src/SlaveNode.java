import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by wenhanl on 14-9-4.
 */
public class SlaveNode {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    SlaveNode(){
        try {

            /*
            while(true){
                int length = in.readInt();
                byte[] data = new byte[length];
                if(in.read(data,0,length)>0) {
                    String cmdInput = new String(data);
                    System.out.println(cmdInput);
                }
             */

            //SocketChannel sc = socket.getChannel();

            SocketChannel sc = SocketChannel.open();
            sc.connect(new InetSocketAddress("localhost", 15640));
            sc.configureBlocking(false);


            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ);

            while(true){
                selector.select();

                Set<SelectionKey> keySet = selector.selectedKeys();

                Iterator<SelectionKey> keyIterator = keySet.iterator();

                while(keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if(key.isReadable()){
                        ByteBuffer buf = ByteBuffer.allocate(50);
                        int bufRead = sc.read(buf);

                        if(bufRead == -1){
                            System.out.println("Read error");
                        }


                        System.out.println(new String(buf.array()));


                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
