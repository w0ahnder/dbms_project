package common;

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

public abstract class ExpressVisit implements ExpressionVisitor {

    public void visit(AndExpression andExpr){
    }
    public void visit(LongValue longVal){}
    public void visit(Column column){}
    public void visit(EqualsTo equalsto){}
    public void visit(NotEqualsTo notequalsto){}
    public void visit(GreaterThan greaterThan){}
    public void visit(GreaterThanEquals greaterEq){}
    public void visit(MinorThan minorThan){}
    public void visit(MinorThanEquals minorThanEq){}



}
