package common;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class TupleWriter {

 private int numTuples;
 private int numAttr;
 private int cap; //maximum number of tuples that the page can store
 private File file;
 private ByteBuffer buff;
 private FileChannel fc;
 private FileOutputStream fout;
 private boolean done;
 private boolean init;
 private int space;
 //every page is 4096 bytes
 //each page stores meta data:  #attributes of the tuples stored on page,  #tuples on page
 //boats has x154  = 340 tuples on page

    public TupleWriter(String outFile ) throws FileNotFoundException {
       fout = new FileOutputStream(outFile);
       fc = fout.getChannel();
       buff = ByteBuffer.allocate(4096);

       numTuples=0;
       numAttr = 0;
       space=4096;
       done = false;
       //if we have set the metadata
       init = false;

    }

    //buffer the tuples in memory (put them in)
    // until there is a full page, then write the page
    //get the number of attributes in tuple
    //keep count of number of tuples
    public void write(Tuple tuple) throws IOException {
        numAttr = tuple.getAllElements().size();
        if(!init){
            //relative, update position
            //numTuples=0;
            buff.putInt(numAttr);
            buff.putInt(numTuples);//no tuples yet
            space -= 8;
            init=true;// page has metadata
        }
        //check if there's enough space left on page
        //size of a tuple is numAttr*4
        if( space - 4*numAttr >=0){
            for(int i=0; i<numAttr;i++){
                buff.putInt(tuple.getElementAtIndex(i));
            }
            numTuples++;
            buff.putInt(4, numTuples);
            space -= 4*numAttr;
        }
        else{//no more space on page
            //have to flush page, fill rest with 0's, reset buffer
            //write tuple on next page
            while(buff.hasRemaining()) buff.putInt(0);
            buff.clear(); //? limit = cap, pos =0 so you don't write past
            fc.write(buff);
            init(); //new page
            write(tuple);
        }
    }

    public void init(){
        space  =4096;
        numTuples=0;
        buff.clear();
        buff.put(new byte[4096]);
        buff.clear();
        init=false;
    }

    public void close() throws IOException {

        while (buff.hasRemaining()) buff.putInt(0);
        buff.clear();
        if(numTuples ==0){
            buff = ByteBuffer.allocate(0);
        }
        fc.write(buff);
        fc.close();
        fout.close();
    }

}