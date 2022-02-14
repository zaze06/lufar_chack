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

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class Server extends WebSocketServer {

    private int turn = 1;

    public Server(int port){
        super(new InetSocketAddress(port));

        for(int x = 0; x < map.length; x++){
            for(int y = 0; y < map[x].length; y++){
                map[x][y] = new Tile();
            }
        }
    }

    WebSocket player1;
    WebSocket player2;

    int player1Wins = 0, player2Wins = 0;

    static Tile[][] map = new Tile[60][60];

    int maxDistance = 1;

    ArrayList<Pair<Vector2I, Tile>> currentPlating = new ArrayList<>();

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if(player1 == null){
            player1 = conn;
            player1.send(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: "+player1Wins+" wins. Player 2 have: "+player2Wins+" wins"), "").toJSON().toString());
        }else if(player2 == null){
            player2 = conn;
            player2.send(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: "+player2Wins+" wins. Player 1 have: "+player1Wins+" wins"), "").toJSON().toString());
        }else{
            conn.send(new DataPacket(Type.FAIL, new JSONObject().put("error", "lobby full"), "lobby full").toJSON().toString());
            conn.close();
            return;
        }
        System.out.println("Client joined");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        System.out.println(message);

        JSONObject tmp = new JSONObject(message);

        int id = 0;
        if(player1.equals(conn)){
            id = 1;
        }else if(player2.equals(conn)){
            id = 2;
        }
        Pair<Type, JSONObject> data = new Pair<>(tmp.getEnum(Type.class, "type"), tmp.getJSONObject("data"));

        if(data.getKey() == Type.CLICK){
            if(player1 == null || player2 == null) return;
            if(turn == id) {
                final JSONObject tile = data.getValue();
                int x = tile.getInt("x");
                int y = tile.getInt("y");
                if(map[x][y].isPlaced()) return;

                boolean valid = false;
                for(Pair<Vector2I, Tile> checkTile : currentPlating){
                    Vector2I pos = checkTile.getKey();
                    for(int xDir = -maxDistance; xDir <= maxDistance; xDir++){
                        for(int yDir = -maxDistance; yDir <= maxDistance; yDir++){
                            int x1 = pos.getX()+xDir;
                            int y1 = pos.getY()+yDir;
                            if(x1 == x && y1 == y){
                                valid = true;
                                break;
                            }
                        }
                        if(valid){
                            break;
                        }
                    }
                    if(valid){
                        break;
                    }
                }
                if(currentPlating.isEmpty()){
                    valid = true;
                }
                if(!valid) return;

                map[x][y].place(id);
                currentPlating.add(new Pair<>(new Vector2I(x,y), map[x][y]));
                Vector2I firstTile = null;
                Vector2I lastTile = null;
                boolean finished = false;
                for(Pair<Vector2I, Tile> checkTile : currentPlating){
                    int id1 = checkTile.getValue().getId();
                    Vector2I pos = checkTile.getKey();
                    firstTile = pos;
                    for(int xDir = -1; xDir <= 1; xDir++){
                        for(int yDir = -1; yDir <= 1; yDir++){
                            try{
                                if(yDir == 0 && xDir == 0) continue;
                                int y2 = pos.getY()+yDir;
                                int x2 = pos.getX()+xDir;
                                Tile t = map[x2][y2];
                                int stack = 1;
                                while (t.getId() == id1 && stack < 5) {
                                    if(t.isFinished()) break;
                                    x2+=xDir;
                                    y2+=yDir;
                                    t = map[x2][y2];
                                    stack++;
                                }
                                if(stack == 5){
                                    x2 -= xDir;
                                    y2 -= yDir;
                                    finished = true;
                                    lastTile = new Vector2I(x2, y2);
                                    if(id1 == 1){
                                        player1Wins++;
                                    }else if(id1 == 2){
                                        player2Wins++;
                                    }
                                    break;
                                }
                            }catch (IndexOutOfBoundsException e){

                            }
                        }
                        if(finished){
                            break;
                        }
                    }
                    if(finished){
                        break;
                    }
                }
                if(finished) {
                    JSONObject line = new JSONObject();
                    JSONObject finishedMap = new JSONObject();
                    for (Pair<Vector2I, Tile> checkTile : currentPlating) {
                        checkTile.getValue().setFinished(true);

                        line.put("start", firstTile.toJSON());
                        line.put("end", lastTile.toJSON());


                        LineType lineType = LineType.DIAGONAL;
                        if(firstTile.getY() == lastTile.getY()){
                            lineType = LineType.HORIZONTAL;
                        }else if(firstTile.getX() == lastTile.getX()){
                            lineType = LineType.VERTICAL;
                        }
                        line.put("type", lineType);

                        final Vector2I pos = checkTile.getKey();
                        final int y1 = pos.getY();
                        final int x1 = pos.getX();
                        finishedMap.put(x1 +":"+ y1, new Pair<>(pos.toJSON(), checkTile.getValue().toJSON(x1, y1)).toJSON(x1, y1));
                    }
                    turn = (turn==1?2:1);
                    player1.send(new DataPacket(Type.MAP, finishedMap, "").toJSON().toString());
                    player2.send(new DataPacket(Type.MAP, finishedMap, "").toJSON().toString());

                    player1.send(new DataPacket(Type.LINE, line, "").toJSON().toString());
                    player2.send(new DataPacket(Type.LINE, line, "").toJSON().toString());

                    player1.send(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: "+player1Wins+" wins. Player 2 have: "+player2Wins+" wins"), "").toJSON().toString());
                    player2.send(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: "+player2Wins+" wins. Player 1 have: "+player1Wins+" wins"), "").toJSON().toString());
                    currentPlating = new ArrayList<>();
                    return;
                }
                player1.send(new DataPacket(Type.TILE, map[x][y].toJSON(x,y), "").toJSON().toString());
                player2.send(new DataPacket(Type.TILE, map[x][y].toJSON(x,y), "").toJSON().toString());
                turn = (turn==1?2:1);
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
    }
}
