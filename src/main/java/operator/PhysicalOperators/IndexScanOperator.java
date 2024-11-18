package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import common.TupleReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.schema.Column;

/**
 * Scan operator supports queries that are full table scans know which base table is it scanning
 * Column constructor Column(Table table, String columnName) Table(String schemaName, String name)
 * what is difference between the two names construct a Scan operator for the table in the fromItem
 */
public class IndexScanOperator extends ScanOperator {
  public DBCatalog db;
  public BufferedReader br;
  public String table_path;
  public TupleReader reader;
  TupleReader tr; // read tuples from original table file
  FileInputStream fin;
  FileChannel fc;
  ByteBuffer buff;
  int rootAddr;
  int numLeaves;
  List<Integer> root_keys;
  List<Integer> root_addresses;
  boolean clustered;
  int lowkey;
  int highkey;
  List<Integer> leafKeys; // all the keys in the current leaf node
  List<ArrayList<Tuple>> leafRids; // all the rids for each of the keys in this leaf
  int leafAddr; // page address of the current leaf node
  int currLeafKeyIndex;
  int RIDindex;
  int col;

  // path is path for table
  /**
   * @param outputSchema
   * @param table_file
   * @param ind index of the indexed column
   * @param clustered return 0/1
   * @param low
   * @param high
   * @param index_file the file from the dbcatalog method
   * @throws FileNotFoundException
   */
  public IndexScanOperator(
      ArrayList<Column> schema,
      String tablepath,
      String table,
      File indexFile,
      int col,
      Integer low,
      Integer high,
      boolean clustered)
      throws FileNotFoundException {
    super(schema, tablepath);
    lowkey = low;
    highkey = high;
    try {
      tr = new TupleReader(DBCatalog.getInstance().getFileForTable(table));
      fin = new FileInputStream(indexFile);
      fc = fin.getChannel();
      buff = ByteBuffer.allocate(4096);
      this.clustered = clustered;
      currLeafKeyIndex = 0;
      this.col = col;
      // have to deserialize from root to leaf layer
      processHeader();
      processNode(rootAddr);

      // if high key is null it's okay, we just keep reading until we hit an index page
      // else{////we have to start reading from lowkey instead
      // rootToLeaf(lowkey);//get leaf info
      // now have to find the smallest key inside leaf node that is >=lowkey
      while (leafKeys.get(currLeafKeyIndex) < lowkey && currLeafKeyIndex < leafKeys.size()) {
        int curr = leafKeys.get(currLeafKeyIndex);
        currLeafKeyIndex++;
      }
      RIDindex = 0;
      if (clustered) {
        ArrayList<Tuple> rids = leafRids.get(currLeafKeyIndex);
        Tuple curr = rids.get(RIDindex);
        int page = curr.getElementAtIndex(0);
        int row = curr.getElementAtIndex(1);
        tr.reset(page, row);
      }
    } catch (Exception e) {

      e.printStackTrace();
    }
  }

  /***
   * The node this address corresponds to is processed according to whether or not it is an
   * index or leaf node
   * @param addr is the address of the node we want to process
   * @throws IOException
   */
  void processNode(int addr) throws IOException {
    fc.position(addr * 4096);
    bufferClear();
    fc.read(buff);
    buff.flip();
    int indicator = buff.getInt();
    if (indicator == 0) { // leaf, now we process leaf
      processLeaf(addr);
    } else {
      processIndex(addr);
    }
  }

  /**
   * Processes the index node at page addr. It follows the key that will lead down to a leaf level
   * corresponding to lowkey
   *
   * @param addr is address of this index node
   * @throws IOException
   */
  void processIndex(int addr) throws IOException {
    int num_keys = buff.getInt();
    int num_addresses = num_keys + 1;
    List<Integer> index_keys = new ArrayList<Integer>();
    List<Integer> addresses = new ArrayList<>();

    int found_low = num_keys;

    for (int i = 0; i < num_keys; i++) {
      int currKey = buff.getInt();
      index_keys.add(currKey); //
      // indexx nodes have 2d +1 children and 2d pointers
      // key_0  points to child with key < k_0
      // want to get as close as possible
      if (found_low == num_keys && lowkey < currKey) {
        found_low = i;
      }
    }

    for (int i = 0; i < num_addresses; i++) {
      int currAddr = buff.getInt();
      addresses.add(currAddr);
    }
    int follow_addr = addresses.get(found_low);
    processNode(follow_addr);
  }

