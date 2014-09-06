import java.util.ArrayList;

/**
 * Process manager.
 *
 * run ProcessManager directly to start a master node
 * run ProcessManager -s to start a slave node
 */
public class ProcessManager {

    public static void main(String args[]){

        if(args.length == 0) { // Start master
            MasterNode master = new MasterNode();
<<<<<<< HEAD

            // master.start();
=======
>>>>>>> 8aa7f7cc9a26fc819376b702d310a042f8e7802b
            // master.run();
        }
        else if (args.length >= 1 && args[0].equals("-s")){
            SlaveNode slave = new SlaveNode();
        }
        else {
            // Error message
        }

    }

}
