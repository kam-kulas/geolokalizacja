package messageTemplate;
import java.io.Serializable;

public class ReservationContent implements Serializable{
    private boolean reservationStatus;

    public ReservationContent(boolean reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public boolean getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(boolean reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}