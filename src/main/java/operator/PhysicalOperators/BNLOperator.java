package operator.PhysicalOperators;

import common.DBCatalog;
import common.SelectVisitor;
import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/** Class that extends Operator Class to handle join SQL queries using Block Nested Loop Join. */
public class BNLOperator extends Operator {
  public Operator left;
  public Operator right;
  public int block;
  public int cols;
  public int num_tup;
  public ArrayList<Tuple> buffer;
  public boolean filled = false;
  public int reads;
  public int tot_elements;
  public Expression condition;
  public Tuple s;
  public Tuple check;
  public Tuple check_t;

  /**
   * Creates a BNLOperator Object
   *
   * @param schema the Schema of the output which becomes its child.
   * @param left the child operator which is the outer table and is of type Operator
   * @param right the child operator which is the inner table and is of type Operator
   */
  public BNLOperator(ArrayList<Column> schema, Operator left, Operator right, Expression cond) {
    super(schema);
    this.left = left;
    this.right = right;
    condition = cond;
    cols = schema.size();
    block = DBCatalog.getInstance().blockSize();
    num_tup = ((4096) / (cols * 4)) * block;
    reads = 0; // havent called get next tuple yet
    tot_elements =
        0; // keep track of how many tuples we have in B bc sometimes the block may not be filled up
    buffer = new ArrayList<>();
    // fill();//get block B from left operators

  }

  /**
   * Resets pointer on the operator object to the beginning. Achieves this by resetting its left and
   * right children and resetting some tracking fields
   */
  public void reset() {
    reads = 0;
    tot_elements = 0;
    filled = false;
    left.reset();
    right.reset();
    fill();
  }

  /**
   * Fills the buffer with the correct amount of tuples from the outer table that can fit on the
   * buffer
   */
  public void fill() {
    // get the total number of elements that can fit in the buffer where each page is 4096 bytes
    // (4096/ (num of col * 4) ) * block = total number of tuples we should have in the block B
    // initialize the block

    // when we reach the end of all the blocks and left.getNextTuple is called, it starts reading
    // from
    // the beginning again
    reads = 0;
    tot_elements = 0;
    buffer.clear();
    Tuple t;
    // keeps reading tuples until left is null, but then the select op resets it already, so next
    // set is
    // fresh remove reset from select next tuple
    while ((tot_elements < num_tup) && (t = left.getNextTuple()) != null) {
      buffer.add(t);
      tot_elements++;
      check_t = t;
    }
    filled = tot_elements > 0; // if any elements were read
  }

  /**
   * Get next tuple from operator
   *
   * @return Tuple, or null if we are at the end. Retrieves next tuple by calling getNextTuple() on
   *     its left and right child operator and checking that those tuples satisfy the condition in
   *     the expression
   */
  public Tuple getNextTuple() {
    if (!filled) fill();
    // if not filled, fill it; and if filled is still false, there are no more tuples in
    // Tuple reader, so we return null
    if (!filled) {
      // reset();
      return null;
    }
    while (reads < tot_elements) {
      Tuple r = buffer.get(reads);
      s = right.getNextTuple();
      while (s != null) {
        ArrayList<Integer> elements = new ArrayList<>();
        elements.addAll(r.getAllElements());
        elements.addAll(s.getAllElements());
        Tuple curr = new Tuple(elements);
        // s = right.getNextTuple();
        if (this.condition == null) {
          return curr;
        }
        SelectVisitor sv = new SelectVisitor(curr, concatSchema(), this.condition);
        if (sv.evaluate_expr()) {
          return curr;
        }
        // condiitons not met, so get next s Tuple
        s = right.getNextTuple();
      }
      // s = null, so now we get next element in block
      right.reset();
      reads++;
    }
    // all elements in block were read, get next block
    right.reset();
    fill();
    return this.getNextTuple();
  }

  /** Concatenates the schema of the two tuples */
  public ArrayList<Column> concatSchema() {
    ArrayList<Column> conc = new ArrayList<Column>();
    conc.addAll(left.getOutputSchema());
    conc.addAll(right.getOutputSchema());
    return conc;
  }
}
