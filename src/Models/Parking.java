package Models;

import jade.core.AID;

import java.util.Date;

public class Parking{
    private AID name;
    private int x;
    private int y;
    private int freeSpace;
    private Date freeSpaceLastUpdate;

    public AID getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getFreeSpace(){
        return freeSpace;
    }

    public Date getFreeSpaceLastUpdate(){
        return freeSpaceLastUpdate;
    }

    public void setFreeSpace(int freeSpace) {
        this.freeSpace = freeSpace;
        this.freeSpaceLastUpdate = new Date();
    }

    public Parking(AID name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.freeSpace = 0;
        this.freeSpaceLastUpdate = null;
    }
}
