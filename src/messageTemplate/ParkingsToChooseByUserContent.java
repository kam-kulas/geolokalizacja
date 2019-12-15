package messageTemplate;

import Models.Parking;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;

public class ParkingsToChooseByUserContent implements Serializable {

    public ParkingsToChooseByUserContent(List<Parking> parkings){
        this.Availableparkings = parkings;
    }

    public List<Parking> Availableparkings;

    public void Addparking(Parking parking){
        Availableparkings.add(parking);
    }
}
