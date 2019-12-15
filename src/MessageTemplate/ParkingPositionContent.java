package MessageTemplate;
import java.io.Serializable;

public class ParkingPositionContent implements Serializable{
    private int XP;
    private int YP;

    public ParkingPositionContent(int XP, int YP){
        this.XP = XP;
        this.YP = YP;
    }

    public int getXP() {
        return XP;
    }
    public int getYP() {
        return YP;
    }

}