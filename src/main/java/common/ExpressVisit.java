package common;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OverlapsCondition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;
import java.lang.Long;

public abstract class ExpressVisit implements ExpressionVisitor {
    protected long longValue=0;
    private boolean cond;

    public boolean return_cond(){
        return cond;
    }
    public void visit(AndExpression andexpr){
        //Sailors.A = Reserves.G AND SAILORS.A<500 AND Sailors.B>300 AND Sailors.B>200
        //left expression is Sailors.A = Reserves.G AND SAILORS.A<500 AND Sailors.B>300
        //right expression is Sailors.B>200

        andexpr.getLeftExpression().accept(this);
        boolean left = cond;
        andexpr.getRightExpression().accept(this);
        boolean right = cond;
        cond = left && right;
    }
    public void visit(Column column){}
    public void visit(EqualsTo equalsto){
        System.out.println("equalsto:"+ equalsto.toString());
        equalsto.getLeftExpression().accept(this);
        long left = longValue;
        equalsto.getRightExpression().accept(this);
        long right = longValue;
        cond =  left==right;
    }

    public void visit(LongValue longval){
        longValue = longval.getValue();
    }
    public void visit(NotEqualsTo notequalsto){
        notequalsto.getLeftExpression().accept(this);
        long left = longValue;
        notequalsto.getRightExpression().accept(this);
        long right = longValue;
        cond =  left != right;
    }
    public void visit(GreaterThan greaterThan){
        long left = 0;
        long right = 0;
        greaterThan.getLeftExpression().accept(this);
        left = longValue;
        greaterThan.getRightExpression().accept(this);
        right = longValue;
        cond = left > right;
    }
    public void visit(GreaterThanEquals greaterEq){
        long left = 0;
        long right = 0;
        greaterEq.getLeftExpression().accept(this);
        left = longValue;
        greaterEq.getRightExpression().accept(this);
        right = longValue;
        cond = left >= right;
    }
    public void visit(MinorThan minorThan){

        minorThan.getLeftExpression().accept(this);
        long left = longValue;
        minorThan.getRightExpression().accept(this);
        long right = longValue;
        cond =  left<right;
    }
    public void visit(MinorThanEquals minorThanEq){
        minorThanEq.getLeftExpression().accept(this);
        long left = longValue;
        minorThanEq.getRightExpression().accept(this);
        long right = longValue;
        cond = left <= right;
    }

    //extra
    public void visit(InExpression inExpression) {
    }
    public void visit(FullTextSearch fullTextSearch) {
    }
    public void visit(IsNullExpression isNullExpression) {
    }
    public void visit(IsBooleanExpression isBooleanExpression) {
    }
    public void visit(LikeExpression likeExpression) {
    }
    public void visit(OrExpression orExpression) {
    }
    public void visit(XorExpression orExpression) {
    }
    public void visit(Between between) {
    }
    public void visit(OverlapsCondition overlapsCondition) {
    }
    public void visit(BitwiseRightShift aThis) {
    }
    public void visit(BitwiseLeftShift aThis) {
    }
    public void visit(NullValue nullValue) {
    }
    public void visit(Function function) {
    }
    public void visit(SignedExpression signedExpression) {
    }
    public void visit(JdbcParameter jdbcParameter) {
    }
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
    }
    public void visit(DoubleValue doubleValue) {
    }
    public void visit(HexValue hexValue) {
    }
    public void visit(DateValue dateValue) {
    }
    public void visit(TimeValue timeValue) {
    }
    public void visit(TimestampValue timestampValue) {
    }
    public void visit(Parenthesis parenthesis) {
    }
    public void visit(StringValue stringValue) {
    }
    public void visit(Addition addition) {
    }
    public void visit(Division division) {
    }
    public void visit(IntegerDivision division) {
    }
    public void visit(Multiplication multiplication) {
    }
    public void visit(Subtraction subtraction) {
    }
    public void visit(SubSelect subSelect) {
    }
    public void visit(CaseExpression caseExpression) {
    }
    public void visit(WhenClause whenClause) {
    }
    public void visit(ExistsExpression existsExpression) {
    }
    public void visit(AnyComparisonExpression anyComparisonExpression) {
    }
    public void visit(Concat concat) {
    }
    public void visit(Matches matches) {
    }
    public void visit(BitwiseAnd bitwiseAnd) {
    }
    public void visit(BitwiseOr bitwiseOr) {
    }
    public void visit(BitwiseXor bitwiseXor) {
    }
    public void visit(CastExpression cast) {
    }
    public void visit(TryCastExpression cast) {
    }
    public void visit(SafeCastExpression cast) {
    }
    public void visit(Modulo modulo) {
    }
    public void visit(AnalyticExpression aexpr) {
    }
    public void visit(ExtractExpression eexpr) {
    }
    public void visit(IntervalExpression iexpr) {
    }
    public void visit(OracleHierarchicalExpression oexpr) {
    }
    public void visit(RegExpMatchOperator rexpr) {
    }
    public void visit(JsonExpression jsonExpr) {
    }
    public void visit(JsonOperator jsonExpr) {
    }
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
    }
    public void visit(UserVariable var) {
    }
    public void visit(NumericBind bind) {
    }
    public void visit(KeepExpression aexpr) {
    }
    public void visit(MySQLGroupConcat groupConcat) {
    }
    public void visit(ValueListExpression valueList) {
    }
    public void visit(RowConstructor rowConstructor) {
    }
    public void visit(RowGetExpression rowGetExpression) {
    }
    public void visit(OracleHint hint) {
    }
    public void visit(TimeKeyExpression timeKeyExpression) {
    }
    public void visit(DateTimeLiteralExpression literal) {
    }
    public void visit(NotExpression aThis) {
    }
    public void visit(NextValExpression aThis) {
    }
    public void visit(CollateExpression aThis) {
    }
    public void visit(SimilarToExpression aThis) {
    }
    public void visit(ArrayExpression aThis) {
    }
    public void visit(ArrayConstructor aThis) {
    }
    public void visit(VariableAssignment aThis) {
    }
    public void visit(XMLSerializeExpr aThis) {
    }
    public void visit(TimezoneExpression aThis) {
    }
    public void visit(JsonAggregateFunction aThis) {
    }
    public void visit(JsonFunction aThis) {
    }
    public void visit(ConnectByRootOperator aThis) {
    }
    public void visit(OracleNamedFunctionParameter aThis) {
    }
    public void visit(AllColumns allColumns) {
    }
    public void visit(AllTableColumns allTableColumns) {
    }
    public void visit(AllValue allValue) {
    }
    public void visit(IsDistinctExpression isDistinctExpression) {
    }
    public void visit(GeometryDistance geometryDistance) {

    }




}
