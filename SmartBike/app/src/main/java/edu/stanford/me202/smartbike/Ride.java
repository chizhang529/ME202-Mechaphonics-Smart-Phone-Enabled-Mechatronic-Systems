package edu.stanford.me202.smartbike;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by czhang on 4/16/17.
 */

public class Ride {
    private int iconID;
    private String location;
    private String date;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public Ride(int iconID, String location, Calendar calendar) {
        this.iconID = iconID;
        this.location = location;
        this.date = dateFormat.format(calendar.getTime());
    }

    public int getIconID() {
        return iconID;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }
}
