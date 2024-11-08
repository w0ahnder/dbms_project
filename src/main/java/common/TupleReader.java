package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class TupleReader {

  private int numTuples;
  private int numAttr;
  private int cap; // maximum number of tuples that the page can store
  private File file;
  private ByteBuffer buff;
  private FileChannel fc;
  private boolean done;
  private boolean page_start;
  private int pageID;
  private int tupleID;

  // every page is 4096 bytes
  // each page stores meta data:  #attributes of the tuples stored on page,  #tuples on page
  // boats has x154  = 340 tuples on page

  public TupleReader(File file) throws FileNotFoundException {
    this.file = file;
    buff = ByteBuffer.allocate(4096);
    FileInputStream fin = new FileInputStream(file);
    fc = fin.getChannel();
    numTuples = 0;
    numAttr = 0;
    done = false;
    page_start = true;
    pageID = -1;
    tupleID = -1;
  }

  // keep getting elements from the file as long as there is space
  // if no more data, fcin.read() returns -1
  // if page is full
  // relative get methods don't update position or limit
  // first 4 bytes is number of attributes
  // second 4 bytes is number of tuples
  // third 4 bytes is where data starts
  public Tuple read() throws IOException {
    // read from channel into buffer; no more data to read
    while (!done) {
      // beginning of new page
      if (page_start) {
        // try to read from channel into buffer; check how many bytes can be read
        int reads = fc.read(buff);
        if (reads <= 0) {
          return null;
        }
        // otherwise get new  page details
        newPage();
        page_start = false;
        pageID++;
      }
      // position and limit still has elements between them => 1+ tuples
      if (buff.hasRemaining()) {
        // read one record
        ArrayList<Integer> tuple = new ArrayList<>();
        for (int i = 0; i < numAttr; i++) {
          tuple.add(buff.getInt());
        }
        tupleID++;
        return new Tuple(tuple);
      }
      // otherwise position is at limit => 0 tuples, get next page
      page_start = true;
      tupleID = -1;
      // clear out everything from buffer
      bufferClear();
    }
    // System.out.println("Reader tuple: " + null);
    return null;
  }

  // set the page up to be read
  public void newPage() {
    // limit=pos, pos=0
    buff.flip();
    numAttr = buff.getInt();
    numTuples = buff.getInt();
    // total number of bytes to get after numAtrr, numTuples
    // don't read past this
    int limit = numAttr * numTuples * 4;

    // how is limit set?
    // is it position + limit, or 0 + limit
    buff.limit(limit + 8);
  }

  public void bufferClear() {
    // set limit to capacity, position=0
    buff.clear();
    byte[] zeros = new byte[4096];
    buff.put(zeros);
    buff.clear();
  }

  public int pID() {
    return pageID;
  }

  public int tID() {
    return tupleID;
  }

  public void reset() throws IOException {
    // buff.clear();
    bufferClear();
    fc.close();
    fc = (new FileInputStream(file)).getChannel();
    buff = ByteBuffer.allocate(4096);
    page_start = true;
    done = false;
    numAttr = 0;
    numTuples = 0;
    pageID = -1;
    tupleID = -1;
  }

  /***
   * Resets the index at which the Tuple Reader starts to read tuples from
   * @param index is the index to reset the Tuple Reader to
   * @throws IOException
   */
  public void reset(int index) throws IOException {
    // index is the number of tuples we have read so far?
    // file channel position function counts the number of bytes from the beginning of the file
    // to the current position
    // each page of the file is 4096 bytes

    // number of bytes is 4096 * page tuple is on
    // have to calculate offset
    // =>>>>>>>>> (index  - tuple index on previous page)*4*numcol + 8
    int tuplesperpage = (4088) / (numAttr * 4);
    int pageforindex = (int) Math.ceil((double) index / (double) tuplesperpage);
    // now calculate offset into the page
    // =>calculate index of first tuple on this page
    int first_indx =
        pageforindex == 0
            ? 0
            : (pageforindex - 1) * tuplesperpage; // (4*2) = 8 first tuple index on the page we want
    int offset = 8 + (index - first_indx) * (numAttr * 4); // offset into page, 8 for metadata

    int total_bytes =
        4096
            * (pageforindex == 0
                ? 0
                : (pageforindex - 1)); // total page offset from beginning of file

    fc.position(total_bytes); // set file scan to location of tuple
    bufferClear(); // clear out all elements from buffer
    page_start = true;
    done = false;
    // have to set position of buffer so that the buffer only reads this page's tuples; dont go onto
    // next
    buff.clear(); // position is at 0, limit is at capacity
    int reads = fc.read(buff);

    if (reads >= 0) { // we can read from from channel
      newPage(); // get metadata
      buff.position(offset); // byte to start reading at on this page
      page_start = false;
    }
  }
}
