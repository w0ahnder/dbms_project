package common;

import java.io.FileNotFoundException;
import java.util.List;
import net.sf.jsqlparser.statement.select.OrderByElement;
import operator.LogicalOperators.DuplicateEliminationLogOperator;
import operator.LogicalOperators.JoinLogOperator;
import operator.LogicalOperators.NewJoinLogOperator;
import operator.LogicalOperators.ProjectLogOperator;
import operator.LogicalOperators.ScanLogOperator;
import operator.LogicalOperators.SelectLogOperator;
import operator.LogicalOperators.SortLogOperator;
import operator.PhysicalOperators.*;
import optimal.*;
import utilities.ColumnProcessor;

public class PhysicalPlanBuilder {

  private Operator rootOperator;

  public Operator returnResultTuple() {
    return rootOperator;
  }

  public void visit(ScanLogOperator scanLogOperator) throws FileNotFoundException {
    rootOperator = new ScanOperator(scanLogOperator.outputSchema, scanLogOperator.path);
  }

  public void visit(SelectLogOperator selectLogOperator) throws FileNotFoundException {
    selectLogOperator.scan.accept(this);
    // String table = selectLogOperator.scan.
    SelectPlan selectPlan =
        new SelectPlan(
            selectLogOperator.table_name,
            selectLogOperator.outputSchema,
            selectLogOperator.table_path,
            selectLogOperator.scan);
    selectPlan.plan(selectLogOperator.where);
    rootOperator = selectPlan.optimalPlan();
  }

  public void visit(JoinLogOperator joinLogOperator) throws FileNotFoundException {
    // System.out.println("Visiting");
    Operator[] child = new Operator[2];
    joinLogOperator.leftOperator.accept(this);
    child[0] = rootOperator;
    joinLogOperator.rightOperator.accept(this);
    child[1] = rootOperator;

    if (DBCatalog.getInstance().if_TNLJ()) {
      rootOperator =
          new JoinOperator(
              joinLogOperator.outputSchema, child[0], child[1], joinLogOperator.condition);
    } else if (DBCatalog.getInstance().if_BNLJ()) {
      rootOperator =
          new BNLOperator(
              joinLogOperator.outputSchema, child[0], child[1], joinLogOperator.condition);
    } else if (DBCatalog.getInstance().if_SMJ()) {
      if (joinLogOperator.condition == null) {
        rootOperator =
            new JoinOperator(
                joinLogOperator.outputSchema, child[0], child[1], joinLogOperator.condition);
      } else {
        ColumnProcessor cp = new ColumnProcessor();
        List<OrderByElement> leftCond = cp.getOrderByElements(child[0], joinLogOperator.condition);

        List<OrderByElement> rightCond = cp.getOrderByElements(child[1], joinLogOperator.condition);
        //  TODO: change buffer pages to accurate one
        SortOperator left =
            new ExternalSortOperator(
                child[0].getOutputSchema(),
                leftCond,
                child[0],
                DBCatalog.getInstance().getSortBuff(),
                joinLogOperator.tempDir);
        SortOperator right =
            new ExternalSortOperator(
                child[1].getOutputSchema(),
                rightCond,
                child[1],
                DBCatalog.getInstance().getSortBuff(),
                joinLogOperator.tempDir);
        rootOperator =
            new SortMergeJoinOperator(
                joinLogOperator.outputSchema, left, right, leftCond, rightCond);
      }
    }
  }

  public void visit(DuplicateEliminationLogOperator duplicateEliminationLogOperator)
      throws FileNotFoundException {
    duplicateEliminationLogOperator.sort.accept(this);
    rootOperator =
        new DuplicateEliminationOperator(
            duplicateEliminationLogOperator.outputSchema, rootOperator);
  }

  public void visit(ProjectLogOperator projectLogOperator) throws FileNotFoundException {
    projectLogOperator.child.accept(this);
    rootOperator =
        new ProjectOperator(
            projectLogOperator.outputSchema,
            rootOperator.getOutputSchema(),
            rootOperator,
            projectLogOperator.selectItems);
  }

  public void visit(SortLogOperator sortLogOperator) throws FileNotFoundException {
    sortLogOperator.child.accept(this);
    if (sortLogOperator.bufferPages == null) {
      rootOperator =
          new InMemorySortOperator(
              rootOperator.getOutputSchema(), sortLogOperator.orderByElements, rootOperator);
    } else {
      rootOperator =
          new ExternalSortOperator(
              rootOperator.getOutputSchema(),
              sortLogOperator.orderByElements,
              rootOperator,
              sortLogOperator.bufferPages,
              sortLogOperator.tempDir);
    }
  }

  public void visit(NewJoinLogOperator NewJoinLogOperator) throws FileNotFoundException {
    TableSizeCalculator calc =
        new TableSizeCalculator(
            NewJoinLogOperator.tableToOp,
            NewJoinLogOperator.tables,
            NewJoinLogOperator.selectExpressions,
            NewJoinLogOperator.joinExpressions,
            NewJoinLogOperator.colMinMax);
    for (String table : NewJoinLogOperator.tables) {
      calc.getTableSize(null, table);
    }
    CostCalculator calcu = new CostCalculator(calc);
    List<String> bestOrder = calcu.findOptimalJoinOrder(NewJoinLogOperator.tables);
    System.out.println("best join order: " + bestOrder);
    JoinPlanBuilder plan =
        new JoinPlanBuilder(
            NewJoinLogOperator.tableToOp,
            bestOrder,
            NewJoinLogOperator,
            NewJoinLogOperator.outputSchema);
    rootOperator = plan.buildPlan();
  }

  public Operator getRootOp() {
    return rootOperator;
  }
}
