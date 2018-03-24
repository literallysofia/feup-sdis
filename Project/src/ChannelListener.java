import java.lang.Runnable;
import java.net.UnknownHostException;

public class ChannelListener implements Runnable{
    private Multicast MC;

    public ChannelListener(Multicast MC){
        this.MC = MC;
    }

    
    public void run(){
        try{
            MC.receive();
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}