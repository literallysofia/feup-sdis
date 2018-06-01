
package Peer;

public abstract class Node{
    
    private static int receiverCounter = 0;

    public Node(){}

    /**
     * @return the receiverCounter
     */
    public static int getReceiverCounter() {
        return receiverCounter;
    }

    /**
     * @param receiverCounter the receiverCounter to set
     */
    public static void incReceiverCounter() {
        receiverCounter ++;
    }

     /**
     * @param receiverCounter the receiverCounter to set
     */
    public static void decReceiverCounter() {
        receiverCounter --;
    }

}