package me.alien.lufar.chack.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import me.alien.lufar.chack.Main;
import me.alien.lufar.chack.util.*;
import me.alien.lufar.chack.util.math.Vector2I;
import me.alien.lufar.chack.util.networking.DataPacket;
import org.json.JSONObject;

import static me.alien.lufar.chack.Main.VERSION;

public class Client extends JPanel implements MouseListener, ActionListener, KeyListener {
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
    static ArrayList<Line<Vector2I, Vector2I, LineType>> lines = new ArrayList<>();

    JFrame frame;

    Timer displayTimer;

    Socket socket;

    PrintWriter out;
    static BufferedReader in;

    InputDataThread inputDataThread;

    static Tile[][] map = new Tile[1000][1000];

    Vector2I offset = new Vector2I(map.length/2-30, map[0].length/2-30);

    public Client(String hostname){
        frame = new JFrame();

        frame.setSize(600,600);

        addMouseListener(this);
        addKeyListener(this);

        frame.addKeyListener(this);
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

        for(int x = 0; x < map.length; x++){
            for(int y = 0; y < map[x].length; y++){
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
        Graphics2D g2d = (Graphics2D) g;
        doDraw(g2d);
    }

    public void doDraw(Graphics2D g2d){

        for(int x = 0; x < 60; x++){
            int xPos = x*10;
            for(int y = 0; y < 60; y++){
                int yPos = y*10;
                try {
                    final Tile tile = map[x+offset.getX()][y+offset.getY()];
                    if(tile.isFinished()){
                        g2d.setColor(Color.GRAY);
                        g2d.fillRect(xPos, yPos, 10, 10);
                    }
                    tile.draw(g2d, xPos, yPos, 10);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    System.out.println("x: "+x+" y: "+y);
                    System.exit(1);
                }
            }
        }

        for(int x = 0; x < getWidth(); x+=10){
            g2d.drawLine(x,0, x, getHeight());
        }

        for(int y = 0; y < getHeight(); y+=10){
            g2d.drawLine(0,y, getWidth(), y);
        }

        for(Line<Vector2I, Vector2I, LineType> line : lines){
            g2d.setColor(Color.GREEN);
            g2d.drawLine((line.getKey().getX()-offset.getX())*10+5, (line.getKey().getY()-offset.getY())*10+5,
                    (line.getValue().getX()-offset.getX())*10+5, (line.getValue().getY()-offset.getY())*10+5);
            /*if(line.getType() == LineType.HORIZONTAL){
                g2d.drawLine(line.getKey().getX()*10-offset.getX()+5, line.getKey().getY()*10-offset.getY()+5, line.getValue().getX()*10-offset.getX()-5, line.getValue().getY()*10-offset.getY()+5);
            }else if(line.getType() == LineType.VERTICAL){
                g2d.drawLine(line.getKey().getX()*10-offset.getX()+5, line.getKey().getY()*10-offset.getY()+5, line.getValue().getX()*10-offset.getX()+5, line.getValue().getY()*10-offset.getY()-5);
            }else if(line.getType() == LineType.DIAGONAL){
                g2d.drawLine(line.getKey().getX()*10-offset.getX()+5, line.getKey().getY()*10-offset.getY()+5, line.getValue().getX()*10-offset.getX()-5, line.getValue().getY()*10-offset.getY()-5);
            }*/
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        out.println(new DataPacket(Type.CLICK, new JSONObject().put("x", e.getX()/10+offset.getX()).put("y", e.getY()/10+offset.getY()), "").toJSON());
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP){
            offset.addY(-1);
        }else if(e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN){
            offset.addY(1);
        }else if(e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT){
            offset.addX(-1);
        }else if(e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT){
            offset.addX(1);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public class InputDataThread extends Thread{
        @Override
        public void run() {
            while (true){
                try {
                    String dataIn = in.readLine();
                    System.out.println(dataIn);
                    JSONObject object = new JSONObject(dataIn);
                    JSONObject data = object.getJSONObject("data");
                    Type type = object.getEnum(Type.class, "type");
                    Pair<Type, JSONObject> tmp = new Pair<>(type, data);

                    if(type == Type.TILE){
                        Pair<Vector2I, Tile> pair1 = Tile.fromJSON(tmp.getValue());
                        int x = pair1.getKey().getX(), y = pair1.getKey().getY();
                        map[x][y] = pair1.getValue();
                        //dataIn.remove(pair);
                    }else if(type == Type.MAP){
                        JSONObject tiles = tmp.getValue();
                        for(String name : tiles.keySet()){
                            Pair<Vector2I, Tile> pair1 = Tile.fromJSON(tiles.getJSONObject(name).getJSONObject("value"));
                            int x = pair1.getKey().getX(), y = pair1.getKey().getY();
                            map[x][y] = pair1.getValue();
                        }
                    }else if(type == Type.LINE){
                        JSONObject startJSON = tmp.getValue();
                        Vector2I start = new Vector2I(startJSON.getJSONObject("start").getInt("x"), startJSON.getJSONObject("start").getInt("y"));
                        Vector2I end = new Vector2I(startJSON.getJSONObject("end").getInt("x"), startJSON.getJSONObject("end").getInt("y"));
                        LineType lineType = startJSON.getEnum(LineType.class, "type");

                        lines.add(new Line<>(start, end, lineType));
                    }else if(type == Type.NAME){
                        frame.setTitle(tmp.getValue().getString("name"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
