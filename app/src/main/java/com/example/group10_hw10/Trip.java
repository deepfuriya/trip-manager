package com.example.group10_hw10;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Trip implements Serializable {

    public String location,user_id;
    public Timestamp start_time,complete_time;
    public Integer complete;
    public Double total_miles,start_lat,start_long,end_lat,end_long;

    public Trip() {
    }

    public Trip(HashMap<String,Object> val){
        this.location = (String) val.get("location");
        this.start_time = (Timestamp) val.get("start_time");
        this.complete_time = (Timestamp) val.get("complete_time");
        this.complete = (Integer) val.get("complete");
        this.total_miles = (Double) val.get("total_miles");
        this.user_id = (String) val.get("user_id");
        this.start_lat = (Double) val.get("start_lat");
        this.start_long = (Double) val.get("start_long");
        this.end_lat = (Double) val.get("end_lat");
        this.end_long = (Double) val.get("end_long");
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Double getStart_lat() {
        return start_lat;
    }

    public void setStart_lat(Double start_lat) {
        this.start_lat = start_lat;
    }

    public Double getStart_long() {
        return start_long;
    }

    public void setStart_long(Double start_long) {
        this.start_long = start_long;
    }

    public Double getEnd_lat() {
        return end_lat;
    }

    public void setEnd_lat(Double end_lat) {
        this.end_lat = end_lat;
    }

    public Double getEnd_long() {
        return end_long;
    }

    public void setEnd_long(Double end_long) {
        this.end_long = end_long;
    }

    public String getStart_time() {
        String date = null;
        if (!this.start_time.toString().isEmpty()) {
            final String preConverted = this.start_time.toString();
            final int _seconds = Integer.parseInt(preConverted.substring(18, 28)); // 1621176915
            final int _nanoseconds = Integer.parseInt(preConverted.substring(42, preConverted.lastIndexOf(')'))); // 276147000
            final com.google.firebase.Timestamp postConverted = new com.google.firebase.Timestamp(_seconds, _nanoseconds);

            date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(postConverted.getSeconds() * 1000));
        }
        return date;
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = start_time;
    }

    public String getComplete_time() {
        String date = null;
        if (this.complete_time == null) {
        }else{
            final String preConverted = this.complete_time.toString();
            final int _seconds = Integer.parseInt(preConverted.substring(18, 28)); // 1621176915
            final int _nanoseconds = Integer.parseInt(preConverted.substring(42, preConverted.lastIndexOf(')'))); // 276147000
            final com.google.firebase.Timestamp postConverted = new com.google.firebase.Timestamp(_seconds, _nanoseconds);

            date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date(postConverted.getSeconds() * 1000));

        }
        return date;
    }

    public void setComplete_time(Timestamp complete_time) {
        this.complete_time = complete_time;
    }

    public Integer getComplete() {
        return complete;
    }

    public void setComplete(Integer complete) {
        this.complete = complete;
    }

    public Double getTotal_miles() {
        return total_miles;
    }

    public void setTotal_miles(Double total_miles) {
        this.total_miles = total_miles;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "location='" + location + '\'' +
                ", user_id='" + user_id + '\'' +
                ", start_lat='" + start_lat + '\'' +
                ", start_long='" + start_long + '\'' +
                ", end_lat='" + end_lat + '\'' +
                ", end_long='" + end_long + '\'' +
                ", start_time=" + start_time +
                ", complete_time=" + complete_time +
                ", complete=" + complete +
                ", total_miles=" + total_miles +
                '}';
    }
}
