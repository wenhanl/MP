import java.util.ArrayList;
import java.io.*;
/**
 * Created by wenhanl on 14-9-4.
 */
public class MasterNode {
    private ArrayList<NodeInfo> slaveList;

    MasterNode(){
        slaveList = new ArrayList<>();
        // Listen to slave connections
        BufferedReader buffInput = new BufferedReader(new InputStreamReader(System.in));
        String cmdInput = "";
        while(true)
        {
            System.out.print("--> ");
            try {
                cmdInput = buffInput.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String args[]=cmdInput.split(" ");
            if(args.length==0)
                continue;
            else if(args.length==1){
                if(args[0].equals("list"))
                    printslaveList();
            }
            else if(args.length==2){
                if(args[0].equals("terminate")){
                    /* TODO */
                }
            }
            else if(args.length==3){
                if(args[0].equals("run")){
                    /* TODO */
                }
                else if(args[0].equals("migrate")){
                    /* TODO */
                }
            }
        }
    }
    void printslaveList(){
        /* TODO */
    }
}
