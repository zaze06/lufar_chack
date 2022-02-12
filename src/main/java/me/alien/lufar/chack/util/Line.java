package me.alien.lufar.chack.util;

public class Line<K, V, T> {
    K key;
    V value;
    T type;

    public Line(K key, V value, T type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public Line(){

    }

    public V getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }

    public T getType() {
        return type;
    }

    public void setType(T type) {
        this.type = type;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Line{" +
                "key=" + key +
                ", value=" + value +
                ", type=" + type +
                '}';
    }
}
