package common;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.schema.Column;
import operator.SelectOperator;

import java.util.ArrayList;

public class SelectVisitor extends ExpressVisit {
    Expression expression;
    ArrayList<Column> schema;
    ArrayList<Expression> andExpressions;
    Tuple tuple;
    public SelectVisitor(SelectOperator selectOp, Tuple t, ArrayList<Column> tableschema, Expression expr) {
        expression = expr;
        schema = tableschema;
        tuple = t;
    }

    public boolean evaluate_expr() {
        expression.accept(this);
        return return_cond();
    }
    public ArrayList<Expression> getAnds(Expression where){
        ArrayList<Expression> ands = new ArrayList<>();
        if(where!=null){
            while(where instanceof AndExpression){
                ands.add(((AndExpression) where).getRightExpression());
                where = ((AndExpression) where).getLeftExpression();
            }
            ands.add(where);
        }
        return ands;
    }

    public void visit(Column column){
        longvalue = longvalue;
        String[] data = (column.toString()).split("\\.");
        String table = data[0];
        String col = data[1];
        int count=0;
        for(Column c: schema){
            String colname = c.getColumnName();
            String tablename = c.getTable().getName();
            if(colname.equals(col) && tablename.equals(data[0])){
                longvalue = tuple.getElementAtIndex(count);
                return;
            }
            count++;
        }

    }


}





