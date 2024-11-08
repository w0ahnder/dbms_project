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
   ArrayList<PageItem> page_stack;

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
        int root_page_num = header_page.pageValues[0];
        number_of_leaves = header_page.pageValues[1];
        order = header_page.pageValues[2];
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
        int[] pageValues;

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

        Boolean on_final_index_key = null;

        public PageItem(int page_num) throws IOException {
            this.pageValues = setup(page_num);
            this.isLeaf = pageValues[0] == 0;
            this.first_read = true;
            if (isLeaf){
                this.currentLeafRidPosition = 4;
                this.leafCurrentKeyPosition = 2;
                this.leafCurrentKeyRIDCount = pageValues[3];
                this.leafKeyCount = pageValues[1];
            }else{
                this.numberOfIndexKeys = pageValues[1] + 1;
                untouchedIndexKeyCount = this.numberOfIndexKeys - 1;
                this.currentIndexKeyPosition = 2;
            }
        }

        public String toString() {
            StringBuilder build = new StringBuilder();
            if (this.isLeaf){
                build.append("Leaf: currentLeafRidPosition:")
                        .append(this.leafCurrentKeyPosition)
                        .append(", leafCurrentKeyPosition:")
                        .append(this.leafCurrentKeyPosition)
                        .append(", leafCurrentKeyRIDCount:")
                        .append(this.leafCurrentKeyRIDCount)
                        .append(", leafKeyCount:")
                        .append(this.leafKeyCount);
            }else{
                build.append("Index: numberOfIndexKeys:")
                        .append(this.numberOfIndexKeys)
                        .append(", currentIndexKeyPosition:")
                        .append(this.currentIndexKeyPosition);

            }
            build.append("list values: ")
                    .append(Arrays.toString(this.pageValues));


            return build.toString();

        }

        //takes a page number and creates a page item of that page.
        public int[] setup(int page_num) throws IOException {
            int[] pageValues = new int[page_size / 4];

//            //to read from the beginning of a particular page
//            indexFile.seek((long) page_num * page_size);
//            fileChannel.position((long) page_num * page_size);
//
//            //to store the binary content of the page
//            byte[] pageBuffer = new byte[page_size];
//            int bytesRead = indexFile.read(pageBuffer);
//
//            if (bytesRead != page_size) {
//                throw new IOException("Could not read a full page.");
//            }
//
//            ByteBuffer byteBuffer = ByteBuffer.wrap(pageBuffer);
//
//            for (int i = 0; i < pageValues.length; i++) {
//                pageValues[i] = byteBuffer.getInt();
//            }
            fileChannel.position((long) page_num * page_size);
            fileChannel.read(buff);
            for (int i = 0; i < pageValues.length; i++) {
                pageValues[i] = buff.getInt();
            }


            //returns the content of the page but in denary
            return pageValues;
        }

        public Integer getChildPageFromIndex(){
            if (this.numberOfIndexKeys == 0){ //done reading all keys and children in that index
                return null;
            }

            int curr_key = this.pageValues[this.currentIndexKeyPosition];

            while (!((lowKey < curr_key) || this.numberOfIndexKeys == 1)){
                this.numberOfIndexKeys -= 1;
                this.currentIndexKeyPosition  += 1;
                curr_key = this.pageValues[this.currentIndexKeyPosition];
            }
            if (lowKey < curr_key){
                int child_page = this.pageValues[this.currentIndexKeyPosition + untouchedIndexKeyCount];
                this.numberOfIndexKeys -= 1;
                this.currentIndexKeyPosition  += 1;
                return child_page;
            }
            if (curr_key < highKey) {
                this.numberOfIndexKeys-=1;
                return this.pageValues[this.currentIndexKeyPosition + untouchedIndexKeyCount + 1];
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
                this.leafCurrentKeyRIDCount = this.pageValues[this.leafCurrentKeyPosition + 1];
                this.currentLeafRidPosition = this.leafCurrentKeyPosition + 2;
            }

            //need to consider highkey and lowkey
            int curr_key = this.pageValues[this.leafCurrentKeyPosition];

            //if highkey and lowkey

                while (!(lowKey <= curr_key && highKey >= curr_key)) {
                    curr_key = advance();
                    if (curr_key == Integer.MAX_VALUE) {
                        return null;
                    }

                }

            //if only highkey
//            }else if (lowKey==null){
//                if (highKey != null){
//                    while(!(highKey >= curr_key)){
//                        curr_key = advance();
//                        if (curr_key == Integer.MAX_VALUE){
//                            return null;
//                        }
//                    }
//                }
//
//             //if only lowkey
//            }else {
//                if (highKey == null){
//                    while((lowKey <= curr_key)){
//                        curr_key = advance();
//                        if (curr_key == Integer.MAX_VALUE){
//                            return null;
//                        }
//                    }
//                }
//            }
            int[] rid = new int[2];
            rid[0] = pageValues[this.currentLeafRidPosition];
            rid[1] = pageValues[this.currentLeafRidPosition + 1];
            this.currentLeafRidPosition+=2;
            this.leafCurrentKeyRIDCount-=1;
            return rid;

        }

        private Integer advance(){
            this.leafKeyCount-=1;
            if (this.leafKeyCount == 0){
                return Integer.MAX_VALUE;
            }
            this.leafCurrentKeyPosition += (this.leafCurrentKeyRIDCount * 2) + 2;
            this.leafCurrentKeyRIDCount = this.pageValues[this.leafCurrentKeyPosition + 1];
            this.currentLeafRidPosition = this.leafCurrentKeyPosition + 2;
            return this.pageValues[this.leafCurrentKeyPosition];
        }




    }

}
