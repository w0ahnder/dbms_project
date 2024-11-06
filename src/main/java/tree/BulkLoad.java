package tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import common.*;

public class BulkLoad {

    public int d;
    public File table;
    public int attribute;
    public boolean clustered; //true if clustered
    public TupleReader reader;
    public BTree tree;
    public BulkLoad(File table, int order, int col, boolean clustered) throws FileNotFoundException {
    d = order;
    this.table = table;
    attribute  = col;
    this.clustered = clustered;
    reader = new TupleReader(table);
    tree = new BTree(clustered, col, order);

    }

    //keep reading tuples and add them to a hashmap keeping track of the key, and list of (pageid, tupleid)
    //want to keep track of which page a tuple is read from, and what is the tupleid
    public void load() throws IOException {
        //have to have the (pageID, tupleID) sorted first by pageID then tupleID
        HashMap<Integer, ArrayList<Tuple>> data = new HashMap<>();
        Tuple t;
        while((t = reader.read())!=null){
            int key = t.getElementAtIndex(attribute);
            int pID  = reader.pID();
            int tID = reader.tID();
            ArrayList<Integer> pr = new ArrayList<>();
            pr.add(pID); pr.add(tID);
            ArrayList<Tuple> loc = new ArrayList<>();
            if(data.containsKey(key)){
                loc = data.get(key);
            }
            loc.add(new Tuple(pr));
            data.put(key,loc);
        }

        //have to create leaf layer
        ArrayList<Leaf> leaves = tree.leafLayer(data);
        printLeaves(leaves);
        //now have to make index layer
    }
    public void printLeaves(ArrayList<Leaf> leaves) throws FileNotFoundException {
        PrintStream ps = new PrintStream(new File("src/test/resources/samples-2/bulkload/Boats.E_bulk"));
        for(Leaf leaf: leaves){
            ps.println("Leaf Node");
            String s = leaf.toString();
            ps.println(s);
        }
        ps.close();
    }

}

class PRCompare implements Comparator<Tuple>{

    public int compare(Tuple t1, Tuple t2){
        int t1_1 = t1.getElementAtIndex(0);
        int t1_2 = t1.getElementAtIndex(1);

        int t2_1 = t2.getElementAtIndex(0);
        int t2_2 = t2.getElementAtIndex(1);
        if(t1_1<t2_1){
            return -1;
        }
        if(t1_1>t2_1){
            return 1;
        }
        return Integer.compare(t1_2, t2_2);
    }
}
