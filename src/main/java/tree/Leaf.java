package tree;

import common.Tuple;

import java.util.ArrayList;

public class Leaf extends Node{

    public ArrayList<Integer>  keys;
    public ArrayList< ArrayList<Tuple>> rids;
    public int addr;

    public Leaf(ArrayList<Integer> keys, ArrayList< ArrayList<Tuple>> rid, int addr){
        super(new ArrayList<>(), addr);
        this.keys = keys;
        this.rids = rid;
        this.addr = addr;
    }


    public ArrayList<Integer> getKeys(){
        return keys;
    }

    public int smallest(){
        return keys.getFirst();
    }
    public String toString(){
        String s = "";
        for(int i=0; i< keys.size();i++){
            s += "< " + keys.get(i) + " , " + rids.get(i).toString() + " >\n"  ;
        }
        return s;
    }
    public void makeLeaf(){

    }
}
