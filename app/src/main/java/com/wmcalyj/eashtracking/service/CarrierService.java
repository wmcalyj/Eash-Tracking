package com.wmcalyj.eashtracking.service;

import com.wmcalyj.eashtracking.TrackingNumber;

/**
 * Created by mengchaowang on 1/6/17.
 */
public class CarrierService {
    private static CarrierService ourInstance = new CarrierService();

    private CarrierService() {
    }

    public static CarrierService getInstance() {
        return ourInstance;
    }

    public String getCarrier(TrackingNumber trackingNumber) {
        String carrier = trackingNumber.carrier;
        return getCarrier(carrier);
    }

    public String getCarrier(String carrier) {
        if (carrier.toLowerCase().equals("ups")) {
            return "ups";
        } else if (carrier.toLowerCase().equals("usps")) {
            return "usps";
        } else if (carrier.toLowerCase().equals("dhl")) {
            return "dhl";
        } else if (carrier.toLowerCase().equals("fedex")) {
            return "fedex";
        } else {
            // TODO
        }
        return "";
    }
}
