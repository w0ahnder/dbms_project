package tree;

import common.Tuple;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

    public void serial(ByteBuffer bb, FileChannel fc){
        //Leaf nodes need 0 first
        //number of data entries in node
        //serialized rep of each data entry, in order

        // <k, [(p1,t1), (p2,t2),..]>
        //write k, then number of rid's, then pi, ti

        bb.putInt(0);
        bb.putInt(keys.size());
        for(int i=0;i<keys.size();i++){
            int key = keys.get(i);
            bb.putInt(key);
            ArrayList<Tuple> id = rids.get(i);
            bb.putInt(id.size()); //number of rids
            for(Tuple t: id){
                bb.putInt(t.getElementAtIndex(0));
                bb.putInt(t.getElementAtIndex(1));
            }
        }

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
