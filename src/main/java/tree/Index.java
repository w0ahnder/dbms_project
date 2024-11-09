package tree;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Index extends Node {

  public ArrayList<Node> children;
  public int address;
  public int num_keys;
  public ArrayList<Integer> keys;

  public Index(ArrayList<Node> children, int address, ArrayList<Integer> keys) {
    super(children, address);
    this.keys = keys;
    this.children = getChildren();
    num_keys = keys.size();
  }

  /**
   * gets the smallest element in leaf level following the subtrees of this index node
   * @return the leftmost key in leaf layer for this index node
   */
  public int smallest() {
    return children.get(0).smallest();
  }

  // each index page has 1 to indicate it is index page
  // # keys in the node
  // actual keys in order
  // address of all children nodes

  /**
   * Creates a serial representation of this index node
   * @param bb ByteBuffer to put integers into
   * @param fc will write the contents of the buffer to the location of our index file
   */
  public void serial(ByteBuffer bb, FileChannel fc) {

    // index node needs 1 first
    // number of keys in node
    // the keys, in order
    // then addresses of all children in order
    int index = 0;
    bb.putInt(index, 1);
    index += 4;
    bb.putInt(index, num_keys);
    for (int i = 0; i < num_keys; i++) {
      index += 4;
      bb.putInt(index, keys.get(i));
    }

    for (Node n : children) {
      index += 4;
      bb.putInt(index, n.getAddress());
    }
  }

  /**
   * String representation of this Index that prints its keys and their addresses
   * @return
   */
  public String toString() {
    String s = "";
    s += "keys " + keys.toString() + " addresses ";

    for (int i = 0; i < children.size(); i++) {
      s += children.get(i).getAddress() + " ";
    }
    return s;
  }
  // index node keys are chosen from the smallest element in the 2+ subtrees/pointers (not first
  // one)

}
