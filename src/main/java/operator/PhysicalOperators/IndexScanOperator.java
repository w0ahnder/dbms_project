package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import common.TupleReader;
import net.sf.jsqlparser.schema.Column;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.nio.channels.FileChannel;

public class IndexScanOperator extends ScanOperator{
    TupleReader tr;//read tuples from original table file
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
    List<Integer> leafKeys;//all the keys in the current leaf node
    List< ArrayList<Tuple>> leafRids;//all the rids for each of the keys in this leaf
    int leafAddr;//page address of the current leaf node
    int currLeafKeyIndex;


    public IndexScanOperator(ArrayList<Column> schema, String tablepath, String table,File indexFile, int col, Integer low, Integer high, boolean clustered) throws FileNotFoundException {
        super(schema, tablepath);
        try{
            tr = new TupleReader(DBCatalog.getInstance().getFileForTable(table));

             fin= new FileInputStream(indexFile);
             fc = fin.getChannel();
             buff = ByteBuffer.allocate(4096);
             this.clustered = clustered;
            currLeafKeyIndex = 0;
             //have to deserialize from root to leaf layer
            processHeader();
            //TODO: deal with null low or high key; go from root to leaf to get smallest or biggest elements
            if(low ==  null){
                lowkey = getSmallest(rootAddr);//smallest entry in entire tree
            }
            //if high key is null it's okay, we just keep reading until we hit an index page
            else{////we have to start reading from lowkey instead
                    rootToLeaf(lowkey);//get leaf info
                    //now have to find the smallest key inside leaf node that is >=lowkey
                    while(leafKeys.get(currLeafKeyIndex)<lowkey && currLeafKeyIndex<leafKeys.size()){
                        currLeafKeyIndex++;
                    }
            }


        }
        catch(Exception e){
            System.out.println("Index scan operator constructor failed");
        }
    }

    public void processRootPage() throws IOException {
        fc.position(rootAddr*4096);
        fc.read(buff);
        buff.flip();
        buff.getInt();//1 to indicate it is an index node
        int num_keys = buff.getInt();
        root_keys = new ArrayList<>();
        root_addresses = new ArrayList<>();
        for(int i=0;i<num_keys;i++){
            root_keys.add(buff.getInt());
        }

        int j=0;
        int currAddr;
        while(j<(num_keys+1) && (currAddr= buff.getInt())!=0){
            root_addresses.add(currAddr);
        }
        //now we have all the addresses, children = 2d+1, keys = 2d usually
        //go look through the children index nodes until we get to leaf layer
    }

    public int getSmallest(int addr) throws IOException {
        int leftmost = root_addresses.get(0);
        fc.position(leftmost*4096);
        fc.read(buff);
        //have to keep following left most address until we hit leaf layer
        int type = buff.getInt();
        if(type==0){
            int num_entries = buff.getInt();
            return buff.getInt(); //leftmost key for the leaf node
        }
        else if(type==1){
            int num_keys = buff.getInt();
            ArrayList<Integer> keys = new ArrayList<>();
            for(int i=0;i<num_keys;i++){
                keys.add(buff.getInt());
            }
            int first_address  = buff.getInt();
            return getSmallest(first_address);//returns the the smallest entry in the entire tree
        }
        System.out.println("no smallest");
        return 0;
    }

    public void rootToLeaf(int key) throws IOException {
        //go to leaf with key
        fc.position(rootAddr);
        bufferClear();
        fc.read(buff);
        processNode(rootAddr);

    }
    void processNode(int addr){
        int indicator = buff.getInt();
        if(indicator==0){//leaf, now we process leaf
            processLeaf(addr);
        }

        else {
            processIndex(addr);
        }

    }

    void processIndex(int addr){
        int num_keys = buff.getInt();
        int num_addresses = num_keys+1;
        List<Integer> index_keys = new ArrayList<Integer>();
        List<Integer> addresses = new ArrayList<>();

        //we want to get the index key that is closest to lowkey
        //once we have that

        //say lowkey = 5, and keys 4, 6, 7
        //we read 4, but 5>4, so keep going
        //5<6 so go to the pointer associated with 6 leading to elements <6

        //if 3, 5, 7
        // we cant follow pointer associated with 5 because it leads to elements <5
        //but if we got 1,2,3,4,6
        int found_low = num_keys;//means that the lowkey is greater than the last key in the index
        //so we get the last address
        for(int i=0;i<num_keys;i++){
            int currKey = buff.getInt();
            index_keys.add(currKey); //
            //indexx nodes have 2d +1 children and 2d pointers
            //key_0  points to child with key < k_0
            //want to get as close as possible
            if(lowkey<currKey){
                found_low = i;
            }
            if(lowkey >= currKey) {
                break; //means that we passed the location of where our low key would be
            }

        }

        for(int i=0;i<num_addresses;i++){
            int currAddr = buff.getInt();
            addresses.add(currAddr);
        }
        int follow_addr = addresses.get(found_low);
        processNode(follow_addr);

    }

    void processLeaf(int addr){
        //number data entries, then <k <rid>>
        int num_entries = buff.getInt();
        List<Integer> keys= new ArrayList<>();
        List< ArrayList<Tuple>> all_rids = new ArrayList<>();
        for(int i=0; i< num_entries;i++){
            keys.add(buff.getInt());
            int num_rids = buff.getInt();
            ArrayList<Tuple> rids = new ArrayList<>();//process the rids for this specific key

            for(int j=0;j<num_rids;j++){
                ArrayList<Integer> elements = new ArrayList<>();
                elements.add(buff.getInt()); elements.add(buff.getInt()); //pageid, tupleid
                rids.add(new Tuple(elements));
            }
            all_rids.add(rids);
        }
        leafKeys = keys;
        leafRids = all_rids;
        leafAddr = addr;

    }

    public void bufferClear() {
        // set limit to capacity, position=0
        buff.clear();
        byte[] zeros = new byte[4096];
        buff.put(zeros);
        buff.clear();
    }

    @Override
    public Tuple getNextTuple() {
        try{
        if(clustered){
            Tuple t = tr.read();
            return t;
        }
        else{

        }

        }
        catch (Exception e){
            System.out.println("index scan gt next tuple failed");
        }
        return null;
    }






    public void processHeader(){

        try{
            fc.read(buff); //fill buffer with values
            buff.flip(); //limit = curr pos, then pos = 0
            rootAddr  = buff.getInt();
            numLeaves = buff.getInt();
            int order = buff.getInt();
        }
        catch (Exception e){
            System.out.println("processing header failed");
        }

    }

    public void reset(){

    }


}
