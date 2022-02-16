package me.alien.lufar.chack.ws;

import me.alien.lufar.chack.util.LineType;
import me.alien.lufar.chack.util.Pair;
import me.alien.lufar.chack.util.Tile;
import me.alien.lufar.chack.util.Type;
import me.alien.lufar.chack.util.math.Vector2I;
import me.alien.lufar.chack.util.networking.DataPacket;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server extends WebSocketServer {

    private int turn = 1;

    public Server(int port){
        super(new InetSocketAddress(port));
    }

    static Tile[][] map = new Tile[60][60];

    ArrayList<WebSocket> fallback = new ArrayList<>();
    ArrayList<WebSocket> waitingHosts = new ArrayList<>();

    Map<Integer, Lobby> lobbys = new HashMap<>();

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new client put in fallback");
        fallback.add(conn);
        conn.send(new DataPacket(Type.JOIN, new JSONObject().put("info", "welcome"), "").toJSON().toString());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        fallback.remove(conn);
        waitingHosts.remove(conn);
        for(Map.Entry<Integer, Lobby> entryLobby : lobbys.entrySet()){
            Lobby lobby = entryLobby.getValue();
            if(lobby.remove(conn)){
                lobbys.remove(entryLobby.getKey());
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        JSONObject rawData = new JSONObject(message);
        JSONObject data = rawData.getJSONObject("data");
        if(fallback.contains(conn)){
            Lobby lobby = lobbys.get(data.getInt("lobby"));
            if(lobby == null){
                conn.send(new DataPacket(Type.CREATE_LOBBY, new JSONObject().put("lobby", data.getInt("lobby")).put("message", "lobby dos not exist, whana create it?"), "").toJSON().toString());
                fallback.remove(conn);
                waitingHosts.add(conn);
            }else {
                if (!lobby.addPlayer(conn)) {
                    conn.close(1, "lobby full");
                } else {
                    fallback.remove(conn);
                }
            }
        }else if(waitingHosts.contains(conn)){
            Lobby lobby = new Lobby(data.getInt("md"), data.getBoolean("singleClient"));
            lobby.addPlayer(conn);
            lobbys.put(data.getInt("lobby"), lobby);
            waitingHosts.remove(conn);
        }else{
            for(Map.Entry<Integer, Lobby> entryLobby : lobbys.entrySet()){
                Lobby lobby = entryLobby.getValue();
                if(lobby.contains(conn)){
                    lobby.onMessage(conn, message);
                }
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }

    public static void main(){
        Server server = new Server(8080);
        server.start();
        System.out.println("Server started on port: "+server.getPort());

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            try {
                String data = in.readLine();
                if(data.equalsIgnoreCase("exit")){
                    server.stop();
                    System.exit(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
