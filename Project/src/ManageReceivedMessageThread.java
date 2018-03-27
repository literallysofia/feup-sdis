public class ManageReceivedMessageThread implements Runnable{

    public String msg;

    public ManageReceivedMessageThread(String msg) {
        this.msg=msg;
    }

    public void run(){
        String trimmedMsg = this.msg.trim();
        String[] msgArray=trimmedMsg.split(" ");

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
                managePutchunk(trimmedMsg);
                break;
           //RESTORE
            case "RESTORE":
                break;
            case "CHUNK":
                break;
        }
    }

    public void managePutchunk(String msg){
        System.out.println("MANAGE PUTCHUNK");
    }




}
