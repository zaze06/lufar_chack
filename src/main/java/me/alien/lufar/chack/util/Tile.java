package me.alien.lufar.chack.util;

import me.alien.lufar.chack.util.math.Vector2I;
import org.json.JSONObject;

import java.awt.*;

public class Tile {
    boolean placed = false;
    int id = 0;

    public Tile(){}

    public Tile place(int id){
        placed = true;
        this.id = id;
        return this;
    }

    public int getId() {
        return id;
    }

    public boolean isPlaced() {
        return placed;
    }

    public void draw(Graphics2D g2d, int x, int y){
        if(placed) {
            if (id == 1) {
                g2d.setColor(Color.BLUE);
                g2d.drawRect(x + 2, y + 2, 6, 6);
            } else if (id == 2) {
                g2d.setColor(Color.red);
                g2d.drawArc(x + 2, y + 2, 6, 6, 0, 360);
            }
        }
    }

    public Tile clone() {
        Tile tile = new Tile();
        if(placed){
            tile.place(id);
        }
        return tile;
    }

    public JSONObject toJSON(int x, int y){
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("placed", placed);
        obj.put("x", x);
        obj.put("y", y);
        return obj;
    }

    public static Pair<Vector2I, Tile> fromTile(JSONObject data){
        Tile tile = new Tile();
        if(data.getBoolean("placed")){
            tile.place(data.getInt("id"));
        }
        return new Pair<>(new Vector2I(data.getInt("x"), data.getInt("y")), tile);
    }
}
