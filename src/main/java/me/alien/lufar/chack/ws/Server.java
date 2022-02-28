package me.alien.lufar.chack.ws;

import me.alien.lufar.chack.util.Type;
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

    public Server(int port){
        super(new InetSocketAddress(port));
    }

    ArrayList<WebSocket> fallback = new ArrayList<>();
    ArrayList<WebSocket> waitingHosts = new ArrayList<>();
    ArrayList<WebSocket> clients = new ArrayList<>();

    Map<Integer, Lobby> lobby = new HashMap<>();

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new client put in fallback");
        fallback.add(conn);
        conn.send(new DataPacket(Type.JOIN, new JSONObject().put("info", "welcome"), "").toJSON().toString());
        clients.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        fallback.remove(conn);
        waitingHosts.remove(conn);
        clients.remove(conn);
        for(Map.Entry<Integer, Lobby> entryLobby : lobby.entrySet()){
            Lobby lobby = entryLobby.getValue();
            if(lobby.remove(conn)){
                this.lobby.remove(entryLobby.getKey());
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        JSONObject rawData = new JSONObject(message);
        JSONObject data = rawData.getJSONObject("data");
        final int lobbyID = data.getInt("lobby");
        if(fallback.contains(conn)){
            Lobby lobby = this.lobby.get(lobbyID);
            if(lobby == null){
                conn.send(new DataPacket(Type.CREATE_LOBBY, new JSONObject().put("lobby", lobbyID).put("message", "lobby dos not exist, wanna create it?"), "").toJSON().toString());
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
            Lobby lobby = new Lobby(data.getInt("md"), data.getInt("mode"));
            lobby.addPlayer(conn);
            this.lobby.put(lobbyID, lobby);
            waitingHosts.remove(conn);
        }else{
            for(Map.Entry<Integer, Lobby> entryLobby : lobby.entrySet()){
                Lobby lobby = entryLobby.getValue();
                int lobbyId = entryLobby.getKey();
                if(lobbyId == lobbyID){
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
                String[] data = in.readLine().split(" ");
                String command = data[0];
                ArrayList<String> args = new ArrayList<>();

                for(int i = 1; i < data.length; i++){
                    args.add(data[i]);
                }

                if(command.equalsIgnoreCase("exit")){
                    server.stop();
                    System.exit(0);
                }else if(command.equalsIgnoreCase("kick")){

                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
