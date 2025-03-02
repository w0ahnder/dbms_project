package tree;

import common.Tuple;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BTree {

  boolean clustered;
  int attribute;
  int d;
  List<List<Node>> layers;

  public BTree(boolean clust, int col, int order) {
    clustered = clust;
    attribute = col;
    d = order;
    layers = new ArrayList<>();
  }

  /**
   * Create a leaf layer
   *
   * @param data has the keys and pageid,rowid
   * @return a layer of Leaf nodes
   */
  public ArrayList<Node> leafLayer(HashMap<Integer, ArrayList<Tuple>> data) {

    ArrayList<Node> leaves = new ArrayList<>();
    int tot = data.size(); // total number of keys
    int processed = 0; // number of keys we have processed

    // each leaf node has d<= k <=2d
    // to fill tree completely, each node gets 2d entries
    // if 2d<k<3d have to handle differently

    Integer[] key_arr = data.keySet().toArray(new Integer[tot]);
    int start = 0;
    // keep track of addresses for serializing later
    int addr = 1; // pages for leaves start at 1
    int rem = tot;
    while (!(2 * d < rem && rem < 3 * d) && processed < tot) {
      // means k>=2d  (so we can use 2d) , k<2d ( we have to use k), k>=3d (can use 2d)
      int collect =
          (tot - processed) < 2 * d
              ? (tot - processed)
              : 2 * d; // how many keys to put in this node
      int end = start + collect;
      leaves.add(makeLeaf(data, start, end, key_arr, addr));
      start = end;
      processed = processed + collect;
      rem = tot - processed;
      addr++;
    }
    rem = tot - processed;
    // now handle case when not enough to fill the last 2 leaves properly
    if (2 * d < rem && rem < 3 * d) {
      int left = rem / 2; // number of entries for second to last leaf node
      int right = rem - left;
      int end = start + left;
      leaves.add(makeLeaf(data, start, end, key_arr, addr));
      addr++;
      start = end;
      end = tot;
      leaves.add(makeLeaf(data, start, end, key_arr, addr));
    }

    return leaves;
  }

  /**
   * create a layer of index nodes
   *
   * @param nodes list of nodes we include in this layer
   * @return A new layer of nodes
   */
  public ArrayList<Node> indexLayer(ArrayList<Node> nodes) {
    int num = nodes.size();
    int processed = 0;
    int start = 0;
    ArrayList<Node> indexes = new ArrayList<>();
    int addr = num + nodes.get(0).getAddress();
    int rem = num;
    while (!(2 * d + 1 < rem && rem < 3 * d + 2) && processed < num) {
      int collect =
          rem < 2 * d + 1 ? rem : 2 * d + 1; // how many nodes we include in this index node
      int end = start + collect;
      indexes.add(makeIndex(nodes, start, end, addr));
      addr++;
      start = end;
      processed += collect;
      rem = num - processed;
    }
    // now case when last two indexes cant be filled properly
    if (2 * d + 1 < rem && rem < 3 * d + 2) {
      int left = rem / 2;
      int right = rem - left;
      int end = start + left;
      indexes.add(makeIndex(nodes, start, end, addr));

      addr++;
      start = end;
      end = num;
      indexes.add(makeIndex(nodes, start, end, addr));
    }
    return indexes;
  }

  /**
   * Creates an index node
   *
   * @param nodes list of nodes to put in subtree of this index node
   * @param start where we start collecting nodes
   * @param end where we stop getting nodes for subtree
   * @param address address of index node
   * @return an Index node
   */
  public Index makeIndex(ArrayList<Node> nodes, int start, int end, int address) {
    // add pointers to all the nodes in this range
    ArrayList<Node> children = new ArrayList<>();
    ArrayList<Integer> keys = new ArrayList<>();
    for (int i = start; i < end; i++) {
      children.add(nodes.get(i));
    }
    // now have to get the keys we want to use;
    // look into the second, third, ...  pointers to get the smallest element in the leaf of that
    // subtree
    for (int i = 1; i < children.size(); i++) {
      int small = children.get(i).smallest();
      keys.add(small);
    }
    return new Index(children, address, keys);
  }

  /**
   * makes a leaf node
   *
   * @param data is the pageID, rowID tuples for all the keys
   * @param start is from where we should start collecting the keys
   * @param end is where we stop collecting keys to add to a leaf node
   * @param key_arr list of keys from which we can select to include in a leaf node
   * @param address address of where the leaf node would be
   * @return a Leaf node with keys and (pageid, rowid) lists
   */
  public Leaf makeLeaf(
      HashMap<Integer, ArrayList<Tuple>> data, int start, int end, Integer[] key_arr, int address) {
    // each leaf node has to have the < key, list> structure
    ArrayList<Leaf> leaves = new ArrayList<>();
    ArrayList<Integer> key_list = new ArrayList<>();
    ArrayList<ArrayList<Tuple>> rid_lists = new ArrayList<>();
    // keep keys separate from rids so easier to search for a specific search key
    // key i corresponds to list i in
    for (int i = start; i < end; i++) {
      int key = key_arr[i];
      ArrayList<Tuple> list = data.get(key);

      key_list.add(key);
      rid_lists.add(list);
    }

    return new Leaf(key_list, rid_lists, address);
  }

  /**
   * adds layers to our tree so we can keep track of when we eventually just reach one root node
   *
   * @param l is a layer of nodes to add to our tree
   */
  public void addLayer(ArrayList<Node> l) {
    layers.add(l);
  }

  public int latestSize() {
    return layers.getLast().size();
  }

  /********************* Serialize tree to a file *********************/

  /**
   * Serialize this tree and store in the File at path
   *
   * @param path is path to store serialized tree
   */
  public void tree_to_file(String path) {
    try {
      ByteBuffer bb = ByteBuffer.allocate(4096);
      FileOutputStream out = new FileOutputStream(new File(path));
      FileChannel fc = out.getChannel();
      // header page has root address, number of leaves, order
      Node root = this.layers.getLast().get(0);
      int rootAddr = root.getAddress();
      int num_leaves = this.layers.getFirst().size();
      bb.putInt(0, rootAddr);
      bb.putInt(4, num_leaves);
      bb.putInt(8, d);
      fc.write(bb);
      // new page now
      // now serialize all the leaf nodes, then all the index nodes
      for (List<Node> l : this.layers) {
        for (Node n : l) {
          // each node gets own page
          init(bb);
          n.serial(bb, fc);
          fc.write(bb);
        }
      }
      fc.close();
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * debugging function to print an index node
   *
   * @param ind is the list of nodes an index node points to
   * @param ps is where to write the string representation of this index node
   * @throws FileNotFoundException
   */
  public void printIndex(ArrayList<Node> ind, PrintStream ps) throws FileNotFoundException {
    for (Node index : ind) {
      if (ind.size() == 1) {
        ps.println("Root Node at " + index.getAddress());
      } else {
        ps.println("Index Node ");
      }
      String s = index.toString();
      ps.println(s);
    }
  }

  /**
   * debugging function to print an index node
   *
   * @param leaves is the list of leaf nodes
   * @param ps is where to write the string representation of these leaf nodes
   * @throws FileNotFoundException
   */
  public void printLeaves(List<Node> leaves, PrintStream ps) throws FileNotFoundException {
    for (Node leaf : leaves) {
      ps.println("Leaf Node");
      String s = leaf.toString();
      ps.println(s);
    }
  }

  /**
   * use to initialize a byte buffer
   *
   * @param b ByteBuffer
   */
  public void init(ByteBuffer b) {
    b.clear();
    b.put(new byte[4096]);
    b.clear();
  }
}
