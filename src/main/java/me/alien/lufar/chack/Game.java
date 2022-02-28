package me.alien.lufar.chack;

import me.alien.lufar.chack.util.*;
import me.alien.lufar.chack.util.math.Vector2I;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Game extends JPanel implements KeyListener, MouseListener, ActionListener {

    JFrame frame;

    Game game = this;

    Timer displayTimer;

    Tile[][] map = new Tile[1000][1000];
    ArrayList<Line<Vector2I, Vector2I, LineType>> lines = new ArrayList<>();
    ArrayList<Pair<Vector2I, Tile>> currentPlating = new ArrayList<>();

    Vector2I offset = new Vector2I(map.length/2-30, map[0].length/2-30);

    int player1Wins = 0;
    int player2Wins = 0;

    int turn = 1;

    int maxDistance = 1;

    int size = 10;

    String score = "X have: "+player1Wins+" wins. O have: "+player2Wins+" wins";

    Tile latestTile = null;

    public Game(ArrayList<String> args){

        if(args.contains("-md")){
            maxDistance = Integer.parseInt(args.get(args.indexOf("-md")+1));
        }else if(args.contains("-maxDistance")){
            maxDistance = Integer.parseInt(args.get(args.indexOf("-maxDistance")+1));
        }

        for(int x = 0; x < map.length; x++){
            for(int y = 0; y < map[x].length; y++){
                map[x][y] = new Tile(x,y);
            }
        }

        frame = new JFrame();

        frame.setSize(600,600);

        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);

        frame.add(this);

        displayTimer = new Timer(100, this);
        displayTimer.start();

        frame.setVisible(true);

        frame.setTitle("X have: "+player1Wins+" wins. O have: "+player2Wins+" wins");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                game.requestFocus();
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        doDraw(g2d);
    }

    public void doDraw(Graphics2D g2d){

        frame.setTitle(score+". "+getWidth()/size+":"+getHeight()/size);

        for(int x = 0; x < getWidth()/size; x++){
            int xPos = x*size;
            for(int y = 0; y < getHeight()/size; y++){
                int yPos = y*size;
                try {
                    final Tile tile = map[x+offset.getX()][y+offset.getY()];
                    if(tile.isFinished()){
                        g2d.setColor(Color.GRAY);
                        g2d.fillRect(xPos, yPos, size, size);
                    }
                    if(tile == latestTile){
                        g2d.setColor(new ColorUIResource(Integer.parseInt("47b548", 16)));
                        g2d.fillRect(xPos, yPos, size, size);
                    }
                    tile.draw(g2d, xPos, yPos, size);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    System.out.println("x: "+x+" y: "+y);
                    System.exit(1);
                }
            }
        }

        g2d.setColor(Color.BLACK);

        for(int x = 0; x < getWidth(); x+=size){
            g2d.drawLine(x,0, x, getHeight());
        }

        for(int y = 0; y < getHeight(); y+=size){
            g2d.drawLine(0,y, getWidth(), y);
        }

        g2d.setColor(Color.GREEN);

        for(Line<Vector2I, Vector2I, LineType> line : lines){
            g2d.drawLine((line.getKey().getX()-offset.getX())*size+5, (line.getKey().getY()-offset.getY())*size+5,
                    (line.getValue().getX()-offset.getX())*size+5, (line.getValue().getY()-offset.getY())*size+5);
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
        }else if(e.getKeyCode() == KeyEvent.VK_B){
            if(size+10 < getWidth()){
                size+=10;
            }
        }else if(e.getKeyCode() == KeyEvent.VK_M){
            if(size-10 > 0) {
                size -= 10;
            }
        }else if(e.getKeyCode() == KeyEvent.VK_Q){
            JSONObject map = new JSONObject();
            for(int x = 0; x < this.map.length; x++){
                for(int y = 0; y < this.map[x].length; y++){
                    Tile tile = this.map[x][y];
                    if(false){

                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

        int x = e.getX()/size+offset.getX();
        int y = e.getY()/size+offset.getY();

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
        map[x][y].place(turn);
        latestTile = map[x][y];
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
                    }catch (IndexOutOfBoundsException ex){

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


                LineType lineType = LineType.DIAGONAL;
                if(firstTile.getY() == lastTile.getY()){
                    lineType = LineType.HORIZONTAL;
                }else if(firstTile.getX() == lastTile.getX()){
                    lineType = LineType.VERTICAL;
                }
                lines.add(new Line<>(firstTile, lastTile, lineType));

                final Vector2I pos = checkTile.getKey();
                final int y1 = pos.getY();
                final int x1 = pos.getX();
                finishedMap.put(x1 +":"+ y1, new Pair<>(pos.toJSON(), checkTile.getValue().toJSON(x1, y1)).toJSON(x1, y1));

                score = "X have: "+player1Wins+" wins. O have: "+player2Wins+" wins";
            }
            currentPlating = new ArrayList<>();
        }
        turn = (turn==1?2:1);
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
