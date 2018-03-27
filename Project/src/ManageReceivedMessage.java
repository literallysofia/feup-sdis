public class ManageReceivedMessage implements Runnable{

    public String msg;

    public ManageReceivedMessage(String msg) {
        this.msg=msg;
    }

    public void run(){
        String trimmedMg = this.msg.trim();
        String[] msgArray=trimmedMg.split(" ");

        switch(msgArray[0]){
            //CONTROL
            case "STORED":
                break;
            case "GETCHUNK":
                break;
            case "DELETE":
                break;
            case "REMOVED":
                break;
            case "STATUS":
                break;
            //BACKUP
            case "PUTCHUNK":
                break;
           //RESTORE
            case "RESTORE":
                break;
            case "CHUNK":
                break;
        }
    }


}
