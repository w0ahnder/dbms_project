package tree;

import java.util.ArrayList;

public abstract class Node {

    int address;
    public ArrayList<Node> children;
    public Node(ArrayList<Node> children, int address){
        this.children=children;
        this.address = address;
    }


public int getAddress(){
        return address;
}
public ArrayList<Node> getChildren(){return children;
}
public abstract int smallest();

    public abstract String toString();

}
