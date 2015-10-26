package db;

import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by slgu1 on 10/9/15.
 */
public class Tweet {
    public double getLontitude() {
        return lontitude;
    }

    public void setLontitude(double lontitude) {
        this.lontitude = lontitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return String.valueOf(lontitude) + "," + String.valueOf(latitude);
    }
    public HashMap <String, String> toMap() {
        HashMap <String, String> res = new HashMap<String, String>();
        res.put("id",id);
        res.put("lon",String.valueOf(lontitude));
        res.put("lat",String.valueOf(latitude));
        res.put("date",String.valueOf(createTime));
        res.put("username",userName);
        res.put("text",text);
        res.put("category", category);
        return res;
    }

    public HashMap <String, String> toPosMap() {
        HashMap <String, String> res = new HashMap<String, String>();
        res.put("lon",String.valueOf(lontitude));
        res.put("lat",String.valueOf(latitude));
        return res;
    }
    private double lontitude;
    private double latitude;
    private String id;
    private String userName;
    private Date createTime;
    private String text;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String category = "NULL";
}