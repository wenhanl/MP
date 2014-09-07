
import java.io.IOException;
import java.net.InetSocketAddress;
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
    private SocketChannel sc;

    SlaveNode(){
        try {

            // Open a socket channel connecting to master node on port 15640
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress("localhost", 15640));
            sc.configureBlocking(false);

            // Create selector and bind socketChannel to it
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ);

            while(true){
                // Use select to block until something readable in channel
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
                        for(int i=0; i<cmdTmpInput.length(); i++)
                            if((int)cmdTmpInput.charAt(i)!=0)
                                tmp.append(cmdTmpInput.charAt(i));
                        String cmdInput = tmp.toString();

                        String args[]=cmdInput.split(" ");
                        if(args[0].equals("run")){

                            MigratableProcess mpProcess = null;
                            try {
                                Class<MigratableProcess> mpClass = (Class<MigratableProcess>) Class.forName(args[2]);
                                Constructor<?> mpConstructor = mpClass.getConstructor(String[].class);
                                Object[] mpArgs = { Arrays.copyOfRange(args, 3, args.length) };
                                mpProcess = (MigratableProcess) mpConstructor.newInstance(mpArgs);
                            } catch (ClassNotFoundException e) {
                                System.err.format("Class Not Found: Can't find class with provided class name", e.getMessage());
                            } catch (NoSuchMethodException e) {
                                System.err.format("No such method: No such constructor", e.getMessage());
                            } catch (InstantiationException e) {
                                System.err.format("Instantiation Error:", e.getMessage());
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
