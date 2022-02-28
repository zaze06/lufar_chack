package me.alien.lufar.chack.util;

import me.alien.lufar.chack.util.math.Vector2I;
import org.json.JSONObject;

import java.awt.*;

public class Tile {
    boolean placed = false;
    int id = 0;
    boolean finished = false;
    Vector2I pos;

    public Tile(int x, int y){
        this(new Vector2I(x,y));
    }

    public Tile(Vector2I pos){
        this.pos = pos;
    }

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

    public boolean isFinished(){
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Vector2I getPos() {
        return pos;
    }

    public void setPos(Vector2I pos) {
        this.pos = pos;
    }

    public boolean isEmpty(){
        if(id == 0) return true;
        return false;
    }

    public void draw(Graphics2D g2d, int x, int y, int size){
        if(placed) {
            if (id == 1) {
                g2d.setColor(Color.BLUE);
                //g2d.drawRect(x + (2*(size/10)), y + (2*(size/10)), size-(4*(size/10)), size-(4*(size/10)));
                g2d.drawLine(x+(2*(size/10)), y+(2*(size/10)), x+size-(2*(size/10)), y+size-(2*(size/10)));
                g2d.drawLine(x+size-(2*(size/10)), y+(2*(size/10)), x+(2*(size/10)), y+size-(2*(size/10)));
            } else if (id == 2) {
                g2d.setColor(Color.red);
                g2d.drawArc(x + (2*(size/10)), y + (2*(size/10)), size-(4*(size/10)), size-(4*(size/10)), 0, 360);
            }
        }
    }

    public Tile clone() {
        Tile tile = new Tile(this.pos);
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
        obj.put("finished", finished);
        obj.put("pos", pos.toJSON());
        return obj;
    }

    public static Pair<Vector2I, Tile> fromJSON(JSONObject data){
        final JSONObject pos = data.getJSONObject("pos");
        Tile tile = new Tile(Vector2I.fromJSON(pos));
        if(data.getBoolean("placed")){
            tile.place(data.getInt("id"));
        }
        tile.setFinished(data.getBoolean("finished"));
        return new Pair<>(new Vector2I(data.getInt("x"), data.getInt("y")), tile);
    }

    @Override
    public String toString() {
        return "Tile{" +
                "placed=" + placed +
                ", id=" + id +
                ", finished=" + finished +
                ", pos=" + pos +
                '}';
    }
}
