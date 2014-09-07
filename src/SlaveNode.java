import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Arrays;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by wenhanl on 14-9-4.
 */
public class SlaveNode {
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    SlaveNode(){
        try {
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
                        ByteBuffer buf = ByteBuffer.allocate(500);
                        int bufRead = sc.read(buf);

                        if(bufRead == -1){
                            System.out.println("Read error");
                        }

                        String cmdTmpInput = new String(buf.array());
                        StringBuilder tmp = new StringBuilder();
                        for(int i=0;i<cmdTmpInput.length();i++)
                            if((int)cmdTmpInput.charAt(i)!=0)
                                tmp.append(cmdTmpInput.charAt(i));
                        String cmdInput = tmp.toString();

                        String args[]=cmdInput.split(" ");
                        if(args[0].equals("run")){
                            Class<MigratableProcess> mpClass = null;
                            Constructor<?> mpConstructor = null;
                            MigratableProcess mpProcess = null;
                            try {
                                mpClass = (Class<MigratableProcess>) Class.forName(args[2]);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            try {
                                mpConstructor = mpClass.getConstructor(String[].class);
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            Object[] mpArgs = { Arrays.copyOfRange(args, 3, args.length) };
                            try {
                                mpProcess = (MigratableProcess) mpConstructor.newInstance(mpArgs);
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                            Thread tp = new Thread(mpProcess);
                            tp.start();
                        }
                        else if(args[0].equals("terminate"))
                            System.exit(1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
