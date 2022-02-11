package me.alien.lufar.chack.server;

import me.alien.lufar.chack.util.Pair;
import me.alien.lufar.chack.util.Type;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class Client {
    Socket socket;

    int id;

    ReceiveThread receiveThread;

    BufferedReader in;
    PrintWriter out;

    Server server;
    public Client(Socket socket, int id, Server server) throws IOException {
        this.socket = socket;
        this.id = id;
        this.server = server;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    private class ReceiveThread extends Thread{
        @Override
        public void run() {
            while (true){
                try {
                    String data = in.readLine();
                    JSONObject obj = new JSONObject(data);
                    server.dataIn.add(new Pair<>(id, new Pair<>(obj.getEnum(Type.class, "type"), obj.getJSONObject("data"))));
                    synchronized (server.dataIn) {
                        server.dataIn.notifyAll();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
