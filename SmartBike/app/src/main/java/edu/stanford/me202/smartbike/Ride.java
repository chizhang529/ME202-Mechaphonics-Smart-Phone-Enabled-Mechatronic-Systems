package edu.stanford.me202.smartbike;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by czhang on 4/16/17.
 */

public class Ride extends RealmObject{

    @PrimaryKey
    @Required
    private String timeStamp;
    @Required
    private String date;
    @Required
    private String location;
    private int iconID;

    public Ride() {
        // need an empty constructor to make Realm work and make Firebase deserialize
    }

    public Ride(int iconID, String location, String dateToday, String timeStamp) {
        this.iconID = iconID;
        this.location = location;
        this.date = dateToday;
        this.timeStamp = timeStamp;
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

    public String getTimeStamp() {return timeStamp;}
}
