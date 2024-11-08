package tree;

import common.Tuple;

import java.io.*;
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

    /**
     * Serialize this tree and store in the File at path
     * @param path is path to store serialized tree
     */
    public void tree_to_file(String path) {
        try {
            ByteBuffer bb = ByteBuffer.allocate(4096);
            FileOutputStream out = new FileOutputStream(new File(path));
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
            fc.close();
            out.close();
        }
        catch (Exception e){
            System.out.println("tree_to_file failed");
        }
    }

    /************ Deserialize file to tree *************/

    public void file_to_tree(){
        try{
            ByteBuffer bb = ByteBuffer.allocate(4096);
            FileInputStream in  = new FileInputStream("src/test/resources/samples-2/expected_indexes/Boats.E");
            FileChannel fc = in.getChannel();

            fc.read(bb);
            //read the header page root address, num leaves, order
            int root_addr = bb.getInt(0);
            int num_leaves = bb.getInt(4);
            int order = bb.getInt(8);
            //start processing leaf nodes
            int page=1;
            List<List<Node>> lrs = new ArrayList<>();
            List<Node> leaf_layer = new ArrayList<>();
            for(int i=0;i<num_leaves;i++){
                Node leaf = deserializeNode(page);
                leaf_layer.add(leaf);
                page++;
            }
            //now do index nodes
            //go from root downwards bc otherwise we dont know how many index nodes are in each layer
            //keep going down until we hit the leaf node pages
            ArrayList<Node> index_layers = new ArrayList<>();
            for(int i=root_addr; i>num_leaves; i--){//process each page; need to figure out how to distinguish levels
                index_layers.add(0, deserializeNode(i));
            }
            //number of nodes on an index level is
            fc.close();
            in.close();
            PrintStream ps = new PrintStream("src/test/resources/samples-2/bulkload/deserialize_index");
            printIndex(index_layers, ps);
            ps.close();
        }
        catch (Exception e){
            System.out.println("file_to_tree failed");
        }
    }

    public void printIndex(ArrayList<Node> ind, PrintStream ps) throws FileNotFoundException {
        for(Node index:ind){
            if(ind.size()==1){
                ps.println("Root Node at " + index.getAddress());
            }
            else {
                ps.println("Index Node ");
            }
            String s = index.toString();
            ps.println(s);
        }
    }

    public void printLeaves(List<Node> leaves, PrintStream ps) throws FileNotFoundException {
        for(Node leaf: leaves){
            ps.println("Leaf Node");
            String s = leaf.toString();
            ps.println(s);
        }
    }


    public Node deserializeNode(int addr){
        try {
            ByteBuffer bb = ByteBuffer.allocate(4096);
            FileInputStream in = new FileInputStream("src/test/resources/samples-2/expected_indexes/Boats.E");
            FileChannel fc = in.getChannel();
            nextPage(bb, fc, addr);
            int ind = bb.getInt();
            if(ind==1){
                fc.close();
                in.close();
                return deserializeIndex(bb,fc,addr);
            }

            fc.close();
            in.close();
            return deserializeLeaf(bb,fc,addr);
        } catch (Exception e) {
            System.out.println("deserialize node failed");
        }
        System.out.println("deserialize node returned null");
        return null;
    }
    public Node deserializeIndex(ByteBuffer b, FileChannel fc, int page){
        //nextPage(b, fc, page);
        int address = page;
        int num_keys = b.getInt();//number of keys
        ArrayList<Integer> keys = new ArrayList<>();
        for(int i=0;i<num_keys;i++){//get all the keys
            keys.add(b.getInt());
        }
        //now get all the addresses for the children nodes
        ArrayList<Integer> addresses = new ArrayList<>();
        //follow the addresses to construct the children for this index node

        ArrayList<Node> children  = new ArrayList<>();
        for(int i=0;i<num_keys +1;i++){//get all the addresses, now follow all of the addresses to deserialize
            addresses.add(b.getInt());
        }
        for(Integer addr: addresses){
            Node child = deserializeNode(addr);
            children.add(child);
        }

        return new Index(children, page, keys);
    }
    public Node deserializeLeaf(ByteBuffer b, FileChannel fc, int page){
        int numEntries = b.getInt(); //number of data entries in this leaf
        ArrayList<Integer> keys = new ArrayList<>();
        ArrayList< ArrayList<Tuple>> rids = new ArrayList<>();
        for(int i=0;i<numEntries;i++){//loop through all of the keys
            keys.add(b.getInt());

            int numrid = b.getInt();//number of rids for this key
            ArrayList<Tuple> list = new ArrayList<>();
            //get all the data for this specific key
            for(int r=0;r<numrid;r++){
                int pgid = b.getInt();
                int tid = b.getInt();
                ArrayList<Integer> elem = new ArrayList<>();
                elem.add(pgid); elem.add(tid);
                list.add(new Tuple(elem));
            }
            rids.add(list);
        }
        return new Leaf(keys, rids, page);
    }

    public void nextPage(ByteBuffer bb, FileChannel fc, int pg){
        init(bb); //fill with 0's
        //set file channel to start reading on this new page
        try {
            fc.position(4096 * pg);
            fc.read(bb); //get data on this page
            bb.flip(); //limit = curr position, position = 0
        }
        catch (Exception e){
            System.out.println("deserialize next page failed");
        }
    }

    //from Tuple writer
    public void init(ByteBuffer b) {
        b.clear();
        b.put(new byte[4096]);
        b.clear();
    }

}

