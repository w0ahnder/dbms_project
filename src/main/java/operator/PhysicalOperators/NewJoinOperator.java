package operator.PhysicalOperators;

import common.Tuple;

import java.io.PrintStream;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/** Class that extends Operator Class to handle join SQL queries using Block Nested Loop Join. */
public class NewJoinOperator extends Operator {

  Operator child;
  ArrayList<Column> oldSchema;
  ArrayList<Column> schema;

  public NewJoinOperator(Operator child, ArrayList<Column> oldSchema, ArrayList<Column> schema) {
    super(schema);
    this.oldSchema = oldSchema;
    this.child = child;
  }

  public void printPhys(PrintStream ps, int level){
    child.printPhys(ps, level);
  }
  public ArrayList<Column> getOutputSchema() {
    return this.outputSchema;
  }

  public Tuple getNextTuple() {

    Tuple res = this.child.getNextTuple();

    if (res == null) {
      return null;
    }
    if (this.outputSchema == this.oldSchema) {
      return res;
    }
    ArrayList<Integer> result = res.getAllElements();
    ArrayList<Integer> output = new ArrayList<>();
    for (Column col : this.getOutputSchema()) {
      output.add(result.get(this.oldSchema.indexOf(col)));
    }
    return new Tuple(output);
  }

  public void reset() {
    this.child.reset();
  }
}
