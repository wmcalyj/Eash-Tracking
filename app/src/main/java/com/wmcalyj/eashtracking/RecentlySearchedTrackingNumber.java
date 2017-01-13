package com.wmcalyj.eashtracking;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.wmcalyj.eashtracking.service.FileService;

import org.jsoup.helper.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mengchaowang on 1/5/17.
 */

public class RecentlySearchedTrackingNumber {
    private static final long serialVersionUID = 1L;

    private static final String MyTag = "SearchedTrackingNumTag";
    private static RecentlySearchedTrackingNumber instance;
    private Map<String, TrackingNumber> cacheMap;
    private TrackingNumber header;

    private RecentlySearchedTrackingNumber() {
        cacheMap = new HashMap<String, TrackingNumber>();
    }

    public static RecentlySearchedTrackingNumber getInstance() {
        if (instance == null) {
            instance = new RecentlySearchedTrackingNumber();
        }
        return instance;
    }

    public static RecentlySearchedTrackingNumber loadInstance(Context context) {
        TrackingNumber header = FileService.getInstance().loadRecentlySearchedTrackingNumbers
                (context);
        if (instance == null) {
            instance = new RecentlySearchedTrackingNumber();
        }
        instance.header = header;
        loadCacheMap(instance.cacheMap, instance.header);
        Log.d(MyTag, "After loading: " + instance.convertToStrings());
        return instance;

    }

    private static void loadCacheMap(Map<String, TrackingNumber> cacheMap,
                                     TrackingNumber header) {
        while (header != null) {
            cacheMap.put(header.id, header);
            header = header.next;
        }
    }

    public boolean isEmpty() {
        return this.header == null;
    }

    public TrackingNumber getHeader() {
        if (this.header != null) {
            Log.d(MyTag, "First tracking number is: " + this.header.toString());
        } else {
            Log.d(MyTag, "header is null");
        }
        return this.header;
    }

    public void addTrackingNumber(String trackingNumber, String carrier) {
        if (isEmpty()) {
            this.header = new TrackingNumber(trackingNumber, carrier);
            if (this.cacheMap == null) {
                this.cacheMap = new HashMap<>();
            }
            this.cacheMap.put(this.header.id, this.header);
        } else {
            String id = generateId(trackingNumber, carrier);
            if (this.cacheMap != null && this.cacheMap.containsKey(id)) {
                Log.d(MyTag, "Key found: " + id);
                reorderSearchHistory(id);
            } else {
                TrackingNumber next = new TrackingNumber(trackingNumber, carrier);
                next.next = this.header;
                this.header.prev = next;
                this.header = next;
                this.cacheMap.put(next.id, next);
            }
        }
    }

    public void validate() {
        TrackingNumber tmp = header;
        StringBuilder sb = new StringBuilder();
        while (tmp != null) {
            sb.append(tmp.id).append("\n");
            tmp = tmp.next;
        }
        Log.d(MyTag, "Validate: \n" + sb.toString());
    }

    private void reorderSearchHistory(String id) {
        TrackingNumber hit = this.cacheMap.get(id);
        if (hit != this.header) {
            if (hit.prev != null) {
                hit.prev.next = hit.next;
            }
            if (hit.next != null) {
                hit.next.prev = hit.prev;
            }
            hit.next = this.header;
            this.header.prev = hit;

            hit.prev = null;
            this.header = hit;
        }
    }

    public String generateId(String trackingNumber, String carrier) {
        return trackingNumber + "--" + carrier;
    }

    public TrackingNumber getTrackingNumber(String id) {
        return cacheMap.get(id);
    }

    public void removeTrackingNumberById(String trackingNumberId) {
        TrackingNumber toBeRemoved = getTrackingNumber(trackingNumberId);
        if (toBeRemoved == header) {
            if (toBeRemoved.next == null) {
                // Remove the only item in the list
                header = null;
            } else {
                header = header.next;
                header.prev = null;
            }
        } else {
            toBeRemoved.prev.next = toBeRemoved.next;
            if (toBeRemoved.next != null) {
                toBeRemoved.next.prev = toBeRemoved.prev;
            }
        }
        cacheMap.remove(trackingNumberId);
    }

    public String convertToStrings() {
        Gson gson = new Gson();
        TrackingNumber tmp = this.header;
        String gsonString = gson.toJson(tmp);
        Log.d(MyTag, "gson string " + gsonString);
        return gsonString;
    }

    public boolean hasCommentForId(String trackingId) {
        if (cacheMap == null || cacheMap.get(trackingId) == null) {
            return false;
        }
        return cacheMap.get(trackingId).hasComment();
    }

    public boolean saveComment(String trackingId, String comment) {
        if (StringUtil.isBlank(comment)) {
            return true;
        }
        if (cacheMap == null || cacheMap.get(trackingId) == null) {
            return false;
        }
        cacheMap.get(trackingId).comment = comment;
        return true;
    }

    public String getComment(String trackingId) {
        if (StringUtil.isBlank(trackingId)) {
            return "";
        }
        if (cacheMap == null || cacheMap.get(trackingId) == null) {
            return "";
        }
        return cacheMap.get(trackingId).comment;
    }

    public void deleteCommentForId(String trackingId) {
        if (StringUtil.isBlank(trackingId) || cacheMap == null || cacheMap.get(trackingId) ==
                null) {
            return;
        }
        cacheMap.get(trackingId).comment = null;
    }

    public void clearAllHistoryRecords() {
        if (cacheMap != null) {
            cacheMap.clear();
        }
        if (header != null) {
            TrackingNumber tmp = header;
            while (tmp != null && tmp.next != null) {
                tmp = tmp.next;
            }
            TrackingNumber prev = tmp.prev;
            while (prev != null && prev.prev != null) {
                tmp = null;
                tmp = prev;
                prev = prev.prev;
            }
            prev = null;
            header = null;
            instance = null;
        }
    }
}
