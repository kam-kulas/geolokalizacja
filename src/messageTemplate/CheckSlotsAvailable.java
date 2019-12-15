package messageTemplate;
import java.io.Serializable;

public class CheckSlotsAvailable implements Serializable{
    private boolean isAvailable;

    public CheckSlotsAvailable(boolean isAvailable){
        this.isAvailable = isAvailable;
    }

    public boolean checkAvailability() {
        return isAvailable;
    }

    public void setAvailability(boolean isAvailable) { this.isAvailable = isAvailable; }

}