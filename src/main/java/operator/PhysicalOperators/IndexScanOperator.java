package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import common.TupleReader;
import net.sf.jsqlparser.schema.Column;
import tree.BTree;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class IndexScanOperator extends Operator {

    int index;

    boolean isClustered;

    Integer lowKey;

    Integer highKey;

    public DBCatalog db;
    public String table_path;
    public TupleReader reader;

    BTree indexTree;

    int page_size = 4096;

    public String indexFilePath;

    public RandomAccessFile indexFile;

    int order;
    int number_of_leaves;

    boolean first_read;


   ArrayList<PageItem> page_stack;

    public IndexScanOperator(ArrayList<Column> outputSchema, String path, int ind, boolean clustered, int low, int high, BTree tree) throws IOException {
        super(outputSchema);
        this.index = ind;
        this.isClustered = clustered;
        this.lowKey = low;
        this.highKey = high;
        this.indexTree = tree;
        this.indexFilePath = indexTree.makeIndexFile();
        db = DBCatalog.getInstance();
        table_path = path;
        reader = DBCatalog.getInstance().getReader(path);
        indexFile = new RandomAccessFile(indexFilePath, "r");

        //add the root page to the stack at the beginning
        initial_setup();

    }

    @Override
    public void reset() {

    }

    @Override
    public Tuple getNextTuple() {
        try{
            if (!isClustered){
                int [] rid = getRID();
                if (rid == null){
                    return null;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    //get's the header of the index file to get the root page address and add that page to the stack
    public void initial_setup() throws IOException {
        PageItem header_page = new PageItem(0);
        int root_page_num = header_page.page_values[0];
        number_of_leaves = header_page.page_values[1];
        order = header_page.page_values[2];
        page_stack.add(new PageItem(root_page_num));
    }

    public int[] getRID() throws IOException {
        PageItem lastItem = page_stack.getLast();
        while (!lastItem.isLeaf && !page_stack.isEmpty()){
            Integer next_child_page = lastItem.getChildPageFromIndex();
            if(next_child_page == null){
                page_stack.removeLast();
            }else{
                page_stack.add(new PageItem(next_child_page));
            }
            lastItem = page_stack.getLast();
        }
        if (page_stack.isEmpty()){
            return null;
        }
        return lastItem.getRIDfromLeaf();
    }


    private class PageItem{

        //all the values for a page
        int[] page_values;

        //position to read from
        int leaf_curr_rid_pos;

        int leaf_curr_key_rid_count;
        boolean isLeaf;

        int number_of_index_keys;

        int curr_index_key_pos;

        int curr_index_child;

        int leaf_curr_key_pos;

        int leaf_key_count;

        //indicates if we are reading this Pageitem for the first time to determine how to move forward
        boolean first_read;

        Boolean on_final_index_key = null;

        public PageItem(int page_num) throws IOException {
            this.page_values = setup(page_num);
            this.isLeaf = page_values[0] == 0;
            this.first_read = true;
            if (isLeaf){
                this.leaf_curr_rid_pos = 4;
                this.leaf_curr_key_pos = 2;
                this.leaf_curr_key_rid_count = page_values[3];
                this.leaf_key_count = page_values[1];
            }else{
                this.number_of_index_keys = page_values[1];
                this.curr_index_key_pos = page_values[2];
                this.curr_index_child = page_values[4];
            }
        }

        //takes a page number and creates a page item of that page.
        public int[] setup(int page_num) throws IOException {
            int[] page_values = new int[page_size / 4];

            //to read from the beginning of a particular page
            indexFile.seek((long) page_num * page_size);

            //to store the binary content of the page
            byte[] pageBuffer = new byte[page_size];
            int bytesRead = indexFile.read(pageBuffer);

            if (bytesRead != page_size) {
                throw new IOException("Could not read a full page.");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(pageBuffer);

            for (int i = 0; i < page_values.length; i++) {
                page_values[i] = byteBuffer.getInt();
            }


            //returns the content of the page but in denary
            return page_values;
        }

        public Integer getChildPageFromIndex(){
            if (lowKey == null){
                lowKey = Integer.MIN_VALUE;
            }
            int curr_key = this.page_values[this.curr_index_key_pos];


            while (!(lowKey < curr_key || this.number_of_index_keys == 0)){
                this.number_of_index_keys -= 1;
                this.curr_index_key_pos  += 1;
                if (this.number_of_index_keys == 0){
                    on_final_index_key = true;
                }

            }
            if (lowKey < curr_key){
                int child_page = this.page_values[this.curr_index_key_pos + this.number_of_index_keys];
                this.number_of_index_keys -= 1;
                this.curr_index_key_pos  += 1;
                return child_page;
            }
            if (on_final_index_key) {
                if (highKey == null || curr_key < highKey) {
                    int child_page = this.page_values[this.curr_index_key_pos + this.number_of_index_keys + 1];
                    on_final_index_key = false;
                    return child_page;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }


        public int[] getRIDfromLeaf(){
            if (this.leaf_key_count == 0){
                return null;
            }if (this.leaf_curr_key_rid_count == 0) {
                this.leaf_key_count-=1;
                this.leaf_curr_key_pos = this.leaf_curr_rid_pos;
                this.leaf_curr_key_rid_count = this.page_values[this.leaf_curr_key_pos + 1];
                this.leaf_curr_rid_pos = this.leaf_curr_key_pos + 2;
            }

            //need to consider highkey and lowkey
            int curr_key = this.page_values[this.leaf_curr_key_pos];
            if (lowKey != null && highKey != null){
                while(!(lowKey <= curr_key && highKey >= curr_key)){
                    curr_key = advance();
                    if (curr_key == Integer.MAX_VALUE){
                        return null;
                    }

                }
            //if only highkey
            }else if (lowKey==null){
                while(!(highKey >= curr_key)){
                    curr_key = advance();
                    if (curr_key == Integer.MAX_VALUE){
                        return null;
                    }
                }
             //if only lowkey
            }else {
                while(!(lowKey <= curr_key)){
                    curr_key = advance();
                    if (curr_key == Integer.MAX_VALUE){
                        return null;
                    }
                }
            }
            int[] rid = new int[2];
            rid[0] = page_values[this.leaf_curr_rid_pos];
            rid[1] = page_values[this.leaf_curr_rid_pos + 1];
            this.leaf_curr_rid_pos+=2;
            this.leaf_curr_key_rid_count-=1;
            return rid;

        }

        private Integer advance(){
            this.leaf_curr_key_pos += (this.leaf_curr_key_rid_count * 2) + 2;
            if (this.leaf_curr_key_pos >= this.page_values.length){
                return Integer.MAX_VALUE;
            }
            this.leaf_curr_key_rid_count = this.page_values[this.leaf_curr_key_pos + 1];
            this.leaf_curr_rid_pos = this.leaf_curr_key_pos + 2;
            return this.page_values[this.leaf_curr_key_pos];
        }




    }

}
