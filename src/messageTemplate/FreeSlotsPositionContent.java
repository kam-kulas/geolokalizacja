package messageTemplate;
import java.io.Serializable;

public class FreeSlotsPositionContent implements Serializable{
    private static int XP;
    private static int YP;

    public FreeSlotsPositionContent(int XP, int YP){
        this.XP = XP;
        this.YP = YP;
    }

    public int getXP() {
        return XP;
    }
    public int getXY() {
        return YP;
    }

}