  /**
   * Processes a leaf node at addr. It gets the keys, data entries, and sets the address. This is
   * the leaf node where we will be starting to read tuples from.
   *
   * @param addr
   */
  void processLeaf(int addr) {
    // number data entries, then <k <rid>>
    int num_entries = buff.getInt();
    List<Integer> keys = new ArrayList<>();
    List<ArrayList<Tuple>> all_rids = new ArrayList<>();
    for (int i = 0; i < num_entries; i++) {
      keys.add(buff.getInt());
      int num_rids = buff.getInt();
      ArrayList<Tuple> rids = new ArrayList<>(); // process the rids for this specific key

      for (int j = 0; j < num_rids; j++) {
        ArrayList<Integer> elements = new ArrayList<>();
        elements.add(buff.getInt());
        elements.add(buff.getInt()); // pageid, tupleid
        rids.add(new Tuple(elements));
      }
      all_rids.add(rids);
    }
    leafKeys = keys;
    leafRids = all_rids;
    leafAddr = addr;
  }

  /** Fills the buffer full of zeros */
  public void bufferClear() {
    // set limit to capacity, position=0
    buff.clear();
    byte[] zeros = new byte[4096];
    buff.put(zeros);
    buff.clear();
  }

  @Override
  public Tuple getNextTuple() {
    try {
      if (clustered) {
        Tuple t = tr.read();
        if (t == null || t.getElementAtIndex(col) > highkey) return null;
        // TODO: handle closing all channels when we return
        return t;
      } else {
        // when do we return null?
        // return null when the key is >high key
        // return null when we processed all of the leaf nodes and we hit an index page
        //      leaf address can be used to monitor how many leaf pages are left

        // get next leaf node when all of the keys are processed in this one
        // 1. check if all keys in this leaf are processed,
        // 2. check if addr+1 is valid, if valid update current leaf info
        if (currLeafKeyIndex >= leafKeys.size()) {
          fc.position((leafAddr + 1) * 4096);
          bufferClear();
          fc.read(buff);
          buff.flip();
          int indicator = buff.getInt();
          if (indicator == 1) {
            return null;
          }
          processNode(leafAddr + 1);
          currLeafKeyIndex = 0;
          RIDindex = 0;
          return getNextTuple();
        }
        if (leafKeys.get(currLeafKeyIndex) > highkey) {
          return null; // check if new leaf is in
        }
        // bounds

        int currentRidsize =
            leafRids.get(currLeafKeyIndex).size(); // number of <pg,tid> for a given key
        if (RIDindex >= currentRidsize) { // means we looked at all of the rids for this key
          // get next key, and reset the rid to start from pageid_0, tid_0
          RIDindex = 0;
          currLeafKeyIndex++;
          return getNextTuple();
        }

        ArrayList<Tuple> entries = leafRids.get(currLeafKeyIndex);
        Tuple entry = entries.get(RIDindex);
        reader.reset(entry.getElementAtIndex(0), entry.getElementAtIndex(1));
        Tuple result = reader.read();
        RIDindex += 1;
        return result;
      }

    } catch (Exception e) {
      System.out.println("index scan gt next tuple failed");
    }
    return null;
  }

  /**
   * Processes the header page for the index file. It sets the address of the root, the number of
   * leaves, and the order of the tree
   */
  public void processHeader() {

    try {
      fc.read(buff); // fill buffer with values
      buff.flip(); // limit = curr pos, then pos = 0
      rootAddr = buff.getInt();
      numLeaves = buff.getInt();
      int order = buff.getInt();
    } catch (Exception e) {
      System.out.println("processing header failed");
    }
  }

  public void reset() {
    try {
      bufferClear();
      fc.position(0);
      currLeafKeyIndex = 0;
      RIDindex = 0;
      processNode(rootAddr);
      while (leafKeys.get(currLeafKeyIndex) < lowkey && currLeafKeyIndex < leafKeys.size()) {
        currLeafKeyIndex++;
      }

    } catch (Exception e) {

      e.printStackTrace();
    }
  }
}
