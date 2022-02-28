package me.alien.lufar.chack.ws;

import me.alien.lufar.chack.util.LineType;
import me.alien.lufar.chack.util.Pair;
import me.alien.lufar.chack.util.Tile;
import me.alien.lufar.chack.util.Type;
import me.alien.lufar.chack.util.math.Vector2I;
import me.alien.lufar.chack.util.networking.DataPacket;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.ArrayList;

public class Lobby {
    public static final int PVP = 1;
    public static final int PVP_SINGLE_CLIENT = 2;
    public static final int PVD = 3;

    WebSocket player1;
    WebSocket player2;

    int player1Wins = 0, player2Wins = 0;

    static Tile[][] map = new Tile[60][60];

    int maxDistance;

    ArrayList<Pair<Vector2I, Tile>> currentPlating = new ArrayList<>();

    int turn = 1;

    int mode;

    public Lobby(int md, int mode) {
        for(int x = 0; x < map.length; x++){
            for(int y = 0; y < map[x].length; y++){
                map[x][y] = new Tile(x,y);
            }
        }

        maxDistance = md;
        this.mode = mode;

        //map[0][0].place(1);
    }

    public void onMessage(WebSocket conn, String message){
        JSONObject tmp = new JSONObject(message);

        int id = 0;
        if(player1.equals(conn)){
            id = 1;
        }else if(player2.equals(conn)){
            id = 2;
        }
        if(mode == PVP_SINGLE_CLIENT) id = turn;

        Pair<Type, JSONObject> data = new Pair<>(tmp.getEnum(Type.class, "type"), tmp.getJSONObject("data"));

        if(data.getKey() == Type.CLICK){
            if(!(mode == PVP_SINGLE_CLIENT || mode == PVD)) if(player1 == null || player2 == null) return;

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
                Vector2I aiPos = null;
                if(mode == 3){
                    aiPos = ai();
                }
                testWin(new Vector2I(x,y), aiPos);
                if(mode == 3){
                    turn = 1;
                }
            }
        }else if(data.getKey() == Type.MESSAGE){

        }
    }

    private Vector2I ai() {
        int id = 2;
        ArrayList<Pair<Integer, ArrayList<Pair<Vector2I, Tile>>>> sets = new ArrayList<>();
        for (Pair<Vector2I, Tile> checkTile : currentPlating) {
            int id1 = 1;
            Vector2I pos = checkTile.getKey();
            if(map[pos.getX()][pos.getY()].getId() != id1){
                continue;
            }
            boolean done = false;
            ArrayList<Pair<Vector2I, Tile>> tiles = new ArrayList<>();
            for (int xDir = -1; xDir <= 1; xDir++) {
                for (int yDir = -1; yDir <= 1; yDir++) {
                    try {
                        if (yDir == 0 && xDir == 0) continue;
                        int y2 = pos.getY() + yDir;
                        int x2 = pos.getX() + xDir;
                        if(x2 < 0 || y2 < 0) continue;
                        Tile t = map[x2][y2];
                        if(t.getId() != id1) continue;
                        tiles.add(new Pair<>(new Vector2I(x2, y2), t));
                        int priority = 1;
                        while (t.getId() == id1 && priority < 5) {
                            if (t.isFinished()) break;
                            x2 += xDir;
                            y2 += yDir;
                            if(x2 < 0 || y2 < 0) continue;
                            t = map[x2][y2];
                            tiles.add(new Pair<>(new Vector2I(x2, y2), t));
                            priority++;
                        }
                        x2 -= xDir;
                        y2 -= yDir;
                        sets.add(new Pair<>(priority, tiles));
                        if(priority > 1) {
                            System.out.println("("+pos.getX()+","+pos.getY()+"): (" + xDir + "," + yDir + ") " + priority);
                        }

                    } catch (IndexOutOfBoundsException ignored) {

                    }
                }
            }
        }
        int highestPriorityIndex = 0;
        int highestPriority = 0;
        int x = 0, y = 0;

        for (int i = 0; i < sets.size(); i++) {
            Pair<Integer, ArrayList<Pair<Vector2I, Tile>>> set = sets.get(i);
            if (set.getKey() > highestPriority) {
                highestPriorityIndex = i;
                highestPriority = set.getKey();
            }
        }

        if (highestPriority >= 3) {
            System.out.println("Find a priority 3");

            Vector2I firstTileDir = new Vector2I(0, 0);
            Vector2I lastTileDir = new Vector2I(0, 0);
            Pair<Integer, ArrayList<Pair<Vector2I, Tile>>> set = sets.get(highestPriorityIndex);
            final ArrayList<Pair<Vector2I, Tile>> tiles = set.getValue();
            if (tiles.get(0).getKey().getX() > tiles.get(1).getKey().getX()) {
                firstTileDir.setX(1);
            } else if (tiles.get(0).getKey().getX() < tiles.get(1).getKey().getX()) {
                firstTileDir.setX(-1);
            }
            if (tiles.get(0).getKey().getY() > tiles.get(1).getKey().getY()) {
                firstTileDir.setY(1);
            } else if (tiles.get(0).getKey().getY() < tiles.get(1).getKey().getY()) {
                firstTileDir.setY(-1);
            }
            boolean firstBlockLocationPlaced = map[tiles.get(0).getKey().getX() + firstTileDir.getX()][tiles.get(0).getKey().getY() + firstTileDir.getY()].isPlaced();

            System.out.println("("+tiles.get(tiles.size()-1).getKey().getX()+","+tiles.get(tiles.size()-1).getKey().getY()+") ("+
                    tiles.get(tiles.size()-2).getKey().getX()+","+tiles.get(tiles.size()-2).getKey().getY()+")");

            if (tiles.get(tiles.size() - 1).getKey().getX() > tiles.get(tiles.size() - 2).getKey().getX()) {
                lastTileDir.setX(1);
            } else if (tiles.get(tiles.size() - 1).getKey().getX() < tiles.get(tiles.size() - 2).getKey().getX()) {
                lastTileDir.setX(-1);
            }
            if (tiles.get(tiles.size() - 1).getKey().getY() > tiles.get(tiles.size() - 2).getKey().getY()) {
                lastTileDir.setY(1);
            } else if (tiles.get(tiles.size() - 1).getKey().getY() < tiles.get(tiles.size() - 2).getKey().getY()) {
                lastTileDir.setY(-1);
            }
            boolean lastBlockLocationPlaced = map[tiles.get(tiles.size() - 1).getKey().getX() + lastTileDir.getX()][tiles.get(tiles.size() - 1).getKey().getY() + lastTileDir.getY()].isPlaced();

            System.out.println(lastBlockLocationPlaced+" "+firstBlockLocationPlaced);

            if (!firstBlockLocationPlaced) {
                map[tiles.get(0).getKey().getX() + firstTileDir.getX()][tiles.get(0).getKey().getY() + firstTileDir.getY()].place(id);
                x = tiles.get(0).getKey().getX() + firstTileDir.getX();
                y = tiles.get(0).getKey().getY() + firstTileDir.getY();
                System.out.println("("+x+","+y+")");
            } else if (!lastBlockLocationPlaced) {
                map[tiles.get(tiles.size() - 1).getKey().getX() + lastTileDir.getX()][tiles.get(tiles.size() - 1).getKey().getY() + lastTileDir.getY()].place(id);
                x = tiles.get(tiles.size() - 1).getKey().getX() + lastTileDir.getX();
                y = tiles.get(tiles.size() - 1).getKey().getY() + lastTileDir.getY();
                System.out.println("("+x+","+y+")");
            }

        } else {
            System.out.println("No priority, highest priority is "+highestPriority);
            sets = new ArrayList<>();
            for (Pair<Vector2I, Tile> checkTile : currentPlating) {
                int id1 = 2;
                Vector2I pos = checkTile.getKey();
                boolean done = false;
                ArrayList<Pair<Vector2I, Tile>> tiles = new ArrayList<>();
                for (int xDir = -1; xDir <= 1; xDir++) {
                    for (int yDir = -1; yDir <= 1; yDir++) {
                        try {
                            if (yDir == 0 && xDir == 0) continue;
                            int y2 = pos.getY() + yDir;
                            int x2 = pos.getX() + xDir;
                            Tile t = map[x2][y2];
                            tiles.add(new Pair<>(new Vector2I(x2, y2), t));
                            int priority = 1;
                            while (t.getId() == id1 && priority < 5) {
                                if (t.isFinished()) break;
                                x2 += xDir;
                                y2 += yDir;
                                t = map[x2][y2];
                                tiles.add(new Pair<>(new Vector2I(x2, y2), t));
                                priority++;
                            }
                            x2 -= xDir;
                            y2 -= yDir;
                            sets.add(new Pair<>(priority, tiles));
                        } catch (IndexOutOfBoundsException ignored) {

                        }
                    }
                }
            }

            highestPriority = 0;

            for (int i = 0; i < sets.size(); i++) {
                Pair<Integer, ArrayList<Pair<Vector2I, Tile>>> set = sets.get(i);
                if (set.getKey() > highestPriority) {
                    highestPriorityIndex = i;
                }
            }

            if (highestPriority >= 2) {
                Vector2I start = new Vector2I(0, 0);
                Vector2I end = new Vector2I(0, 0);
                Pair<Integer, ArrayList<Pair<Vector2I, Tile>>> set = sets.get(highestPriorityIndex);
                final ArrayList<Pair<Vector2I, Tile>> tiles = set.getValue();
                if (tiles.get(0).getKey().getX() > tiles.get(1).getKey().getX()) {
                    start.setX(1);
                } else if (tiles.get(0).getKey().getX() < tiles.get(1).getKey().getX()) {
                    start.setX(-1);
                }
                if (tiles.get(0).getKey().getY() > tiles.get(1).getKey().getY()) {
                    start.setY(1);
                } else if (tiles.get(0).getKey().getY() < tiles.get(1).getKey().getY()) {
                    start.setY(-1);
                }
                boolean startBlocked = map[tiles.get(0).getKey().getX() + start.getX()][tiles.get(0).getKey().getY() + start.getY()].isPlaced();

                if (tiles.get(tiles.size() - 1).getKey().getX() > tiles.get(tiles.size() - 2).getKey().getX()) {
                    end.setX(1);
                } else if (tiles.get(tiles.size() - 1).getKey().getX() < tiles.get(tiles.size() - 2).getKey().getX()) {
                    end.setX(-1);
                }
                if (tiles.get(tiles.size() - 1).getKey().getY() > tiles.get(tiles.size() - 2).getKey().getY()) {
                    end.setY(1);
                } else if (tiles.get(tiles.size() - 1).getKey().getY() < tiles.get(tiles.size() - 2).getKey().getY()) {
                    end.setY(-1);
                }
                boolean endBlocked = map[tiles.get(0).getKey().getX() + end.getX()][tiles.get(0).getKey().getY() + end.getY()].isPlaced();

                if (!endBlocked) {
                    map[tiles.get(0).getKey().getX() + end.getX()][tiles.get(0).getKey().getY() + end.getY()].place(2);
                    x = tiles.get(0).getKey().getX() + end.getX();
                    y = tiles.get(0).getKey().getY() + end.getY();
                } else if (!startBlocked) {
                    map[tiles.get(0).getKey().getX() + start.getX()][tiles.get(0).getKey().getY() + start.getY()].place(2);
                    x = tiles.get(0).getKey().getX() + start.getX();
                    y = tiles.get(0).getKey().getY() + start.getY();
                }
            } else {
                int i = (int) (Math.random() * sets.size());
                Pair<Integer, ArrayList<Pair<Vector2I, Tile>>> set = sets.get(i);
                final ArrayList<Pair<Vector2I, Tile>> tiles = set.getValue();
                Vector2I start = new Vector2I(0, 0);
                Vector2I end = new Vector2I(0, 0);
                if (tiles.get(0).getKey().getX() > tiles.get(1).getKey().getX()) {
                    start.setX(1);
                } else if (tiles.get(0).getKey().getX() < tiles.get(1).getKey().getX()) {
                    start.setX(-1);
                }
                if (tiles.get(0).getKey().getY() > tiles.get(1).getKey().getY()) {
                    start.setY(1);
                } else if (tiles.get(0).getKey().getY() < tiles.get(1).getKey().getY()) {
                    start.setY(-1);
                }
                boolean startBlocked = map[tiles.get(0).getKey().getX() + start.getX()][tiles.get(0).getKey().getY() + start.getY()].isPlaced();

                if (tiles.get(tiles.size() - 1).getKey().getX() > tiles.get(tiles.size() - 2).getKey().getX()) {
                    end.setX(1);
                } else if (tiles.get(tiles.size() - 1).getKey().getX() < tiles.get(tiles.size() - 2).getKey().getX()) {
                    end.setX(-1);
                }
                if (tiles.get(tiles.size() - 1).getKey().getY() > tiles.get(tiles.size() - 2).getKey().getY()) {
                    end.setY(1);
                } else if (tiles.get(tiles.size() - 1).getKey().getY() < tiles.get(tiles.size() - 2).getKey().getY()) {
                    end.setY(-1);
                }
                boolean endBlocked = map[tiles.get(0).getKey().getX() + end.getX()][tiles.get(0).getKey().getY() + end.getY()].isPlaced();

                if (!endBlocked) {
                    map[tiles.get(0).getKey().getX() + end.getX()][tiles.get(0).getKey().getY() + end.getY()].place(2);
                    x = tiles.get(0).getKey().getX() + end.getX();
                    y = tiles.get(0).getKey().getY() + end.getY();
                } else if (!startBlocked) {
                    map[tiles.get(0).getKey().getX() + start.getX()][tiles.get(0).getKey().getY() + start.getY()].place(2);
                    x = tiles.get(0).getKey().getX() + start.getX();
                    y = tiles.get(0).getKey().getY() + start.getY();
                } else {
                    ArrayList<Vector2I> available = new ArrayList<>();
                    for (Pair<Vector2I, Tile> checkTile : currentPlating) {
                        Vector2I pos = checkTile.getKey();
                        for (int xDir = -1; xDir <= 1; xDir++) {
                            for (int yDir = -1; yDir <= 1; yDir++) {
                                int y1 = pos.getY() + yDir;
                                int x1 = pos.getX() + xDir;
                                if (map[x1][y1].isEmpty()) {
                                    available.add(new Vector2I(x1, y1));
                                }
                            }
                        }
                    }
                    int j = (int) (Math.random() * available.size());
                    Vector2I pos = available.get(i);
                    x = pos.getX();
                    y = pos.getY();
                    map[x][y].place(2);
                }
                currentPlating.add(new Pair<>(new Vector2I(x,y), map[x][y]));
            }
        }
        return  new Vector2I(x,y);
    }

    private void testWin(Vector2I... positions) {
        for(Vector2I pos1 : positions) {
            if(pos1 == null) continue;
            Vector2I firstTile = null;
            Vector2I lastTile = null;
            boolean finished = false;
            for (Pair<Vector2I, Tile> checkTile : currentPlating) {
                int id1 = checkTile.getValue().getId();
                Vector2I pos = checkTile.getKey();
                firstTile = pos;
                for (int xDir = -1; xDir <= 1; xDir++) {
                    for (int yDir = -1; yDir <= 1; yDir++) {
                        try {
                            if (yDir == 0 && xDir == 0) continue;
                            int y2 = pos.getY() + yDir;
                            int x2 = pos.getX() + xDir;
                            Tile t = map[x2][y2];
                            int stack = 1;
                            while (t.getId() == id1 && stack < 5) {
                                if (t.isFinished()) break;
                                x2 += xDir;
                                y2 += yDir;
                                t = map[x2][y2];
                                stack++;
                            }
                            if (stack == 5) {
                                x2 -= xDir;
                                y2 -= yDir;
                                finished = true;
                                lastTile = new Vector2I(x2, y2);
                                if (id1 == 1) {
                                    player1Wins++;
                                } else if (id1 == 2) {
                                    player2Wins++;
                                }
                                break;
                            }
                        } catch (IndexOutOfBoundsException ignored) {

                        }
                    }
                    if (finished) {
                        break;
                    }
                }
                if (finished) {
                    break;
                }
            }
            if (finished) {
                JSONObject line = new JSONObject();
                JSONObject finishedMap = new JSONObject();
                for (Pair<Vector2I, Tile> checkTile : currentPlating) {
                    checkTile.getValue().setFinished(true);

                    line.put("start", firstTile.toJSON());
                    line.put("end", lastTile.toJSON());


                    LineType lineType = LineType.DIAGONAL;
                    if (firstTile.getY() == lastTile.getY()) {
                        lineType = LineType.HORIZONTAL;
                    } else if (firstTile.getX() == lastTile.getX()) {
                        lineType = LineType.VERTICAL;
                    }
                    line.put("type", lineType);

                    final Vector2I pos = checkTile.getKey();
                    final int y1 = pos.getY();
                    final int x1 = pos.getX();
                    finishedMap.put(x1 + ":" + y1, new Pair<>(pos.toJSON(), checkTile.getValue().toJSON(x1, y1)).toJSON(x1, y1));
                }
                turn = (turn == 1 ? 2 : 1);
                if (player1 != null) player1.send(new DataPacket(Type.MAP, finishedMap, "").toJSON().toString());
                if (player2 != null) player2.send(new DataPacket(Type.MAP, finishedMap, "").toJSON().toString());

                if (player1 != null) player1.send(new DataPacket(Type.LINE, line, "").toJSON().toString());
                if (player2 != null) player2.send(new DataPacket(Type.LINE, line, "").toJSON().toString());

                if (player1 != null)
                    player1.send(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: " + player1Wins + " wins. Player 2 have: " + player2Wins + " wins"), "").toJSON().toString());
                if (player2 != null)
                    player2.send(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: " + player2Wins + " wins. Player 1 have: " + player1Wins + " wins"), "").toJSON().toString());
                currentPlating = new ArrayList<>();
                return;
            }
            if (player1 != null)
                player1.send(new DataPacket(Type.TILE, map[pos1.getX()][pos1.getY()].toJSON(pos1.getX(), pos1.getY()), "").toJSON().toString());
            if (player2 != null)
                player2.send(new DataPacket(Type.TILE, map[pos1.getX()][pos1.getY()].toJSON(pos1.getX(), pos1.getY()), "").toJSON().toString());
            turn = (turn == 1 ? 2 : 1);
        }
    }

    public boolean addPlayer(WebSocket conn) {
        boolean success = false;
        if(mode == PVD || mode == PVP_SINGLE_CLIENT){
            if(player1 == null){
                player1 = conn;
                success = true;
            }
        }else{
            if(player1 == null){
                player1 = conn;
                success = true;
            }
            if(player2 == null){
                player2 = conn;
                success = true;
            }
        }

        if(success){
            JSONObject dataMap = new JSONObject();
            for(int x = 0; x < map.length; x++){
                for(int y = 0; y < map[x].length; y++){
                    if(!map[x][y].isEmpty()){
                        final JSONObject value = new Pair<>(new Vector2I(x, y).toJSON(), map[x][y].toJSON(x, y)).toJSON(x, y);
                        dataMap.put(x+":"+y, value);
                    }
                }
            }
            conn.send(new DataPacket(Type.MAP, dataMap, "").toJSON().toString());
        }

        return success;
    }

    public boolean contains(WebSocket conn) {
        if(player1 == conn){
            return true;
        }else return player2 == conn;
    }

    public boolean remove(WebSocket conn) {
        if(player1 == conn) player1 = null;
        if(player2 == conn) player2 = null;
        return player1 == null && player2 == null;
    }
}
