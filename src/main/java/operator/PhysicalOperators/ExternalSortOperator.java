package operator.PhysicalOperators;

import common.Tuple;
import common.TupleReader;
import common.TupleWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import org.apache.logging.log4j.*;

/** Class that extends SortOperator Class to sort tables in preparation for SMJ. */
public class ExternalSortOperator extends SortOperator {
  // every page is 4096 bytes

  ArrayList<Tuple> result = new ArrayList<>();
  private int bufferSize;
  TupleWriter tw;
  SortOperator sortOperator;
  private static final Logger logger = LogManager.getLogger();
  String tempDir;
  Integer bufferPages;
  int numTuples;
  public TupleReader reader = null;
  Operator op;
  List<OrderByElement> orderElements;

  TupleComparator comparator;

  /**
   * Creates a ExternalSortOperator Object
   *
   * @param outputSchema the Schema of the output which becomes its child.
   * @param orderElements a list of the columns which should be used to sort the tuples
   * @param sc the child operator
   * @param bufferPages the number of pages we can load into memory at once
   * @param tempDir the directory to dump the sorted tuples for each run
   */
  public ExternalSortOperator(
      ArrayList<Column> outputSchema,
      List<OrderByElement> orderElements,
      Operator sc,
      Integer bufferPages,
      String tempDir) {
    super(outputSchema, orderElements, sc);
    this.tempDir = tempDir + "/" + "test" + UUID.randomUUID();
    File tmp = new File(this.tempDir);
    tmp.mkdir();
    this.op = sc;
    this.comparator = new TupleComparator();
    this.bufferPages = bufferPages;
    this.bufferSize = bufferPages * 4096;
    int tupleSize = op.outputSchema.size() * 4;
    this.numTuples = this.bufferSize / tupleSize;
    this.orderByElements = orderElements;
    // op.reset();
    sort();
  }

  /** Sorts the tuples loaded into memory. */
  private void sort() {
    int run = 0;
    int tupleSize = op.outputSchema.size() * 4;
    int numTuples = this.bufferSize / tupleSize;
    // numTuples = 2; // remove after testing
    ArrayList<Tuple> tuples = new ArrayList<>(numTuples);
    sortOperator = new SortOperator(outputSchema, this.orderByElements, op);
    op.reset();
    // returned null here
    Tuple next = op.getNextTuple();
    try {
      while (next != null) {
        while (numTuples > 0) {
          if (next == null) {
            break;
          }
          tuples.add(next);
          next = op.getNextTuple();
          numTuples--;
        }
        tuples = sortOperator.sort(tuples);
        TupleWriter tw = new TupleWriter(tempDir + "/run" + run);
        for (Tuple t : tuples) {
          tw.write(t);
        }
        tw.close();
        run++;

        tuples = new ArrayList<>(numTuples);
        numTuples = this.bufferSize / tupleSize;
        // numTuples = 2; // remove after testing
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }
    if (run == 0) return;
    merge(run);
    System.out.println("merging for ordeer by elelment " + orderByElements);
  }

  /** Merge sep in External Sort Algoritms */
  private void merge(int Pass) {
    int num = 0;
    int pass = Pass;

    PriorityQueue<Tuple> pQueue = new PriorityQueue<>(comparator);

    // int totalPass = Pass;
    List<TupleReader> buffer = new ArrayList<>();

    if (pass == 1) {
      try {
        reader = new TupleReader(new File(tempDir + "/run" + num));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    while (num < pass - 1) {
      try {
        for (int i = num; i < num + numTuples; i++) {
          if (i < pass) {
            buffer.add(new TupleReader(new File(tempDir + "/run" + i)));
          }
        }
        Tuple tp = null;
        for (TupleReader tr : buffer) {
          tp = tr.read();
          if (tp != null) {
            tp.tupleReader = tr;
            pQueue.add(tp);
          }
        }
        TupleWriter outputPage = new TupleWriter(tempDir + "/run" + pass);
        while (!pQueue.isEmpty()) {
          tp = pQueue.poll();
          outputPage.write(tp);
          TupleReader tr = tp.tupleReader;
          tp = tr.read();
          if (tp != null) {
            tp.tupleReader = tr;
            pQueue.add(tp);
          }
        }
        outputPage.close();
        reader = new TupleReader(new File(tempDir + "/run" + pass));
        //reader.newPage();
        pass++;
        num = num + numTuples;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Resets the operator to the beginning. */
  @Override
  public void reset() {
    if (reader == null) return;
    try {
      reader.reset();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
//external sort operator with order by elements sailors.c
  // sort operator: op is SMJ left is external sort for sailors, right is external sort for reserves
  // the reader is null for some reason
  /** Resets the operator to the to a particular position, not the beginning. */
  @Override
  public void reset(int index) {
    try {
      reader.reset(index);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * returns the next tuple from the sorted files
   *
   * @return Tuple or null if we are at the end of the file
   */
  @Override
  public Tuple getNextTuple() {
    if (reader == null) {
      return null;
    }
    try {
      Tuple tp = reader.read();
      return tp;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
