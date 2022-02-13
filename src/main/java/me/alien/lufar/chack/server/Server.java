package me.alien.lufar.chack.server;

import me.alien.lufar.chack.util.*;
import me.alien.lufar.chack.util.math.Vector2I;
import me.alien.lufar.chack.util.networking.DataPacket;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static me.alien.lufar.chack.Main.VERSION;

public class Server {

    public static void main(String[] args) {
        new Server();
    }

    Server server = this;

    ServerSocket serverSocket;

    static Tile[][] map = new Tile[1000][1000];
    static ArrayList<Pair<Vector2I, Tile>> currentPlating = new ArrayList<>();

    Client player1, player2;

    final ArrayList<Pair<Integer, Pair<Type, JSONObject>>> dataIn = new ArrayList<>();

    ClientAsceptThread clientAsceptThread;

    DataHandler dataHandler;

    int player1Wins = 0;
    int player2Wins = 0;

    int turn = 1;

    public Server(){
        try {
            serverSocket = new ServerSocket(3030);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Server started");

        clientAsceptThread = new ClientAsceptThread();
        clientAsceptThread.start();

        dataHandler = new DataHandler();
        dataHandler.start();

        for(int x = 0; x < map.length; x++){
            for(int y = 0; y < map[x].length; y++){
                map[x][y] = new Tile();
            }
        }

    }

    class ClientAsceptThread extends Thread{
        @Override
        public void run() {
            while (true){
                try {
                    Socket socket = serverSocket.accept();

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    String data = in.readLine();
                    JSONObject obj = new JSONObject(data);
                    if(VERSION.equals(new Version(obj.getJSONObject("data").getString("version")))){
                        out.println(new DataPacket(Type.SUCCES, new JSONObject().put("info", "corect version"), "").toJSON());
                    }else{
                        out.println(new DataPacket(Type.ERROR, new JSONObject().put("error", "Incorrect version server is version: "+VERSION), "Incorrect version server is version: "+VERSION).toJSON());
                        in.close();
                        out.close();
                        continue;
                    }
                    int id;
                    if(player1 == null){
                        player1 = new Client(socket, 1, server);
                        player1.out.println(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: "+player1Wins+" wins. Player 2 have: "+player2Wins+" wins"), "").toJSON());
                        id = 1;
                    }else if(player2 == null){
                        player2 = new Client(socket, 2, server);
                        player2.out.println(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: "+player2Wins+" wins. Player 1 have: "+player1Wins+" wins"), "").toJSON());
                        id = 2;
                    }else{
                        out.println(new DataPacket(Type.ERROR, new JSONObject().put("error", "Server full"), "Server full").toJSON());
                        continue;
                    }
                    out.println(new DataPacket(Type.SUCCES, new JSONObject().put("info", "Ready to play"), "").toJSON());
                    //out.println(new DataPacket(Type.TILE, new Tile().place(id).toJSON(0,id), "").toJSON());

                    /*JSONObject map = new JSONObject();
                    for(int x = 0; x < Server.map.length; x++){
                        for(int y = 0; y < Server.map[x].length; y++){
                            final JSONObject value = new Pair<>(new Vector2I(x, y).toJSON(), Server.map[x][y].toJSON(x, y)).toJSON(x, y);
                            map.put(x+":"+y, value);
                        }
                    }
                    System.out.println(map);
                    final JSONObject mapJson = new DataPacket(Type.MAP, map, "").toJSON();
                    System.out.println(mapJson);
                    out.println(mapJson);*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class DataHandler extends Thread{
        @Override
        public void run() {
            while (true){
                synchronized (dataIn){
                    if(dataIn.size()==0){
                        try {
                            dataIn.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                int id = dataIn.get(0).getKey();
                Pair<Type, JSONObject> data = dataIn.get(0).getValue();
                dataIn.remove(0);

                if(data.getKey() == Type.CLICK){
                    if(player1 == null || player2 == null) continue;
                    if(turn == id) {
                        final JSONObject tile = data.getValue();
                        int x = tile.getInt("x");
                        int y = tile.getInt("y");
                        if(map[x][y].isPlaced()) continue;
                        for(Pair<Vector2I, Tile> checkTile : currentPlating){
                        }
                        map[x][y].place(id);
                        currentPlating.add(new Pair<>(new Vector2I(x,y), map[x][y]));
                        Vector2I firstTile = null;
                        Vector2I lastTile = null;
                        boolean finished = false;
                        for(Pair<Vector2I, Tile> checkTile : currentPlating){
                            int id1 = checkTile.getValue().getId();
                            Vector2I pos = checkTile.getKey();
                            firstTile = pos;
                            for(int x1 = -1; x1 <= 1; x1++){
                                for(int y1 = -1; y1 <= 1; y1++){
                                    try{
                                        if(y1 == 0 && x1 == 0) continue;
                                        int y2 = pos.getY()+y1, x2 = pos.getX()+x1;
                                        Tile t = map[x2][y2];
                                        int stack = 1;
                                        do {
                                            if(t.isFinished()) break;
                                            x2+=x1;
                                            y2+=y1;
                                            t = map[x2][y2];
                                            stack++;
                                        }while (t.getId() == id1 && stack < 5);
                                        if(stack == 5){
                                            x2 -= x1;
                                            y2 -= y1;
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
                            player1.out.println(new DataPacket(Type.MAP, finishedMap, "").toJSON());
                            player2.out.println(new DataPacket(Type.MAP, finishedMap, "").toJSON());

                            player1.out.println(new DataPacket(Type.LINE, line, "").toJSON());
                            player2.out.println(new DataPacket(Type.LINE, line, "").toJSON());

                            player1.out.println(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: "+player1Wins+" wins. Player 2 have: "+player2Wins+" wins"), "").toJSON());
                            player2.out.println(new DataPacket(Type.NAME, new JSONObject().put("name", "You have: "+player2Wins+" wins. Player 1 have: "+player1Wins+" wins"), "").toJSON());
                            currentPlating = new ArrayList<>();
                            continue;
                        }
                        player1.out.println(new DataPacket(Type.TILE, map[x][y].toJSON(x,y), "").toJSON());
                        player2.out.println(new DataPacket(Type.TILE, map[x][y].toJSON(x,y), "").toJSON());
                        turn = (turn==1?2:1);
                    }
                }
            }
        }
    }
}
