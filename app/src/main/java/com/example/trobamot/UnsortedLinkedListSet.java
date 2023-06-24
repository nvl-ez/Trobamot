package com.example.trobamot;

public class UnsortedLinkedListSet<V> {
    private class Node<V>{
        private V value;
        private Node next;
        public Node(V value, Node next){
            this.value = value;
            this.next = next;
        }
    }

    private Node first;

    //Constructor
    public UnsortedLinkedListSet() {
        first = null;
    }

    public boolean contains(V elem) {
        Node temp = first;
        while(temp != null){
            if(temp.value.equals(elem)){
                return true;
            }
            temp = temp.next;
        }
        return false;
    }

    public boolean add(V elem) {
        boolean found = contains(elem);

        if(!found){
            Node n = new Node(elem, first);
            first = n;
        }
        return !found;
    }

    public boolean remove(V elem) {
        Node p = first; Node pp = null; boolean trobat = false;
        while (p != null && !trobat) {
            trobat = p.value.equals(elem);
            if (!trobat) {
                pp = p;
                p = p.next;
            }
        }
        if (trobat) {
            if (pp == null) {
                first = p.next;
            } else {
                pp.next = p.next;
            }
        }
        return trobat;
    }

    public boolean isEmpty() {
        return first == null;
    }
}
