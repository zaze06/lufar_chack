package me.alien.lufar.chack;

import me.alien.lufar.chack.client.Client;
import me.alien.lufar.chack.server.Server;
import me.alien.lufar.chack.util.Type;
import me.alien.lufar.chack.util.Version;
import me.alien.lufar.chack.util.networking.DataPacket;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static final Version VERSION = new Version("2.3-STABLE");
    public static final ArrayList<Class<?>> listeners = new ArrayList<>();

    public static void main(String[] args1) {
        ArrayList<String> args = new ArrayList<>(Arrays.asList(args1));
        System.out.println(VERSION.toData());
        if(args.contains("-server")){
            Server.main(args1);
        }else if(args.contains("-client")){
            Client.main(args);
        }else if(args.contains("-wss")){
            me.alien.lufar.chack.ws.Server.main();
        }else if(args.contains("-ws")){
            me.alien.lufar.chack.ws.Client.main(args);
        }else if(args.contains("-h") || args.contains("-help")){
            System.out.println("Command line arguments");
            System.out.println("-server: starts a tcp server of the game\n" +
                    "    [-md || -maxDistance]: an optional argument to specify the max amount of tiles that can be between a placed marker and a new one WIP");
            System.out.println("-client: starts a tcp client for the game\n" +
                    "    [-ip <ip>]: an optional argument to specify the ip of the server defaults to localhost(this computer)");
            System.out.println("-wss: starts a websocket server for the game. NOTE this is needed to join from webpage 4zellen.se/zacharias/game\n" +
                    "    [-md || -maxDistance]: an optional argument to specify the max amount of tiles that can be between a placed marker and a new one WIP");
            System.out.println("-ws: starts a websocket client of the game\n" +
                    "    [-ip <ip>]: an optional argument to specify the ip to the server defaults to ws://localhost:8080. NOTE ws:// or wss:// is mandatory\n" +
                    "    [-port <port>]: an optional argument to specify the port for the server defaults to 8080\n" +
                    "    [-hostname <hostname>]: an optional argument to specify the hostname without the port for the server");
            System.out.println("-h || -help: shows this menu");
            System.out.println("<non of the above>: starts a single player client where both players is using the same client(computer)\n" +
                    "    [-md || -maxDistance]: an optional argument to specify the max amount of tiles that can be between a placed marker and a new one");
        }else{
            new Game(args);
        }
    }
}
