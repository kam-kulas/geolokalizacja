package messageTemplate;
import java.io.Serializable;

public class CheckSlotsAvailableContent implements Serializable{
    private boolean isAvailable;

    public CheckSlotsAvailableContent(boolean isAvailable){
        this.isAvailable = isAvailable;
    }

    public boolean checkAvailability() {
        return isAvailable;
    }

    public void setAvailability(boolean isAvailable) { this.isAvailable = isAvailable; }

}