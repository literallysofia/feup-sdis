public class SentMessageThread implements Runnable{

    private String msg;
    private String multicastType;


    public SentMessageThread(String msg, String multicastType) {
        this.msg=msg;
        this.multicastType = multicastType;
    }

    public void run(){
        switch (multicastType){
            case "MC":
                Peer.getMC().sendMessage(this.msg);
                break;
            case "MDB":
                Peer.getMDB().sendMessage(this.msg);
                break;
            case "MDR":
                Peer.getMDR().sendMessage(this.msg);
                break;
        }
    }
}
