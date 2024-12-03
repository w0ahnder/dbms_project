package tree;

import common.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.OrderByElement;
import operator.PhysicalOperators.InMemorySortOperator;
import operator.PhysicalOperators.Operator;
import operator.PhysicalOperators.ScanOperator;

public class BulkLoad {

  public int d;
  public File table;
  public int attribute;
  public boolean clustered; // true if clustered
  public TupleReader reader;
  public BTree tree;
  PrintStream ps;

  public BulkLoad(File table, int order, int col, boolean clustered) throws FileNotFoundException {
    d = order;
    this.table = table;
    attribute = col;
    this.clustered = clustered;
    reader = new TupleReader(table);
    tree = new BTree(clustered, col, order);
  }

  // keep reading tuples and add them to a hashmap keeping track of the key, and list of (pageid,
  // tupleid)
  // want to keep track of which page a tuple is read from, and what is the tupleid

  /**
   * Keeps reading tuples and creates a hashmap containing unique elements in the index col and
   * their locations in the table we are reading from
   *
   * @throws IOException
   */
  public void load() throws IOException {
    // have to have the (pageID, tupleID) sorted first by pageID then tupleID
    HashMap<Integer, ArrayList<Tuple>> data = new HashMap<>();
    Tuple t;
    while ((t = reader.read()) != null) {
      int key = t.getElementAtIndex(attribute);
      int pID = reader.pID();
      int tID = reader.tID();
      ArrayList<Integer> pr = new ArrayList<>();
      pr.add(pID);
      pr.add(tID);
      ArrayList<Tuple> loc = new ArrayList<>();
      if (data.containsKey(key)) {
        loc = data.get(key);
      }
      loc.add(new Tuple(pr));
      data.put(key, loc);
    }

    // have to create leaf layer
    ArrayList<Node> leaves = tree.leafLayer(data);
    tree.setNumLeaves(leaves.size());
    tree.addLayer(leaves);
    // now have to make index layer

    // every index node needs d<= k <= 2d entries

    // case where relation is so small only one leaf node
    // 2 node tree where root node points directly to the leaf node

    if (leaves.size() == 1) {
      int address = leaves.get(0).getAddress() + 1;
      ArrayList<Integer> key = new ArrayList<>();
      key.add(leaves.get(0).smallest());
      Node r = new Index(leaves, address, key);
      ArrayList<Node> root = new ArrayList<Node>();
      tree.addLayer(root);
      return;
    }
    // create index layer right above the leaves; can pass in a Leaf list
    // then create index layer on top of that, can pass in Index List
    ArrayList<Node> indPrint = new ArrayList<>();
    ArrayList<Node> nodes = leaves;
    while (tree.latestSize() > 1) {
      ArrayList<Node> indexes = tree.indexLayer((ArrayList<Node>) nodes);
      // printIndex(indexes);
      tree.addLayer(indexes);
      nodes = indexes;
    }
    // printLeaves(leaves);
  }

  /**
   * Print the keys and addresses of the elements in a list of index nodes
   *
   * @param ind list of Index nodes
   * @throws FileNotFoundException
   */
  public void printIndex(ArrayList<Node> ind) throws FileNotFoundException {
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
   * Print the keys and page information for each leaf node
   *
   * @param leaves list of Leaf nodes
   * @throws FileNotFoundException
   */
  public void printLeaves(ArrayList<Node> leaves) throws FileNotFoundException {
    for (Node leaf : leaves) {
      ps.println("Leaf Node");
      String s = leaf.toString();
      ps.println(s);
    }
  }

  /**
   * returns the BTree created from the bulkload
   *
   * @return
   */
  public BTree getTree() {
    return this.tree;
  }

  /**
   * Sort a relation on a given attribute and write the sorted file
   *
   * @param tablename is the name of the table we want to sort (not alias)
   * @param tablepath is the path to the table
   * @param col is the col we want to sort the table on
   * @param outputPath is where the sorted relation should be written
   */
  public static void sortRelation(
      String tablename, String tablepath, String col, String outputPath) {
    // in memeory sort uses column schema, orderby elements, and use a Scan operator
    try {
      ArrayList<Column> schema = DBCatalog.getInstance().get_Table(tablename);
      OrderByElement ob = new OrderByElement();
      Column newc = new Column();
      Table t = new Table(tablename);
      newc.setTable(t);
      newc.setColumnName(col);
      ob.setExpression(newc);
      List<OrderByElement> ele = new ArrayList<>();
      ele.add(ob);
      Operator scan = new ScanOperator(schema, tablepath);
      InMemorySortOperator sorter = new InMemorySortOperator(schema, ele, scan);
      ArrayList<Tuple> res = sorter.getResult();
      TupleWriter tw = new TupleWriter(outputPath);
      for (Tuple tup : res) {
        tw.write(tup);
      }
      tw.close();
    } catch (Exception e) {
      System.out.println("sorting relation in BulkLoad failed");
    }
  }
}

class PRCompare implements Comparator<Tuple> {

  public int compare(Tuple t1, Tuple t2) {
    int t1_1 = t1.getElementAtIndex(0);
    int t1_2 = t1.getElementAtIndex(1);

    int t2_1 = t2.getElementAtIndex(0);
    int t2_2 = t2.getElementAtIndex(1);
    if (t1_1 < t2_1) {
      return -1;
    }
    if (t1_1 > t2_1) {
      return 1;
    }
    return Integer.compare(t1_2, t2_2);
  }
}
