package com.wmcalyj.eashtracking.service;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.wmcalyj.eashtracking.Constants;
import com.wmcalyj.eashtracking.RecentlySearchedTrackingNumber;
import com.wmcalyj.eashtracking.TrackingNumber;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by mengchaowang on 1/5/17.
 */

public class FileService {
    private static final String MyTag = "FileServiceTag";
    private static FileService instance;

    private FileService() {
        // Nothing
    }

    public static FileService getInstance() {
        if (instance == null) {
            instance = new FileService();
        }
        return instance;
    }

    public boolean saveRecentlySearchedTrackingNumbers(Context context) {
        try (FileOutputStream fos = context.openFileOutput(Constants.FILE_NAME,
                Context.MODE_PRIVATE)) {
            String allIdStrings = RecentlySearchedTrackingNumber.getInstance().convertToStrings();
            Log.d(MyTag, "Save all Id Strings" + allIdStrings);
            fos.write(allIdStrings.getBytes());
        } catch (IOException e) {
            Log.e(MyTag, e.getMessage());
            return false;
        }
        return true;
    }


    public TrackingNumber loadRecentlySearchedTrackingNumbers(Context context) {
        Gson gson = new Gson();
        try (FileInputStream fis = context.openFileInput(Constants.FILE_NAME)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String jsonString = gson.toJson(gson.fromJson(reader, JsonElement
                    .class));
            Log.d(MyTag, "Reading json string: " + jsonString);
            TrackingNumber header = gson.fromJson(jsonString, TrackingNumber.class);
            // This is necessary because we made prev trasient, now we have to link them together
            linkBackwardsForHeader(header);
            Log.d(MyTag, "header is: " + header == null ? "null" : header.id);
            return header;
        } catch (Exception e) {
            Log.e(MyTag, e.getMessage());
        }
        return null;
    }

    private void linkBackwardsForHeader(TrackingNumber header) {
        if (header == null) {
            return;
        }
        TrackingNumber next = header.next;
        TrackingNumber prev = header;
        while (next != null) {
            next.prev = prev;
            prev = next;
            next = next.next;
        }
    }
}


