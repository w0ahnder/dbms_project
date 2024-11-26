package common;

import tree.BTree;

import java.util.HashMap;

public class TableStats {
    public int numTuples;
    public HashMap<String, Integer[]> col_infos = new HashMap<>();
    public String table;
    public HashMap<String, BTree> colTree = new HashMap<>();
    public int numLeaves;
    public TableStats(String table, int numTuples){
        this.numTuples = numTuples;
        this.table = table;
    }

    public void addColumnInfo(String col, int min, int max){
        Integer[] info = new Integer[2];
        info[0] = min;
        info[1] = max;
        col_infos.put(col, info);
    }

    public Integer[] getColumnInfo(String col){
        return col_infos.get(col);
    }

    public int getNumTuples(){
        return numTuples;
    }
    public int numCols(){
        return col_infos.size();
    }

    public void setColLeaves(String col, BTree tree){
        colTree.put(col, tree);
    }

    public int getNumLeaves(String col){
        return colTree.get(col).getNumLeaves();
    }
}
