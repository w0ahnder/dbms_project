package tree;

import common.Tuple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BTree {

    boolean clustered;
    int attribute;
    int d;
    List< List<Node>> layers;

    public BTree(boolean clust, int  col , int order){
        clustered = clust;
        attribute = col;
        d = order;
        layers = new ArrayList<>();
    }

    //has to create leaf layer
    public ArrayList<Node> leafLayer(HashMap<Integer, ArrayList<Tuple>> data){

        ArrayList<Node> leaves =  new ArrayList<>();
        int tot = data.size();//total number of keys
        int processed = 0;//number of keys we have processed

        //each leaf node has d<= k <=2d
        //to fill tree completely, each node gets 2d entries
        //if 2d<k<3d have to handle differently

        Integer[] key_arr = data.keySet().toArray(new Integer[tot]);
        int start = 0;
        //keep track of addresses for serializing later
        int addr = 1; //pages for leaves start at 1
        int rem = tot;
        while(!( 2*d < rem && rem <3*d) && processed<tot ){
            //means k>=2d  (so we can use 2d) , k<2d ( we have to use k), k>=3d (can use 2d)
            int collect= (tot - processed) <2*d? (tot - processed) : 2*d; //how many keys to put in this node
            int end = start + collect;
            leaves.add( makeLeaf(data, start, end, key_arr, addr));
            start = end;
            processed = processed+collect;
            rem = tot - processed;
            addr++;
        }
        rem  = tot-processed;
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

    public ArrayList<Node> indexLayer(ArrayList<Node> nodes){
        int num = nodes.size();
        int processed = 0;
        int start = 0;
        ArrayList<Node> indexes = new ArrayList<>();
        int addr = num + nodes.get(0).getAddress();
        int rem = num;
        while(!( 2*d+1< rem && rem< 3*d+2) && processed<num){
            int collect= rem <2*d +1? rem : 2*d +1; //how many nodes we include in this index node
            int end = start +collect;
            indexes.add(makeIndex(nodes, start, end, addr));
            addr++;
            start = end;
            processed += collect;
            rem = num - processed;
        }
        //now case when last two indexes cant be filled properly
        if(2*d+1 <rem && rem<3*d+2){
            int left = rem/2;
            int right = rem - left;
            int end = start + left;
            indexes.add(makeIndex(nodes, start, end, addr));

            addr++;
            start = end;
            end = num;
            indexes.add(makeIndex(nodes, start, end,addr));
        }
        return indexes;
    }

    public Index makeIndex(ArrayList<Node> nodes, int start, int end, int address){
        //add pointers to all the nodes in this range
        ArrayList<Node> children = new ArrayList<>();
        ArrayList<Integer> keys = new ArrayList<>();
        for(int i=start; i<end;i++){
            children.add(nodes.get(i));
        }
        //now have to get the keys we want to use;
        //look into the second, third, ...  pointers to get the smallest element in the leaf of that subtree
        for(int i=1;i<children.size();i++){
            int small = children.get(i).smallest();
            keys.add(small);
        }
        return new Index(children, address, keys);

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

    public void addLayer(ArrayList<Node> l){
        layers.add(l);
    }
    public int latestSize(){
        return layers.getLast().size();
    }
    /********************* Serialize tree to a file *********************/

    public void tree_to_file() {
        try {
            ByteBuffer bb = ByteBuffer.allocate(4096);
            FileOutputStream out = new FileOutputStream(new File("file path"));
            FileChannel fc = out.getChannel();

            //header page has root address, number of leaves, order
            Node root = this.layers.getLast().get(0);
            int rootAddr = root.getAddress();
            int num_leaves = this.layers.getFirst().size();
            bb.putInt(0, rootAddr);
            bb.putInt(4, num_leaves);
            bb.putInt(8, d);
            fc.write(bb);
            //new page now
            //now serialize all the leaf nodes, then all the index nodes
            for(List<Node> l: this.layers){
                for(Node n: l){
                    //each node gets own page
                    init(bb);
                    n.serial(bb, fc);
                    fc.write(bb);
                }

            }

        }
        catch (Exception e){
            System.out.println("tree_to_file failed");
        }
    }

    public void init(ByteBuffer b) {
        b.clear();
        b.put(new byte[4096]);
        b.clear();
    }

}

