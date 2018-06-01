public class SendMessageThread implements Runnable {

    private byte[] msg;
    private String multicastType;


    public SendMessageThread(byte[] msg, String multicastType) {
        this.msg = msg;
        this.multicastType = multicastType;
    }

    public void run() {
        switch (multicastType) {
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
