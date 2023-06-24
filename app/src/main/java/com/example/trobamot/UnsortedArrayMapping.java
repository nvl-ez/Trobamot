package com.example.trobamot;

import java.util.Iterator;

public class UnsortedArrayMapping<K, V> {
    private K[] keys;
    private V[] values;
    private int n;

    //Constructor
    public UnsortedArrayMapping(int max){
            keys = (K[]) new Object[max];
            values = (V[]) new Object[max];
            n = 0;
    }

    //Devuelve el valor de una key
    public V get(K key){
        for(int i = 0; i<keys.length; i++){
            if(key.equals(keys[i])) return values[i];
        }
        return null;
    }

    //Substituye el valor de una key existente (evuelve el valor previo)
    //o crea una key con un valor nuevo
    public V put(K key, V value){
        int i = 0;
        for(; i<n; i++){
            if(key.equals(keys[i])) {
                V lastValue = values[i];
                values[i] = value;
                return lastValue;
            }
        }

        if(i<keys.length){
            values[i] = value;
            keys[i] = key;
            n++;
        }
        return null;
    }

    //Elimina el valor asociado a una key y devuelve el valor previo
    public V remove(K key){
        for(int i = 0; i<n; i++){
            if(key.equals(keys[i])) {
                V lastValue = values[i];
                keys[i]=keys[n-1];
                values[i]=values[n-1];
                n--;
                return lastValue;
            }
        }
        return null;
    }

    //Devuelve si hay elementos en el mapping
    public boolean isEmpty(){
        return n==0;
    }

    //Obtenemso el iterator que permite recorrer el mapping
    public Iterator iterator() {
        Iterator it = new IteratorUnsortedArrayMap();
        return it;
    }

    //--------CLASE ITERATOR PARA RECORREL EL MAPPING-----------------------------------------------
    private class IteratorUnsortedArrayMap implements Iterator {
        private int idxIterator;

        //Constructor
        private IteratorUnsortedArrayMap() {
            idxIterator = 0;
        }

        //devuelve true si hay al menos un elemento mas por leer
        @Override
        public boolean hasNext() {
            return idxIterator < n;
        }

        //Devuelve el siguiente valor
        @Override
        public Object next() {
            idxIterator++;
            return new MappingPair(keys[idxIterator-1], values[idxIterator-1]);
        }
    }

    public class MappingPair{
        private V value;
        private K key;

        public MappingPair(K key, V value){
            this.value = value;
            this.key = key;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
