package me.alien.lufar.chack.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import me.alien.lufar.chack.Main;
import me.alien.lufar.chack.util.*;
import me.alien.lufar.chack.util.math.Vector2I;
import me.alien.lufar.chack.util.networking.DataPacket;
import org.json.JSONObject;

import static me.alien.lufar.chack.Main.VERSION;

public class Client extends JPanel implements MouseListener, ActionListener {
    public static void main(ArrayList<String> args) {
        String hostname = "localhost";
        if(args.contains("-ip")){
            int index = args.indexOf("-ip");
            if(args.size() >= index){
                hostname = args.get(index+1);
            }
        }

        new Client(hostname);
    }

    static ArrayList<Pair<Type, JSONObject>> dataIn = new ArrayList<>();

    JFrame frame;

    Timer displayTimer;

    Socket socket;

    PrintWriter out;
    static BufferedReader in;

    InputDataThread inputDataThread;

    static Tile[][] map = new Tile[60][60];

    public Client(String hostname){
        frame = new JFrame();

        frame.setSize(600,600);

        addMouseListener(this);

        try {
            socket = new Socket(hostname, 3030);

            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(VERSION.toData());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            JSONObject data = new JSONObject(in.readLine());

            if(data.getEnum(Type.class,"type") == Type.SUCCES){
                data = new JSONObject(in.readLine());
                if(Type.valueOf((String) data.get("type")) == Type.MAP){

                }
            }else{
                System.out.println("cant connect to server because: "+data.getString("error"));
                System.exit(1);
            }

            inputDataThread = new InputDataThread();
            inputDataThread.start();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        map[0][0] = new Tile();

        for(int x = 0; x < 20; x++){
            for(int y = 0; y < 20; y++){
                map[x][y] = new Tile();
            }
        }

        frame.add(this);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        displayTimer = new Timer(100, this);

        displayTimer.start();

        Main.listeners.add(this.getClass());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDraw((Graphics2D) g);
    }

    public void doDraw(Graphics2D g2d){
        for(int x = 0; x < getWidth(); x+=10){
            g2d.drawLine(x,0, x, getHeight());
        }

        for(int y = 0; y < getHeight(); y+=10){
            g2d.drawLine(0,y, getWidth(), y);
        }

        for(int x = 0; x < map.length; x++){
            int xPos = x*10;
            for(int y = 0; y < map[x].length; y++){
                int yPos = y*10;
                try {
                    map[x][y].draw(g2d, xPos, yPos);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    System.out.println("x: "+x+" y: "+y);
                    System.exit(1);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        out.println(new DataPacket(Type.CLICK, new JSONObject().put("x", e.getX()).put("y", e.getY()), "").toJSON());
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public class InputDataThread extends Thread{
        @Override
        public void run() {
            while (true){
                try {
                    String dataIn = in.readLine();
                    System.out.print(dataIn);
                    JSONObject obj = new JSONObject(dataIn);
                    Pair<Type, JSONObject> tmp = new Pair<>(Type.valueOf(obj.getString("type")), obj.getJSONObject("data"));

                    //dataIn.add(tmp);

                    if(tmp.getKey() == Type.TILE){
                        Pair<Vector2I, Tile> pair1 = Tile.fromJSON(tmp.getValue());
                        int x = pair1.getKey().getX(), y = pair1.getKey().getY();
                        map[x][y] = pair1.getValue();
                        //dataIn.remove(pair);
                    }else if(tmp.getKey() == Type.MAP){
                        JSONObject tiles = tmp.getValue();
                        for(String name : tiles.keySet()){
                            Pair<Vector2I, Tile> pair1 = Tile.fromJSON(tiles.getJSONObject(name).getJSONObject("value"));
                            int x = pair1.getKey().getX(), y = pair1.getKey().getY();
                            map[x][y] = pair1.getValue();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
