package me.alien.lufar.chack.ws;

import me.alien.lufar.chack.util.*;
import me.alien.lufar.chack.util.math.Vector2I;
import me.alien.lufar.chack.util.networking.DataPacket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Client extends WebSocketClient implements MouseListener, ActionListener, KeyListener {

    public Client(URI serverURI){
        super(serverURI);

        for(int x = 0; x < map.length; x++){
            for(int y = 0; y < map[x].length; y++){
                map[x][y] = new Tile();
            }
        }

        frame = new JFrame();
        component = new Display(this);

        frame.add(component);

        frame.setSize(600,600);

        component.addKeyListener(this);
        component.addMouseListener(this);

        frame.setVisible(true);

        displayTimer = new Timer(100, this);
        displayTimer.start();
    }

    JFrame frame;
    Display component;

    Timer displayTimer;

    static class Display extends JPanel{
        Client c;
        public Display(Client c){
            this.c = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            doDraw((Graphics2D) g);
        }

        public void doDraw(Graphics2D g2d){
            for(int x = 0; x < 60; x++){
                int xPos = x*10;
                for(int y = 0; y < 60; y++){
                    int yPos = y*10;
                    try {
                        final Tile tile = c.map[x+c.offset.getX()][y+c.offset.getY()];
                        if(tile.isFinished()){
                            g2d.setColor(Color.GRAY);
                            g2d.fillRect(xPos, yPos, 10, 10);
                        }
                        tile.draw(g2d, xPos, yPos);
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        System.out.println("x: "+x+" y: "+y);
                        System.exit(1);
                    }
                }
            }

            g2d.setColor(Color.BLACK);

            for(int x = 0; x < getWidth(); x+=10){
                g2d.drawLine(x,0, x, getHeight());
            }

            for(int y = 0; y < getHeight(); y+=10){
                g2d.drawLine(0,y, getWidth(), y);
            }

            for(Line<Vector2I, Vector2I, LineType> line : c.lines){
                g2d.setColor(Color.GREEN);
                g2d.drawLine((line.getKey().getX()-c.offset.getX())*10+5, (line.getKey().getY()-c.offset.getY())*10+5,
                        (line.getValue().getX()-c.offset.getX())*10+5, (line.getValue().getY()-c.offset.getY())*10+5);
            /*if(line.getType() == LineType.HORIZONTAL){
                g2d.drawLine(line.getKey().getX()*10-offset.getX()+5, line.getKey().getY()*10-offset.getY()+5, line.getValue().getX()*10-offset.getX()-5, line.getValue().getY()*10-offset.getY()+5);
            }else if(line.getType() == LineType.VERTICAL){
                g2d.drawLine(line.getKey().getX()*10-offset.getX()+5, line.getKey().getY()*10-offset.getY()+5, line.getValue().getX()*10-offset.getX()+5, line.getValue().getY()*10-offset.getY()-5);
            }else if(line.getType() == LineType.DIAGONAL){
                g2d.drawLine(line.getKey().getX()*10-offset.getX()+5, line.getKey().getY()*10-offset.getY()+5, line.getValue().getX()*10-offset.getX()-5, line.getValue().getY()*10-offset.getY()-5);
            }*/
            }
        }
    }

    Tile[][] map = new Tile[60][60];
    ArrayList<Line<Vector2I, Vector2I, LineType>> lines = new ArrayList<>();

    Vector2I offset = new Vector2I(map.length/2-30, map[0].length/2-30);

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {
        JSONObject object = new JSONObject(message);
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
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }

    public static void main(ArrayList<String> args){
        String hostname = "localhost";
        int port = 8080;
        String completeHostname = "";
        if(args.contains("-hostname")){
            hostname = args.get(args.indexOf("-hostname")+1);
        }
        if(args.contains("-port")){
            port = Integer.parseInt(args.get(args.indexOf("-port")+1));
        }
        if(args.contains("-ip")){
            completeHostname = args.get(args.indexOf("-ip")+1);
        }else{
            completeHostname = "ws://"+hostname+":"+port;
        }

        try {
            URI ip = new URI(completeHostname);
            Client c = new Client(ip);
            c.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        component.repaint();
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

    @Override
    public void mouseClicked(MouseEvent e) {
        send(new DataPacket(Type.CLICK, new JSONObject().put("x", e.getX()/10+offset.getX()).put("y", e.getY()/10+offset.getY()), "").toJSON().toString());
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
}
