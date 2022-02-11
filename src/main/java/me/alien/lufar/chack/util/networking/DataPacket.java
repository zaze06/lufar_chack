package me.alien.lufar.chack.util.networking;

import me.alien.lufar.chack.util.Type;
import org.json.JSONObject;

public class DataPacket {
    Type type;
    JSONObject data;
    String error;

    public DataPacket(Type type, JSONObject data, String error) {
        this.type = type;
        this.data = data;
        this.error = error;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public JSONObject getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "type=" + type +
                ", data=" + data +
                ", error='" + error + '\'' +
                '}';
    }

    public JSONObject toJSON(){
        JSONObject dataPacket = new JSONObject();
        dataPacket.put("type", type);
        dataPacket.put("data", data);
        dataPacket.put("error", error);
        return dataPacket;
    }
}
