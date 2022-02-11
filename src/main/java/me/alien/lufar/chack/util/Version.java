package me.alien.lufar.chack.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import me.alien.lufar.chack.util.networking.DataPacket;
import org.json.JSONObject;

public class Version {
    private final int length;
    ArrayList<Pair<Integer, Integer>> versionInt;
    ArrayList<Pair<Integer, String>> versionString;
    public static Map<String, Integer> StringValue = new HashMap<>();

    public Version(String version) {
        StringValue.put("DEV", 3);
        StringValue.put("ALFA", 2);
        StringValue.put("BETA", 1);
        StringValue.put("STABLE", 0);


        versionInt = new ArrayList<>();
        versionString = new ArrayList<>();
        char[] versionIn = version.toCharArray();
        StringBuilder tmpInt = new StringBuilder();
        StringBuilder tmpStr = new StringBuilder();
        int pos = 0;
        for (char c : versionIn) {
            if (testIfInt(c)) {
                tmpInt.append(c);
            } else if (testIfAscii(c)) {
                tmpStr.append(c);
            } else if ((c == '-') || (c == '.')) {
                if (!tmpInt.toString().equalsIgnoreCase("")) {
                    versionInt.add(new Pair<>(pos, Integer.parseInt(tmpInt.toString())));
                    tmpInt = new StringBuilder();
                }
                if (!tmpStr.toString().equalsIgnoreCase("")) {
                    versionString.add(new Pair<>(pos, tmpStr.toString()));
                    tmpStr = new StringBuilder();
                }
                pos++;
            }
        }
        if (!tmpInt.toString().equalsIgnoreCase("")) {
            versionInt.add(new Pair<>(pos, Integer.parseInt(tmpInt.toString())));
        }
        if (!tmpStr.toString().equalsIgnoreCase("")) {
            versionString.add(new Pair<>(pos, tmpStr.toString()));
        }
        this.length = pos+1;
    }

    private boolean testIfAscii(char c) {
        return ((c >= 64 && c <= 122));
    }

    private boolean testIfInt(char c){
        return (c == '0' || c == '1' || c == '2' ||
                c == '3' || c == '4' || c == '5' ||
                c == '6' || c == '7' || c == '8' ||
                c == '9');
    }

    public ArrayList<Pair<Integer, Integer>> getVersionInt() {
        return versionInt;
    }

    public ArrayList<Pair<Integer, String>> getVersionString() {
        return versionString;
    }

    @Override
    public String toString(){
        StringBuilder out = new StringBuilder();
        boolean wasInt = false;
        for(int i = 0; i < length; i++){
            String tmp;
            Object tmp1 = "";
            for(Pair<Integer, Integer> i1 : this.versionInt){
                if(i == i1.getKey()){
                    tmp1 = i1.getValue();
                }
            }
            for(Pair<Integer, String> i1 : this.versionString){
                if(i == i1.getKey()){
                    tmp1 = i1.getValue();
                }
            }
            if(tmp1 instanceof Integer) {
                tmp = (wasInt?".":"")+ tmp1;
                wasInt = true;
            }else{
                tmp = ("-")+ tmp1;
                wasInt = false;
            }
            out.append(tmp);
        }
        return out.toString();
    }

    public boolean equals(Version version){
        ArrayList<Pair<Integer, String>> versionString = version.getVersionString();
        ArrayList<Pair<Integer, Integer>> versionInt = version.getVersionInt();

        for(Pair<Integer, String> str : versionString){
            String str1 = null;
            for(Pair<Integer, String> str2 : this.versionString){
                if(str2.getKey().equals(str.getKey())){
                    str1 = str2.getValue();
                }
            }
            if(str1 == null){
                return false;
            }
            if(!Objects.equals(StringValue.get(str.getValue()), StringValue.get(str1))){
                return false;
            }
        }

        for(Pair<Integer, Integer> i : versionInt){
            int i1 = 0;
            boolean isNull = true;
            for(Pair<Integer, Integer> i2 : this.versionInt){
                if(Objects.equals(i2.getKey(), i.getKey())){
                    i1 = i2.getValue();
                    isNull = false;
                }
            }
            if(isNull){
                return false;
            }
            if(!(i.getValue() == i1)){
                return false;
            }
        }
        return true;
    }

    public String toData(){
        return new DataPacket(Type.INFO, new JSONObject().put("version", toString()),"").toJSON().toString();
    }
}
