package me.alien.lufar.chack.util;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class Pair<K, V> {
    K key;
    V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Pair(){
        key = null;
        value = null;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" +
                "\"pairID\": 0" +
                ", \"key\": " + key.toString() +
                ", \"value\": " + value.toString() +
                '}';
    }


    /**
     *
     * @param x x paremeter for if the key or value has an toJSON meathos that requirirs that
     * @param y y paremeter for if the key or value has an toJSON meathos that requirirs that
     * @return the Pair as a JSONObject
     */
    public JSONObject toJSON(int x, int y){
        Method[] keyMethods = key.getClass().getDeclaredMethods();
        Object keyData = key.toString();
        for(Method m : keyMethods){
            if(m.getName().equals("toJSON")){
                try {
                    keyData = m.invoke(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    try{
                        keyData = m.invoke(null, x,y);
                    } catch (InvocationTargetException ex) {
                        ex.printStackTrace();
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                } catch(NullPointerException e) {
                    //Hantera detta på rätt sätt
                    System.out.println("Breakpoint");
                }
            }
        }

        Method[] valueMethods = value.getClass().getDeclaredMethods();
        Object valueData = value.toString();
        for(Method m : valueMethods){
            if(m.getName().equals("toJSON")){
                try {
                    valueData = m.invoke(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    try{
                        valueData = m.invoke(null, x,y);
                    } catch (InvocationTargetException ex) {
                        ex.printStackTrace();
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        return new JSONObject().put("key", key).put("value", value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}
