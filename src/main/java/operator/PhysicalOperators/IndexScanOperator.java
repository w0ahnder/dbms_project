package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import common.TupleReader;
import net.sf.jsqlparser.schema.Column;
import tree.BTree;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class IndexScanOperator extends Operator {

    int index; //tuple column that we care about

    boolean isClustered;

    Integer lowKey;

    Integer highKey;

    public DBCatalog db;
    public String table_path;
    public TupleReader reader;


    int page_size = 4096;

    public String indexFilePath;

    int order;
    int number_of_leaves;

    boolean first_read; //to check if this is the first time reading from a clustered index


    ByteBuffer buff;

    FileChannel fileChannel;
   ArrayList<PageItem> page_stack = new ArrayList<>();

    public IndexScanOperator(ArrayList<Column> outputSchema, File table_file, int ind, boolean clustered, int low, int high, File index_file) throws IOException {
        super(outputSchema);
        this.index = ind;
        this.isClustered = clustered;
        this.lowKey = low;
        this.highKey = high;
        this.indexFilePath = "placeholder";
        db = DBCatalog.getInstance();
        //table_path = path;
        reader = new TupleReader(table_file); //to read table file
        first_read = true;
        buff = ByteBuffer.allocate(4096);
        FileInputStream fin = new FileInputStream(index_file);
        fileChannel = fin.getChannel();

        //add the root page to the stack at the beginning
        initial_setup();

    }

    @Override
    public void reset() {
        page_stack = new ArrayList<>();
        try{
            initial_setup();
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    @Override
    public Tuple getNextTuple() {
        try{
            if (!isClustered){ //if it's not a clustered index
                int [] rid = getRID();
                if (rid == null){
                    return null;
                }
                reader.reset(rid[0], rid[1]);
                return reader.read();
            }else{ //if clustered
                Tuple output;
                if (first_read){ //checks if this is the first time calling getNextTuple
                    int [] rid = getRID();
                    if (rid == null){
                        return null;
                    }
                    reader.reset(rid[0], rid[1]); //fixes the tupleReader pointer to a particular position in the file
                    output = reader.read(); //reads the tuple at that place
                    first_read = false;
                }else{
                    output = reader.read();
                }
                if (output == null){ //this means all pages have been read
                    return null;
                }

                //logic to use highkey and lowkey
                int compare = output.getElementAtIndex(index);
                    if (lowKey <= compare && highKey >= compare) {
                        System.out.println(output);
                        return output;
                    }
                    return null;
//                }else if (lowKey == null && highKey == null){
//                    return output;
//                }
//                else if (lowKey == null){
//                    if (highKey >= compare){
//                        return output;
//                    }return null;
//                }else {
//                    if (lowKey <= compare){
//                        return output;
//                    }return null;
//                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    //get's the header of the index file to get the root page address and add that page to the stack
    public void initial_setup() throws IOException {
        PageItem header_page = new PageItem(0);
        System.out.print("header page before root; " + header_page);
        int root_page_num = header_page.pageValues.get(0);
        number_of_leaves = header_page.pageValues.get(1);
        order = header_page.pageValues.get(2);
        System.out.println("about to add root page");
        page_stack.add(new PageItem(root_page_num));
    }

    public int[] getRID() throws IOException {
        while (!page_stack.isEmpty()){
            PageItem last_item = page_stack.getLast();
            while (!last_item.isLeaf){
                Integer page = last_item.getChildPageFromIndex();
                if (page == null){
                    page_stack.removeLast();
                    if (page_stack.isEmpty()){
                        return null;
                    }
                    last_item = page_stack.getLast();
                }else{
                   PageItem new_item = new PageItem(page);
                   page_stack.add(new_item);
                   last_item = page_stack.getLast();
                }
            }
            int[] rid = last_item.getRIDfromLeaf();
            if (rid == null){
                page_stack.removeLast();
            }else{
                System.out.println("current rid to be read is " + Arrays.toString(rid));
                return rid;
            }
        }
        return null;

    }


    private class PageItem{

        //all the values for a page
        ArrayList<Integer> pageValues;

        //position to read from
        int currentLeafRidPosition;

        int leafCurrentKeyRIDCount;
        boolean isLeaf;

        int numberOfIndexKeys;

        int currentIndexKeyPosition;
        

        int leafCurrentKeyPosition;

        int leafKeyCount;

        //indicates if we are reading this Pageitem for the first time to determine how to move forward
        boolean first_read;

        int untouchedIndexKeyCount;

        int page = 0;

        public PageItem(int page_num) throws IOException {
            page = page_num;
            this.pageValues = setup(page_num);
            this.isLeaf = pageValues.get(0) == 0;
            this.first_read = true;
            if (isLeaf){
                this.currentLeafRidPosition = 4;
                this.leafCurrentKeyPosition = 2;
                this.leafCurrentKeyRIDCount = pageValues.get(3);
                this.leafKeyCount = pageValues.get(1);
            }else{
                this.numberOfIndexKeys = pageValues.get(1) + 1;
                untouchedIndexKeyCount = this.numberOfIndexKeys - 1;
                this.currentIndexKeyPosition = 2;
            }
        }

        public String toString() {
            StringBuilder build = new StringBuilder();
            if (this.isLeaf){
                build.append("Leaf: pageNumber: " + page + " currentLeafRidPosition:")
                        .append(this.leafCurrentKeyPosition)
                        .append(", leafCurrentKeyPosition:")
                        .append(this.leafCurrentKeyPosition)
                        .append(", leafCurrentKeyRIDCount:")
                        .append(this.leafCurrentKeyRIDCount)
                        .append(", leafKeyCount:")
                        .append(this.leafKeyCount);
            }else{
                build.append("Index: pageNumber: " + page + " numberOfIndexKeys:")
                        .append(this.numberOfIndexKeys)
                        .append(", currentIndexKeyPosition: ")
                        .append(this.currentIndexKeyPosition);

            }
            build.append(", list values: ")
                    .append(this.pageValues);


            return build.toString();

        }

        //takes a page number and creates a page item of that page.
        public ArrayList<Integer> setup(int page_num) throws IOException {
            ArrayList<Integer> pageValues = new ArrayList<>();

//            }
            fileChannel.position((long) page_num * page_size);
            fileChannel.read(buff);
            buff.flip();
//            for (int i = 0; i < pageValues.length; i++) {
//                pageValues[i] = buff.getInt();
//            }

            int i=0;
            while(buff.hasRemaining()){
                int val = buff.getInt();
                pageValues.add(val);
                //System.out.println("in while loop " + val);
                i++;
            }
            //System.out.println("page num: " + page_num + " pagevalues: " + pageValues.toString());
            buff.clear();

            //returns the content of the page but in denary
            return pageValues;
        }

        public Integer getChildPageFromIndex(){
            if (this.numberOfIndexKeys == 0){ //done reading all keys and children in that index
                return null;
            }

            int curr_key = this.pageValues.get(this.currentIndexKeyPosition);

            while (!((lowKey < curr_key) || this.numberOfIndexKeys == 1)){
                this.numberOfIndexKeys -= 1;
                this.currentIndexKeyPosition  += 1;
                curr_key = this.pageValues.get(this.currentIndexKeyPosition);
            }
            if (lowKey < curr_key){
                int child_page = this.pageValues.get(this.currentIndexKeyPosition + untouchedIndexKeyCount);
                this.numberOfIndexKeys -= 1;
                this.currentIndexKeyPosition  += 1;
                return child_page;
            }
            if (curr_key < highKey) {
                this.numberOfIndexKeys-=1;
                return this.pageValues.get(this.currentIndexKeyPosition + untouchedIndexKeyCount + 1);
            } else {
                return null;
            }
        }


        public int[] getRIDfromLeaf(){
            if (this.leafKeyCount == 0){ //done reading all the keys and their values
                return null;
            }if (this.leafCurrentKeyRIDCount == 0) {//done reading all the values for a particular key
                this.leafKeyCount-=1;
                this.leafCurrentKeyPosition = this.currentLeafRidPosition;
                this.leafCurrentKeyRIDCount = this.pageValues.get(this.leafCurrentKeyPosition + 1);
                this.currentLeafRidPosition = this.leafCurrentKeyPosition + 2;
            }

            //need to consider highkey and lowkey
            int curr_key = this.pageValues.get(this.leafCurrentKeyPosition);

            //if highkey and lowkey

                while (!(lowKey <= curr_key && highKey >= curr_key)) {
                    curr_key = advance();
                    if (curr_key == Integer.MAX_VALUE) {
                        return null;
                    }
                }

            int[] rid = new int[2];
            rid[0] = pageValues.get(this.currentLeafRidPosition);
            rid[1] = pageValues.get(this.currentLeafRidPosition + 1);
            this.currentLeafRidPosition+=2;
            this.leafCurrentKeyRIDCount-=1;
            return rid;

        }

        private Integer advance(){
            this.leafKeyCount-=1;
            this.leafCurrentKeyPosition += (this.leafCurrentKeyRIDCount * 2) + 2;
            if (this.leafKeyCount == 0 || this.leafCurrentKeyPosition >= 1024){
                return Integer.MAX_VALUE;
            }
            this.leafCurrentKeyRIDCount = this.pageValues.get(this.leafCurrentKeyPosition + 1);
            this.currentLeafRidPosition = this.leafCurrentKeyPosition + 2;
            return this.pageValues.get(this.leafCurrentKeyPosition);
        }




    }

}
