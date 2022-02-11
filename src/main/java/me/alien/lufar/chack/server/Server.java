package me.alien.lufar.chack.server;

import me.alien.lufar.chack.util.Pair;
import me.alien.lufar.chack.util.Tile;
import me.alien.lufar.chack.util.Type;
import me.alien.lufar.chack.util.Version;
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

    static Tile[][] map = new Tile[20][20];

    Client player1, player2;

    final ArrayList<Pair<Integer, Pair<Type, JSONObject>>> dataIn = new ArrayList<>();

    ClientAsceptThread clientAsceptThread;

    DataHandler dataHandler;

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
                        id = 1;
                    }else if(player2 == null){
                        player2 = new Client(socket, 1, server);
                        id = 2;
                    }else{
                        out.println(new DataPacket(Type.ERROR, new JSONObject().put("error", "Server full"), "Server full").toJSON());
                        continue;
                    }
                    out.println(new DataPacket(Type.SUCCES, new JSONObject().put("info", "Ready to play"), "").toJSON());
                    //out.println(new DataPacket(Type.TILE, new Tile().place(id).toJSON(0,id), "").toJSON());
                    JSONObject map = new JSONObject();
                    for(int x = 0; x < 20; x++){
                        for(int y = 0; y < 20; y++){
                            map.put(x+":"+y, new Pair<>(new Vector2I(x,y), Server.map[x][y]));
                        }
                    }
                    out.println(new DataPacket(Type.MAP, map, "").toJSON());
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

                if(data.getKey() == Type.CLICK){
                    if(turn == id) {
                        final JSONObject tile = data.getValue();
                        int x = tile.getInt("x");
                        int y = tile.getInt("y");
                        map[x][y].place(id);
                        turn = (turn==1?2:1);
                    }
                }
            }
        }
    }
}
