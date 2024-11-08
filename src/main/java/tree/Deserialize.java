package tree;

import common.Tuple;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class Deserialize {

    String indexFile;

public Deserialize(String indexFile){
    this.indexFile  = indexFile;
    file_to_tree();
}

public static ArrayList<Node> getNodes(int start, int end, List<Node> nodes){
    ArrayList<Node> res = new ArrayList<>();
    for(int i=start; i<end;i++){
        res.add(nodes.get(i));
    }
    return res;
}
public static ArrayList<Node> indexLayer(List<Node> nodes, int d){
    int num = nodes.size();
    int processed = 0;
    int start = 0;
    ArrayList<Node> indexes = new ArrayList<>();
    int addr = num + nodes.get(0).getAddress();
    int rem = num;
    while(!( 2*d+1< rem && rem< 3*d+2) && processed<num){
        int collect= rem <2*d +1? rem : 2*d +1; //how many nodes we include in this index node
        int end = start +collect;
        indexes.addAll(getNodes(start,end,nodes));
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
        indexes.addAll(getNodes(start,end,nodes));
        start = end;
        end = num;
        indexes.addAll(getNodes(start,end,nodes));
    }
    return indexes;
}
public static void createLayers(int order, List<Node> indexes, List<Node> leaves, BTree tree){
    try {
        //lower levels are closer to leaves
        if (leaves.size() == 1) {
            int address = leaves.get(0).getAddress() + 1;
            ArrayList<Integer> key = new ArrayList<>();
            ArrayList<Node> root = new ArrayList<Node>();
            root.add(indexes.getLast());
            tree.addLayer(root);
            return;
        }
        PrintStream ps = new PrintStream("src/test/resources/samples-2/bulkload/deserialize2_index");
        List<Node> nodes = leaves;
        while (tree.latestSize() > 1) {
            ArrayList<Node> layer = indexLayer((ArrayList<Node>) nodes, order);
            printIndex(layer, ps);
            tree.addLayer(layer);
            nodes = layer;
        }
        ps.close();

    }catch (Exception e){

    }

}
    public static void file_to_tree(){
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
            ArrayList<Integer> layer_sizes = new ArrayList<>();
            HashMap<Integer, Set<Integer>> sizes = new HashMap<>();// layer id, number of indexes on this level
            int level = 0;//means root

            for(int i=root_addr; i>num_leaves; i--){//process each page; need to figure out how to distinguish levels
                index_layers.add(0, deserializeNode(i));
                level++;
            }
            //BTree tree = new BTree(true, 1, order);
            //createLayers(order, index_layers, leaf_layer, tree);

            fc.close();
            in.close();
            PrintStream ps = new PrintStream("src/test/resources/samples-2/bulkload/deserialize_index");
            printIndex(index_layers, ps);
            ps.close();
            System.out.println("Hashmap levels " + sizes.toString());
        }
        catch (Exception e){
            System.out.println("file_to_tree failed");
        }
    }

    public static void printIndex(ArrayList<Node> ind, PrintStream ps) throws FileNotFoundException {
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

    public static void printLeaves(List<Node> leaves, PrintStream ps) throws FileNotFoundException {
        for(Node leaf: leaves){
            ps.println("Leaf Node");
            String s = leaf.toString();
            ps.println(s);
        }
    }


    public static Node deserializeNode(int addr){
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
    public static Node deserializeIndex(ByteBuffer b, FileChannel fc, int page){
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
    public static Node deserializeLeaf(ByteBuffer b, FileChannel fc, int page){
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

    public static void nextPage(ByteBuffer bb, FileChannel fc, int pg){
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

    public static void deserializeNodes(ByteBuffer b, FileChannel fc, int pg){
        //when we first enter, want to read the next page for the node
    }

    public void createLayers(){

    }

    //from Tuple writer
    public static void init(ByteBuffer b) {
        b.clear();
        b.put(new byte[4096]);
        b.clear();
    }
}
