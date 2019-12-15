import jade.core.AID;

public class UserSession {

    public UserSession(AID customerId, String conversationId){
        this.customerId = customerId;
        this.conversationId = conversationId;

        requestAboutFreeSpaces = 0;
        IsDoneCheckFreeSpaces = false;
    }

    public AID customerId;
    public String conversationId;

    private int requestAboutFreeSpaces;


    //steps
    public boolean IsDoneCheckFreeSpaces;

    public void setRequestAboutFreeSpaces(int requestAboutFreeSpaces) {
        this.requestAboutFreeSpaces = requestAboutFreeSpaces;
    }

    public void decrementRequestAboutFreeSpaces(){
        requestAboutFreeSpaces--;
        if(requestAboutFreeSpaces==0)
            IsDoneCheckFreeSpaces = true;
    }
}
