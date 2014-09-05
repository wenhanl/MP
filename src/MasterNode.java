import java.util.ArrayList;

/**
 * Created by wenhanl on 14-9-4.
 */
public class MasterNode {
    private ArrayList<NodeInfo> slaveList;

    MasterNode(){
        slaveList = new ArrayList<>();
        // Listen to slave connections
    }
}
