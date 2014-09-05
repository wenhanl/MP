package transactionIO;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by wenhanl on 14-9-2.
 */
public class TransactionalFileInputStream extends InputStream implements Serializable{
    private String str;

    public TransactionalFileInputStream(String str){
        this.str = str;
    }

    public int read(){
        return 1;
    }
}
