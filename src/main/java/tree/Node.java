package tree;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public abstract class Node {

  int address;
  public ArrayList<Node> children;

  public Node(ArrayList<Node> children, int address) {
    this.children = children;
    this.address = address;
  }

  /**
   * @return the address of this node
   */
  public int getAddress() {
    return address;
  }

  /**
   * @return The child nodes of this node
   */
  public ArrayList<Node> getChildren() {
    return children;
  }

  /**
   * @return the smallest element in this node, if a leaf, or the smallest element in the leaf level
   *     corresponding to an index node
   */
  public abstract int smallest();

  public abstract String toString();

  public abstract void serial(ByteBuffer bb, FileChannel fc);
}
