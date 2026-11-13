package unoeste.fipp.appaondeestou.model;

import com.google.gson.annotations.SerializedName;

public class LocationIqResult {
    private String lat;
    private String lon;
    @SerializedName("display_name")
    private String displayName;

    public double getLatitude() { return Double.parseDouble(lat); }
    public double getLongitude() { return Double.parseDouble(lon); }
    public String getDisplayName() { return displayName; }
}
