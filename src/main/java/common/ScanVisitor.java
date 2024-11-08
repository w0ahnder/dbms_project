package common;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.OverlapsCondition;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
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

public class ScanVisitor implements ExpressionVisitor {
  Expression expression;
  private boolean cond;
  private String col;
  protected Integer intValue = 0;
  // swap them later?
  private Integer highKey = Integer.MAX_VALUE;
  private Integer lowKey = Integer.MIN_VALUE;
  private boolean fixed = false;

  public ScanVisitor(Expression expr, String column) {
    expression = expr;
    col = column;
    // TODO: take in the column name that we can process for
  }

  /**
   * evaluates expression and returns result
   *
   * @return boolean value of the expression evaluated
   */
  public boolean evaluate_expr() {
    expression.accept(this);
    return cond;
  }

  public Integer getHighKey() {
    return highKey;
  }

  public Integer getLowKey() {
    return lowKey;
  }

  public void visit(AndExpression andexpr) {
    andexpr.getLeftExpression().accept(this);
    andexpr.getRightExpression().accept(this);
  }

  public void visit(Column column) {
    intValue = null;
    cond = column.toString().equalsIgnoreCase(col);
  }

  public void visit(EqualsTo equalsto) {
    equalsto.getLeftExpression().accept(this);
    boolean left = cond;
    Integer leftVal = intValue;
    equalsto.getRightExpression().accept(this);
    boolean right = cond;
    Integer rightVal = intValue;
    cond = left && right;
    if (cond && !fixed) {
      fixed = true;
      if (leftVal != null) {
        lowKey = leftVal;
        highKey = leftVal;
      } else if (rightVal != null) {
        lowKey = rightVal;
        highKey = rightVal;
      }
    }
  }

  public void visit(LongValue longval) {
    intValue = (int) longval.getValue();
    cond = true;
  }

  public void visit(NotEqualsTo notequalsto) {
    cond = false;
  }

  public void visit(GreaterThan greaterThan) {
    greaterThan.getLeftExpression().accept(this);
    boolean left = cond;
    Integer leftVal = intValue;
    greaterThan.getRightExpression().accept(this);
    boolean right = cond;
    Integer rightVal = intValue;
    cond = left && right;
    if (cond && !fixed) {
      if (leftVal != null) {
        highKey = Integer.min(leftVal - 1, highKey);
      } else if (rightVal != null) {
        lowKey = Integer.max(rightVal + 1, lowKey);
      }
    }
  }

  public void visit(GreaterThanEquals greaterEq) {
    greaterEq.getLeftExpression().accept(this);
    boolean left = cond;
    Integer leftVal = intValue;
    greaterEq.getRightExpression().accept(this);
    boolean right = cond;
    Integer rightVal = intValue;
    cond = left && right;
    if (cond && !fixed) {
      if (leftVal != null) {
        highKey = Integer.min(leftVal, highKey);
      } else if (rightVal != null) {
        lowKey = Integer.max(rightVal, lowKey);
      }
    }
  }

  public void visit(MinorThan minorThan) {
    minorThan.getLeftExpression().accept(this);
    boolean left = cond;
    Integer leftVal = intValue;
    minorThan.getRightExpression().accept(this);
    boolean right = cond;
    Integer rightVal = intValue;
    cond = left && right;
    if (cond && !fixed) {
      if (leftVal != null) {
        lowKey = Integer.max(leftVal + 1, lowKey);
      } else if (rightVal != null) {
        highKey = Integer.min(rightVal - 1, highKey);
        System.out.println(highKey);
      }
    }
  }

  public void visit(MinorThanEquals minorThanEq) {
    minorThanEq.getLeftExpression().accept(this);
    boolean left = cond;
    Integer leftVal = intValue;
    minorThanEq.getRightExpression().accept(this);
    boolean right = cond;
    Integer rightVal = intValue;
    cond = left && right;
    if (cond && !fixed) {
      if (leftVal != null) {
        lowKey = Integer.max(leftVal, lowKey);
      } else if (rightVal != null) {
        highKey = Integer.min(rightVal, highKey);
      }
    }
  }

