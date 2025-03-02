package common;

import java.util.HashMap;
import tree.BTree;

public class TableStats {
  public int numTuples;
  public HashMap<String, Integer[]> col_infos = new HashMap<>();
  public String table;
  public HashMap<String, BTree> colTree = new HashMap<>();
  public int numLeaves;

  public TableStats(String table, int numTuples) {
    this.numTuples = numTuples;
    this.table = table;
  }

  /**
   * Sets the range of values that column col takes in the base table
   *
   * @param col col we want to set the range for
   * @param min lowest value column can take
   * @param max highest value column can take
   */
  public void addColumnInfo(String col, int min, int max) {
    Integer[] info = new Integer[2];
    info[0] = min;
    info[1] = max;
    col_infos.put(col, info);
  }

  /**
   * Returns an Integer array with the min and max values of col in the base table
   *
   * @param col the column we want to get the range for
   * @return
   */
  public Integer[] getColumnInfo(String col) {
    return col_infos.get(col);
  }

  /**
   * @return The number of tuples in this table
   */
  public int getNumTuples() {
    return numTuples;
  }

  /**
   * @return the number of columns in this table
   */
  public int numCols() {
    return col_infos.size();
  }

  /**
   * Sets tree that is built on column col
   *
   * @param col the col that the index was built on
   * @param tree the index for the column
   */
  public void setColLeaves(String col, BTree tree) {
    colTree.put(col, tree);
  }

  /**
   * Gets the number of leaves in the index built on this column
   *
   * @param col the column the index was built on
   * @return the number of leaves in the tree
   */
  public int getNumLeaves(String col) {
    return colTree.get(col).getNumLeaves();
  }

  /**
   * Get the height of the tree
   *
   * @param col the column the index was built on
   * @return the height of the index tree built on this col
   */
  public int getHeightforCol(String col) {
    return colTree.get(col).getHeight();
  }

  /**
   * @return the col_infos HashMap that holds the name of the column and an Integer array holding
   *     its range
   */
  public HashMap<String, Integer[]> getColumnInfos() {
    return this.col_infos;
  }
}
