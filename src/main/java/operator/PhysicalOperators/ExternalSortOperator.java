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

public class ExternalSortOperator extends SortOperator {
  // every page is 4096 bytes

  ArrayList<Tuple> result = new ArrayList<>();
  private int bufferSize;
  TupleWriter tw;
  InMemorySortOperator sortOperator;
  private static final Logger logger = LogManager.getLogger();
  String tempDir;
  Integer bufferPages;
  int numTuples;
  public TupleReader reader = null;

  TupleComparator comparator;

  public ExternalSortOperator(
      ArrayList<Column> outputSchema,
      List<OrderByElement> orderElements,
      Operator sc,
      Integer bufferPages,
      String tempDir) {
    super(outputSchema, orderElements, sc);
    this.tempDir = tempDir;
    this.comparator = new TupleComparator();
    this.bufferPages = bufferPages;
    this.bufferSize = bufferPages * 4096;
    int tupleSize = op.outputSchema.size() * 4;
    this.numTuples = this.bufferSize / tupleSize;

    sort();
  }

  private void sort() {
    int run = 0;
    int tupleSize = op.outputSchema.size() * 4;
    int numTuples = this.bufferSize / tupleSize;
    // numTuples = 2; // remove after testing
    ArrayList<Tuple> tuples = new ArrayList<>(numTuples);
    sortOperator = new InMemorySortOperator(outputSchema, this.orderByElements, op, tuples, tw);

    System.out.println("calling next tuple");
    Tuple next = op.getNextTuple();
    System.out.println("back");
    try {
      while (next != null) {
        System.out.println("Next was not null");
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
    }
    if (run == 0) return;
    merge(run);
  }

  /** Merge sep in External Sort Algoritms */
  private void merge(int Pass) {
    int num = 0;
    int pass = Pass;

    PriorityQueue<Tuple> pQueue = new PriorityQueue<>(comparator);

    // int totalPass = Pass;
    List<TupleReader> buffer = new ArrayList<>();

    while (num < pass - 1) {
      try {
        // numTuples = 2; // remove after testing
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
        pass++;
        num = num + numTuples;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void reset() {
    super.reset();
  }

  @Override
  public Tuple getNextTuple() {
    if (reader == null) return null;
    try {
      Tuple tp = reader.read();
      return tp;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
