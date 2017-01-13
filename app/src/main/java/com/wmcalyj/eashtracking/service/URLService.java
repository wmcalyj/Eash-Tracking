package com.wmcalyj.eashtracking.service;

import android.util.Log;

import com.wmcalyj.eashtracking.TrackingNumber;

import java.security.InvalidParameterException;

/**
 * Created by mengchaowang on 1/6/17.
 */
public class URLService {
    private static final String MyTag = "URLServiceTag";
    private static URLService ourInstance = new URLService();

    private URLService() {
    }

    public static URLService getInstance() {
        return ourInstance;
    }

    public String getRequestUrl(TrackingNumber trackingNumber) {
        String carrier = CarrierService.getInstance().getCarrier(trackingNumber);
        if (carrier == null) {
            throw new InvalidParameterException("Cannot find carrier: " + trackingNumber.carrier);
        }
        String number = trackingNumber.trackingNumber;

        return getRequestUrl(number, carrier);

    }

    public String getRequestUrl(String trackingNumber, String carrier) {
        String validatedCarrier = CarrierService.getInstance().getCarrier(carrier);

        StringBuilder url = new StringBuilder();
        switch (validatedCarrier) {
            case "ups":
                url.append("https://wwwapps.ups.com/WebTracking/track?track=yes&trackNums=").append
                        (trackingNumber);
                break;
            case "usps":
                url.append("https://tools.usps.com/go/TrackConfirmAction?tLabels=").append
                        (trackingNumber);
                break;
            case "dhl":
                url.append("http://www.dhl.com/en/express/tracking.html?AWB=").append
                        (trackingNumber).append("&brand=DHL");
            case "fedex":
                url.append("https://www.fedex.com/apps/fedextrack/?tracknumbers=").append
                        (trackingNumber);
                break;

        }
        Log.d(MyTag, "Requested url is: " + url.toString());
        return url.toString();
    }

}