  // extra
  public void visit(InExpression inExpression) {}

  public void visit(FullTextSearch fullTextSearch) {}

  public void visit(IsNullExpression isNullExpression) {}

  public void visit(IsBooleanExpression isBooleanExpression) {}

  public void visit(LikeExpression likeExpression) {}

  public void visit(OrExpression orExpression) {}

  public void visit(XorExpression orExpression) {}

  public void visit(Between between) {}

  public void visit(OverlapsCondition overlapsCondition) {}

  public void visit(BitwiseRightShift aThis) {}

  public void visit(BitwiseLeftShift aThis) {}

  public void visit(NullValue nullValue) {}

  public void visit(Function function) {}

  public void visit(SignedExpression signedExpression) {}

  public void visit(JdbcParameter jdbcParameter) {}

  public void visit(JdbcNamedParameter jdbcNamedParameter) {}

  public void visit(DoubleValue doubleValue) {}

  public void visit(HexValue hexValue) {}

  public void visit(DateValue dateValue) {}

  public void visit(TimeValue timeValue) {}

  public void visit(TimestampValue timestampValue) {}

  public void visit(Parenthesis parenthesis) {}

  public void visit(StringValue stringValue) {}

  public void visit(Addition addition) {}

  public void visit(Division division) {}

  public void visit(IntegerDivision division) {}

  public void visit(Multiplication multiplication) {}

  public void visit(Subtraction subtraction) {}

  public void visit(SubSelect subSelect) {}

  public void visit(CaseExpression caseExpression) {}

  public void visit(WhenClause whenClause) {}

  public void visit(ExistsExpression existsExpression) {}

  public void visit(AnyComparisonExpression anyComparisonExpression) {}

  public void visit(Concat concat) {}

  public void visit(Matches matches) {}

  public void visit(BitwiseAnd bitwiseAnd) {}

  public void visit(BitwiseOr bitwiseOr) {}

  public void visit(BitwiseXor bitwiseXor) {}

  public void visit(CastExpression cast) {}

  public void visit(TryCastExpression cast) {}

  public void visit(SafeCastExpression cast) {}

  public void visit(Modulo modulo) {}

  public void visit(AnalyticExpression aexpr) {}

  public void visit(ExtractExpression eexpr) {}

  public void visit(IntervalExpression iexpr) {}

  public void visit(OracleHierarchicalExpression oexpr) {}

  public void visit(RegExpMatchOperator rexpr) {}

  public void visit(JsonExpression jsonExpr) {}

  public void visit(JsonOperator jsonExpr) {}

  public void visit(RegExpMySQLOperator regExpMySQLOperator) {}

  public void visit(UserVariable var) {}

  public void visit(NumericBind bind) {}

  public void visit(KeepExpression aexpr) {}

  public void visit(MySQLGroupConcat groupConcat) {}

  public void visit(ValueListExpression valueList) {}

  public void visit(RowConstructor rowConstructor) {}

  public void visit(RowGetExpression rowGetExpression) {}

  public void visit(OracleHint hint) {}

  public void visit(TimeKeyExpression timeKeyExpression) {}

  public void visit(DateTimeLiteralExpression literal) {}

  public void visit(NotExpression aThis) {}

  public void visit(NextValExpression aThis) {}

  public void visit(CollateExpression aThis) {}

  public void visit(SimilarToExpression aThis) {}

  public void visit(ArrayExpression aThis) {}

  public void visit(ArrayConstructor aThis) {}

  public void visit(VariableAssignment aThis) {}

  public void visit(XMLSerializeExpr aThis) {}

  public void visit(TimezoneExpression aThis) {}

  public void visit(JsonAggregateFunction aThis) {}

  public void visit(JsonFunction aThis) {}

  public void visit(ConnectByRootOperator aThis) {}

  public void visit(OracleNamedFunctionParameter aThis) {}

  public void visit(AllColumns allColumns) {}

  public void visit(AllTableColumns allTableColumns) {}

  public void visit(AllValue allValue) {}

  public void visit(IsDistinctExpression isDistinctExpression) {}

  public void visit(GeometryDistance geometryDistance) {}
}
