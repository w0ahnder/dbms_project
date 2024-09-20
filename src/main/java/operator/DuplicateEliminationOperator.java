package operator;

import common.Tuple;
import net.sf.jsqlparser.schema.Column;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class DuplicateEliminationOperator extends Operator{

    SortOperator so;

    Tuple curr;

    public DuplicateEliminationOperator(ArrayList<Column> outputSchema, SortOperator sortOp){
        super(outputSchema);
        so = sortOp;
        curr = null;

    }

    @Override
    public void reset() {
        curr = new Tuple(new ArrayList<>());
        so.reset();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple next = so.getNextTuple();
        if (curr == null){
            curr = next;
            return curr;
        }
        while (curr.equals(next)){
            next = so.getNextTuple();
        }
        curr = next;
        return next;

    }
}
