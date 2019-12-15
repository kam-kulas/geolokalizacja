package messageTemplate;

import java.io.Serializable;

public class FindFreeSlotsContent implements Serializable{
    private int positionX;
    private int positionY;

    public FindFreeSlotsContent(int positionX, int positionY){
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
}
