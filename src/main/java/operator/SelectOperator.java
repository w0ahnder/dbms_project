package operator;

import common.DBCatalog;
import common.SelectVisitor;
import common.Tuple;
import java.io.*;
import java.util.ArrayList;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class SelectOperator extends Operator{
    ScanOperator scanOp;
    Expression expression;

    public SelectOperator(ArrayList<Column> outputSchema,ScanOperator sc, Expression expr){
        super(outputSchema);
        System.out.println("select expr:"+ expr.toString());
        System.out.println("output schema name:" + outputSchema);
        System.out.println("output schema name:" + outputSchema.get(0).getTable().getName());
        System.out.println("output schema ALIAS:" + outputSchema.get(0).getTable().getSchemaName());
        expression= expr;
        scanOp = sc;
    }
    public void reset(){
        scanOp.reset();
    }
    public Tuple getNextTuple(){
        //query plan builder returns an operator for each statement and then dump is called on operator
        //dump keeps calling getNextTuple until we get a null value, and operation is finished
        //have to connect table names and columns to expressions
        //keep getting tuples from scan until hit null
        //get Tuple from scan ====> check if it passes condition
        Tuple curr = scanOp.getNextTuple();
        while( curr!=null){
            SelectVisitor sv = new SelectVisitor(curr, this.outputSchema, expression);
            if(sv.evaluate_expr()){
                return curr;
            }
            curr = scanOp.getNextTuple();
        }
        scanOp.reset();
        return null;
    }
}
