package operator;

import common.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class SortOperator extends Operator {
    List<OrderByElement> orderByElements;
    Operator op;

    ArrayList<Tuple> result = new ArrayList<>();

    Integer curr;

    public SortOperator(ArrayList<Column> outputSchema, List<OrderByElement> orderElements, Operator sc){
        super(outputSchema);
        orderByElements= orderElements;
        op = sc;
        curr = 0;
        Tuple tuple = sc.getNextTuple();
        while (tuple != null){
            result.add(tuple);
            tuple = sc.getNextTuple();
        }
        System.out.println("before sorting" + result );
        result.sort(new TupleComparator(outputSchema));
        System.out.println("after sorting" + result );
    }

    public void reset(){
        curr = 0;
        op.reset();
    }

    public Tuple getNextTuple(){

        if (curr == result.size()){
            return null;
        }
        curr+=1;
        return result.get(curr-1);

    }

    private class TupleComparator implements Comparator<Tuple> {

        ArrayList<Column> schema;
        public TupleComparator(ArrayList<Column> outputSchema) {
            schema = outputSchema;

        }

        @Override
        public int compare(Tuple t1, Tuple t2) {

            Logger logger = LogManager.getLogger();
            logger.info("comparing " + t1.toString() + " and " + t2.toString());

            if (!orderByElements.isEmpty()){
                Map<String, Integer> columnToIndexMap = new HashMap<>();
                for (int i = 0; i < outputSchema.size(); i += 1) {
                    columnToIndexMap.put(outputSchema.get(i).getFullyQualifiedName(), i);
                }
                for (OrderByElement orderByElement : orderByElements) {
                    Column orderToCol = (Column) orderByElement.getExpression();
                    logger.info("current col is " + orderToCol.getFullyQualifiedName());
                    String col = orderToCol.getFullyQualifiedName();
                    int t1_val = t1.getElementAtIndex(columnToIndexMap.get(col));
                    int t2_val = t2.getElementAtIndex(columnToIndexMap.get(col));

                    if (t1_val> t2_val) {
                        return 1;
                    }else if (t1_val < t2_val){
                        return -1;
                    }
                    columnToIndexMap.remove(col);


                }
                logger.info("keys are " + columnToIndexMap.keySet().toString());
                for (Column col : outputSchema) {
                    String col_str = col.getFullyQualifiedName();
                    logger.info("current key is " + col_str);
                    if (columnToIndexMap.containsKey(col_str)){
                        int t1_val = t1.getElementAtIndex(columnToIndexMap.get(col_str));
                        int t2_val = t2.getElementAtIndex(columnToIndexMap.get(col_str));
                        if (t1_val > t2_val) {
                            return 1;
                        }else if (t1_val < t2_val){
                            return -1;
                        }
                    }
                }
                return 0;
            }else{
               return t1.toString().compareTo(t2.toString());
            }

        }
    }

}

