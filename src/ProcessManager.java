
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

        }
        else if (args.length == 3 && args[0].equals("-s")){
            SlaveNode slave = new SlaveNode(args[1], Integer.parseInt(args[2]));
        }
        else {
            // Error message
            System.out.println("Wrong input: pls try again");
            System.exit(1);
        }

    }

}
