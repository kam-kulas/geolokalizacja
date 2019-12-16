import jade.core.AID;

public class UserSession {

    public UserSession(AID customerId, String conversationId, int posX, int posY){
        this.customerId = customerId;
        this.conversationId = conversationId;
        this.posX = posX;
        this.posY = posY;

        requestAboutFreeSpaces = 0;
        IsDoneCheckFreeSpaces = false;
    }

    private AID customerId;
    private String conversationId;
    private int posX;
    private int posY;


    private int requestAboutFreeSpaces;
    public boolean IsDoneCheckFreeSpaces;



    public void setRequestAboutFreeSpaces(int requestAboutFreeSpaces) {
        this.requestAboutFreeSpaces = requestAboutFreeSpaces;
    }

    public void decrementRequestAboutFreeSpaces(){
        requestAboutFreeSpaces--;
        if(requestAboutFreeSpaces==0)
            IsDoneCheckFreeSpaces = true;
    }

    public AID getCustomerId() {
        return customerId;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public String getConversationId() {
        return conversationId;
    }
}
