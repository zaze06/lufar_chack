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

    public static final Version VERSION = new Version("1.2-STABLE");
    public static final ArrayList<Class<?>> listeners = new ArrayList<>();

    public static void main(String[] args1) {
        ArrayList<String> args = new ArrayList<>(Arrays.asList(args1));
        System.out.println(VERSION.toData());
        if(args.contains("-server")){
            Server.main(args1);
        }else if(args.contains("-client")){
            Client.main(args);
        }
    }
}
