//package transactionIO.process;

/**
 * Created with IntelliJ IDEA.
 * User: kevin, prashanth
 * Date: 9/8/13
 */

public class CountProcess implements MigratableProcess {
    private String processName;
    private int maxValue;
    private int currentValue;
    private volatile boolean suspending;

    public CountProcess(String args[]) throws Exception {
        if (args.length != 1) {
            System.out.println("usage: CountProcess <maxValue>");
            throw new Exception("Invalid Arguments");
        }

        try {
            maxValue = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            System.out.println("<maxValue> must be an integer.");
            throw new Exception("Invalid Arguments");
        }
        currentValue = 0;
    }


    public void run() {
        while (!suspending) {
            System.out.println(currentValue);
            currentValue++;
            try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // ignore it
                }
            if(currentValue > maxValue) {
                System.out.println("Done!");
                break;
            }
            // Make count take longer so that we don't require extremely large numbers for interesting results
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore it
            }
        }

        suspending = false;
    }


    public void suspend() {
        suspending = true;
        while (suspending);
    }


    public String toString() {
        return "Process[CountProcess " + maxValue + "]";
    }


    public String getProcessName() {
        return processName;
    }


    public void setProcessName(String processName) {
        this.processName = processName.replace(" ", "_");
    }

}