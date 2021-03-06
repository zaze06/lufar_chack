package me.alien.lufar.chack.util.math;

import org.json.JSONObject;

import java.util.Vector;

public class Vector2I {
    int x,y;

    public Vector2I(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void addY(int y){
        this.y += y;
    }

    public void addX(int x){
        this.x += x;
    }

    public double distanceTo(int x1, int y1){
        return (Math.sqrt(Math.pow(x-x1, 2) + Math.pow(y-y1,2)));
    }

    public JSONObject toJSON(){
        return new JSONObject().put("x", x).put("y",y);
    }

    public static Vector2I fromJSON(JSONObject json){
        return new Vector2I(json.getInt("x"), json.getInt("y"));
    }
}
