package com.richez.ladylyn.buscommuterapp.Model;

/**
 * Created by Ricojhon on 24/10/2018.
 */

public class Reminders {
    private String id,name,placeaddress,lat,lng,radius;
    public Reminders(){

    }

    public Reminders(String id,String name, String placeaddress,String lat,String lng,String radius){
        this.id=id;
        this.name=name;

        this.placeaddress=placeaddress;
        this.lat=lat;
        this.lng=lng;
        this.radius=radius;


    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceaddress() {
        return placeaddress;
    }
    public void setPlaceaddress(String placeaddress) {
        this.placeaddress = placeaddress;
    }

    public String getLat() {
        return lat;
    }
    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }
    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getRadius() {
        return radius;
    }
    public void setRadius(String radius) {
        this.radius = radius;
    }

}

