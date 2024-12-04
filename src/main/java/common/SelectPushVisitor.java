package common;

import UnionFind.UnionFind;
import java.util.Objects;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;

public class SelectPushVisitor implements ExpressionVisitor {

  UnionFind unionFind;

  Expression expression;

  public SelectPushVisitor(Expression expr) {
    this.unionFind = new UnionFind();
    this.expression = expr;
  }

  public void evaluate_expr() {
    expression.accept(this);
  }

  @Override
  public void visit(AndExpression andExpression) {
    andExpression.getLeftExpression().accept(this);
    andExpression.getRightExpression().accept(this);
  }

  @Override
  public void visit(EqualsTo equalsTo) {
    String left_str = equalsTo.getLeftExpression().toString();
    String right_str = equalsTo.getRightExpression().toString();

    try {
      Integer eq = Integer.parseInt(right_str);
      unionFind.setEquality(left_str, eq);

    } catch (NumberFormatException e) {
      unionFind.mergeElements(left_str, right_str);
      if (Objects.equals(left_str.split("\\.")[0], right_str.split("\\.")[0])) {
        unionFind.sameTableSelect.add(equalsTo);
      } else {
        unionFind.joins.add(equalsTo);
      }
    }
  }

  @Override
  public void visit(GreaterThan greaterThan) {
    String left_str = greaterThan.getLeftExpression().toString();
    String right_str = greaterThan.getRightExpression().toString();
    try {
      Integer lower = Integer.parseInt(right_str) + 1;
      unionFind.setLower(left_str, lower);

    } catch (NumberFormatException e) {
      if (Objects.equals(left_str.split("\\.")[0], right_str.split("\\.")[0])) {
        unionFind.sameTableSelect.add(greaterThan);
      } else {
        unionFind.joins.add(greaterThan);
      }
    }
  }

  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
    String left_str = greaterThanEquals.getLeftExpression().toString();
    String right_str = greaterThanEquals.getRightExpression().toString();
    try {
      Integer lower = Integer.parseInt(right_str);
      unionFind.setLower(left_str, lower);

    } catch (NumberFormatException e) {
      if (Objects.equals(left_str.split("\\.")[0], right_str.split("\\.")[0])) {
        unionFind.sameTableSelect.add(greaterThanEquals);
      } else {
        unionFind.joins.add(greaterThanEquals);
      }
    }
  }

  @Override
  public void visit(MinorThan minorThan) {
    String left_str = minorThan.getLeftExpression().toString();
    String right_str = minorThan.getRightExpression().toString();
    try {
      Integer upper = Integer.parseInt(right_str) - 1;
      unionFind.setUpper(left_str, upper);

    } catch (NumberFormatException e) {
      if (Objects.equals(left_str.split("\\.")[0], right_str.split("\\.")[0])) {
        unionFind.sameTableSelect.add(minorThan);
      } else {
        unionFind.joins.add(minorThan);
      }
    }
  }

  @Override
  public void visit(MinorThanEquals minorThanEquals) {
    String left_str = minorThanEquals.getLeftExpression().toString();
    String right_str = minorThanEquals.getRightExpression().toString();
    try {
      Integer upper = Integer.parseInt(right_str);
      unionFind.setUpper(left_str, upper);

    } catch (NumberFormatException e) {
      if (Objects.equals(left_str.split("\\.")[0], right_str.split("\\.")[0])) {
        unionFind.sameTableSelect.add(minorThanEquals);
      } else {
        unionFind.joins.add(minorThanEquals);
      }
    }
  }

  @Override
  public void visit(NotEqualsTo notEqualsTo) {
    String right_str = notEqualsTo.getRightExpression().toString();
    String left_str = notEqualsTo.getLeftExpression().toString();
    try {
      Integer.parseInt(right_str);
      unionFind.sameTableSelect.add(notEqualsTo);

    } catch (NumberFormatException e) {
      if (Objects.equals(left_str.split("\\.")[0], right_str.split("\\.")[0])) {
        unionFind.sameTableSelect.add(notEqualsTo);
      } else {
        unionFind.joins.add(notEqualsTo);
      }
    }
  }

  @Override
  public void visit(Column column) {}

  @Override
  public void visit(BitwiseRightShift bitwiseRightShift) {}

  @Override
  public void visit(BitwiseLeftShift bitwiseLeftShift) {}

  @Override
  public void visit(NullValue nullValue) {}

  @Override
  public void visit(Function function) {}

  @Override
  public void visit(SignedExpression signedExpression) {}

  @Override
  public void visit(JdbcParameter jdbcParameter) {}

  @Override
  public void visit(JdbcNamedParameter jdbcNamedParameter) {}

  @Override
  public void visit(DoubleValue doubleValue) {}

  @Override
  public void visit(LongValue longValue) {}

  @Override
  public void visit(HexValue hexValue) {}

  @Override
  public void visit(DateValue dateValue) {}

  @Override
  public void visit(TimeValue timeValue) {}

  @Override
  public void visit(TimestampValue timestampValue) {}

  @Override
  public void visit(Parenthesis parenthesis) {}

  @Override
  public void visit(StringValue stringValue) {}

  @Override
  public void visit(Addition addition) {}

  @Override
  public void visit(Division division) {}

  @Override
  public void visit(IntegerDivision integerDivision) {}

  @Override
  public void visit(Multiplication multiplication) {}

  @Override
  public void visit(Subtraction subtraction) {}

  @Override
  public void visit(OrExpression orExpression) {}

  @Override
  public void visit(XorExpression xorExpression) {}

  @Override
  public void visit(Between between) {}

  @Override
  public void visit(OverlapsCondition overlapsCondition) {}

  @Override
  public void visit(InExpression inExpression) {}

  @Override
  public void visit(FullTextSearch fullTextSearch) {}

  @Override
  public void visit(IsNullExpression isNullExpression) {}

  @Override
  public void visit(IsBooleanExpression isBooleanExpression) {}

  @Override
  public void visit(LikeExpression likeExpression) {}

  @Override
  public void visit(SubSelect subSelect) {}

  @Override
  public void visit(CaseExpression caseExpression) {}

  @Override
  public void visit(WhenClause whenClause) {}

  @Override
  public void visit(ExistsExpression existsExpression) {}

  @Override
  public void visit(AnyComparisonExpression anyComparisonExpression) {}

  @Override
  public void visit(Concat concat) {}

  @Override
  public void visit(Matches matches) {}

  @Override
  public void visit(BitwiseAnd bitwiseAnd) {}

  @Override
  public void visit(BitwiseOr bitwiseOr) {}

  @Override
  public void visit(BitwiseXor bitwiseXor) {}

  @Override
  public void visit(CastExpression castExpression) {}

  @Override
  public void visit(TryCastExpression tryCastExpression) {}

  @Override
  public void visit(SafeCastExpression safeCastExpression) {}

  @Override
  public void visit(Modulo modulo) {}

  @Override
  public void visit(AnalyticExpression analyticExpression) {}

  @Override
  public void visit(ExtractExpression extractExpression) {}

  @Override
  public void visit(IntervalExpression intervalExpression) {}

  @Override
  public void visit(OracleHierarchicalExpression oracleHierarchicalExpression) {}

  @Override
  public void visit(RegExpMatchOperator regExpMatchOperator) {}

  @Override
  public void visit(JsonExpression jsonExpression) {}

  @Override
  public void visit(JsonOperator jsonOperator) {}

  @Override
  public void visit(RegExpMySQLOperator regExpMySQLOperator) {}

  @Override
  public void visit(UserVariable userVariable) {}

  @Override
  public void visit(NumericBind numericBind) {}

  @Override
  public void visit(KeepExpression keepExpression) {}

  @Override
  public void visit(MySQLGroupConcat mySQLGroupConcat) {}

  @Override
  public void visit(ValueListExpression valueListExpression) {}

  @Override
  public void visit(RowConstructor rowConstructor) {}

  @Override
  public void visit(RowGetExpression rowGetExpression) {}

  @Override
  public void visit(OracleHint oracleHint) {}

  @Override
  public void visit(TimeKeyExpression timeKeyExpression) {}

  @Override
  public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {}

  @Override
  public void visit(NotExpression notExpression) {}

  @Override
  public void visit(NextValExpression nextValExpression) {}

  @Override
  public void visit(CollateExpression collateExpression) {}

  @Override
  public void visit(SimilarToExpression similarToExpression) {}

  @Override
  public void visit(ArrayExpression arrayExpression) {}

  @Override
  public void visit(ArrayConstructor arrayConstructor) {}

  @Override
  public void visit(VariableAssignment variableAssignment) {}

  @Override
  public void visit(XMLSerializeExpr xmlSerializeExpr) {}

  @Override
  public void visit(TimezoneExpression timezoneExpression) {}

  @Override
  public void visit(JsonAggregateFunction jsonAggregateFunction) {}

  @Override
  public void visit(JsonFunction jsonFunction) {}

  @Override
  public void visit(ConnectByRootOperator connectByRootOperator) {}

  @Override
  public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {}

  @Override
  public void visit(AllColumns allColumns) {}

  @Override
  public void visit(AllTableColumns allTableColumns) {}

  @Override
  public void visit(AllValue allValue) {}

  @Override
  public void visit(IsDistinctExpression isDistinctExpression) {}

  @Override
  public void visit(GeometryDistance geometryDistance) {}
}
