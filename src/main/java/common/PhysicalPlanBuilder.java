package common;

import java.io.FileNotFoundException;
import java.util.List;
import net.sf.jsqlparser.statement.select.OrderByElement;
import operator.LogicalOperators.DuplicateEliminationLogOperator;
import operator.LogicalOperators.JoinLogOperator;
import operator.LogicalOperators.ProjectLogOperator;
import operator.LogicalOperators.ScanLogOperator;
import operator.LogicalOperators.SelectLogOperator;
import operator.LogicalOperators.SortLogOperator;
import operator.PhysicalOperators.*;
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
    rootOperator =
        new SelectOperator(
            rootOperator.getOutputSchema(), (ScanOperator) rootOperator, selectLogOperator.where);
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
      ColumnProcessor cp = new ColumnProcessor();
      List<OrderByElement> leftCond = cp.getOrderByElements(child[0], joinLogOperator.condition);
      System.out.println("leftorder by" + leftCond);
      List<OrderByElement> rightCond = cp.getOrderByElements(child[1], joinLogOperator.condition);
      System.out.println("right orderby:" + rightCond);
      //  TODO: change buffer pages to accurate one

      SortOperator left =
          new ExternalSortOperator(
              child[0].getOutputSchema(), leftCond, child[0], 3, joinLogOperator.tempDir);
      System.out.println("child zero :" + child[0].getOutputSchema());

      System.out.println("child one :" + child[1].getOutputSchema());
      SortOperator right =
          new ExternalSortOperator(
              child[1].getOutputSchema(), rightCond, child[1], 3, joinLogOperator.tempDir);
      rootOperator =
          new SortMergeJoinOperator(
              joinLogOperator.outputSchema,
              left,
              right,
              joinLogOperator.tempDir,
              leftCond,
              rightCond);
    }
  }

  public void visit(DuplicateEliminationLogOperator duplicateEliminationLogOperator)
      throws FileNotFoundException {
    duplicateEliminationLogOperator.sort.accept(this);
    rootOperator =
        new DuplicateEliminationOperator(
            duplicateEliminationLogOperator.outputSchema, (SortOperator) rootOperator);
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
}
