package tree;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Index extends Node{

    public ArrayList<Node> children;
    public int address;
    public int num_keys;
    public ArrayList<Integer> keys;
    public Index(ArrayList<Node> children, int address, ArrayList<Integer> keys){
        super(children, address);
        this.keys = keys;
        this.children = getChildren();
    }

public int smallest(){
        return children.get(1).smallest();
}
    //each index page has 1 to indicate it is index page
    //# keys in the node
    //actual keys in order
    //address of all children nodes

    public String toString(){
        String s = "";
        s += "keys "  + keys.toString() + " addresses ";

        for(int i=0; i< children.size();i++){
             s+= children.get(i).getAddress() + " ";
        }
        return s;
    }
    //index node keys are chosen from the smallest element in the 2+ subtrees/pointers (not first one)

}
