import java.util.ArrayList;

/**
 * User command-line interface. Process manager.
 */
public class ProcessManager {
    private ArrayList<NodeInfo> list; // list of running processes
    private MasterNode master;

    ProcessManager(){
        list = new ArrayList<>();
        master = new MasterNode();
    }

    public static void main(String args[]){
        ProcessManager manager = new ProcessManager();


    }

}
