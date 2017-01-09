package wmcalyj.eashtracking;

import org.jsoup.helper.StringUtil;

import java.io.Serializable;
import java.security.InvalidParameterException;

/**
 * Created by mengchaowang on 1/5/17.
 */

public class TrackingNumber implements Serializable {
    public String id;
    public String trackingNumber;
    public String carrier;
    public String comment;
    public TrackingNumber next;
    // make prev transient so there is no stackoverflow when using gson
    public transient TrackingNumber prev;

    public TrackingNumber(String trackingNumber, String carrier) {
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        next = null;
        prev = null;
        id = trackingNumber + Constants.TRACKINGNUMBER_DELIMITER + carrier;
    }

    public TrackingNumber(String trackingNumberId) {
        int idx = trackingNumberId.indexOf(Constants.TRACKINGNUMBER_DELIMITER);
        if (idx == -1) {
            throw new InvalidParameterException("Invalid Tracking Number Id");
        }
        this.trackingNumber = trackingNumberId.substring(0, idx);
        this.carrier = trackingNumberId.substring(idx + Constants.TRACKINGNUMBER_DELIMITER.length
                ());
        this.id = trackingNumberId;
        next = null;
        prev = null;
    }

    public static TrackingNumber getDummyHeader() {
        return new TrackingNumber(Constants.DUMMY_TRACKINGNUMBER_NUMBER, Constants
                .DUMMY_TRACKINGNUMBER_CARRIER);
    }

    public boolean hasComment() {
        return !StringUtil.isBlank(this.comment);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !TrackingNumber.class.isAssignableFrom(o.getClass())) {
            return false;
        }
        return (this.trackingNumber.equals(((TrackingNumber) o).trackingNumber) && this.carrier
                .equals((((TrackingNumber) o).carrier)));
    }

    @Override
    public int hashCode() {
        return (trackingNumber + carrier).hashCode();
    }

    @Override
    public String toString() {
        return id;
    }

}
