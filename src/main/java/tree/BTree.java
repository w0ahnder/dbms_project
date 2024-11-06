package tree;

import common.Tuple;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BTree {

    boolean clustered;
    int attribute;
    int d;

    public BTree(boolean clust, int  col , int order){
        clustered = clust;
        attribute = col;
        d = order;
    }


    //has to create index nodes
    // TODO: CREATE INDEX LAYER

    //has to create leaf layer
    public ArrayList<Leaf> leafLayer(HashMap<Integer, ArrayList<Tuple>> data){

        ArrayList<Leaf> leaves =  new ArrayList<>();
        int tot = data.size();//total number of keys
        int processed = 0;//number of keys we have processed

        //each leaf node has d<= k <=2d
        //to fill tree completely, each node gets 2d entries
        //if 2d<k<3d have to handle differently

        Integer[] key_arr = data.keySet().toArray(new Integer[tot]);
        int start = 0;
        //keep track of addresses for serializing later
        int addr = 1; //pages for leaves start at 1
        while(!( 2*d < tot && tot <3*d) && processed<tot ){
            //means k>=2d  (so we can use 2d) , k<2d ( we have to use k), k>=3d (can use 2d)
            int collect= (tot - processed) <2*d? (tot - processed) : 2*d; //how many keys to put in this node
            int end = start + collect;
            leaves.add( makeLeaf(data, start, end, key_arr, addr));
            start = end;
            processed = processed+collect;
            addr++;
        }
        int rem  = tot-processed;
        //now handle case when not enough to fill the last 2 leaves properly
        if(2*d < rem && rem< 3*d){
            int left = rem/2; //number of entries for second to last leaf node
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


    public Leaf makeLeaf(HashMap<Integer, ArrayList<Tuple>> data, int start, int end, Integer[] key_arr, int address){
        //each leaf node has to have the < key, list> structure
        ArrayList<Leaf> leaves = new ArrayList<>();
        ArrayList<Integer> key_list = new ArrayList<>();
        ArrayList< ArrayList<Tuple>> rid_lists = new ArrayList<>();
        //keep keys separate from rids so easier to search for a specific search key
        //key i corresponds to list i in
        for(int i=start; i<end;i++){
            int key = key_arr[i];
            ArrayList<Tuple> list = data.get(key);

            key_list.add(key);
            rid_lists.add(list);
        }

        return new Leaf(key_list, rid_lists, address);
    }
}
