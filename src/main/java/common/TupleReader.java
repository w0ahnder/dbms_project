package common;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TupleReader {

 private int numTuples;
 private int numAttr;
 private int cap; //maximum number of tuples that the page can store
 private File file;
 private ByteBuffer buff;
 private FileChannel fc;
 private boolean done;
 private boolean page_start;
 //every page is 4096 bytes
 //each page stores meta data:  #attributes of the tuples stored on page,  #tuples on page
 //boats has x154  = 340 tuples on page

    public TupleReader(File file ) throws FileNotFoundException {
       this.file= file;
       buff = ByteBuffer.allocate(4096);
       FileInputStream fin = new FileInputStream(file);
       fc= fin.getChannel();
       numTuples=0;
       numAttr = 0;
       done = false;
       page_start = true;

    }

    //keep getting elements from the file as long as there is space
    // if no more data, fcin.read() returns -1
    //if page is full
    //relative get methods don't update position or limit
    //first 4 bytes is number of attributes
    //second 4 bytes is number of tuples
    //third 4 bytes is where data starts
    public Tuple read() throws IOException {
        //read from channel into buffer; no more data to read
        while(!done) {
            //beginning of new page
            if (page_start) {
                //try to read from channel into buffer; check how many bytes can be read
                int reads = fc.read(buff);
                if (reads <= 0) {
                    return null;
                }
                //otherwise get new  page details
                newPage();
                page_start = false;
            }
            //position and limit still has elements between them => 1+ tuples
            if (buff.hasRemaining()){
                //read one record
                ArrayList<Integer> tuple = new ArrayList<>();
                for (int i = 0; i < numAttr; i++) {
                    tuple.add(buff.getInt());
                }
                //System.out.println("Reader tuple: " + tuple.toString());
                return new Tuple(tuple);
            }
            //otherwise position is at limit => 0 tuples, get next page
            page_start = true;
            //clear out everything from buffer
            bufferClear();
        }
        //System.out.println("Reader tuple: " + null);
        return null;
    }

    //set the page up to be read
    public void newPage(){
        //limit=pos, pos=0
        buff.flip();
        numAttr = buff.getInt();
        numTuples = buff.getInt();
        //total number of bytes to get after numAtrr, numTuples
        //don't read past this
        int limit = numAttr*numTuples*4;

        //how is limit set?
        // is it position + limit, or 0 + limit
        buff.limit(limit+8);
    }

    public void bufferClear(){
        //set limit to capacity, position=0
        buff.clear();
        byte[] zeros = new byte[4096];
        buff.put(zeros);
        buff.clear();
    }

    public void reset() throws IOException {
        buff.clear();
        fc.close();
        fc = (new FileInputStream(file)).getChannel();
        buff = ByteBuffer.allocate(4096);
        page_start = true;
        done=false;
        numAttr=0;
        numTuples=0;

    }





}