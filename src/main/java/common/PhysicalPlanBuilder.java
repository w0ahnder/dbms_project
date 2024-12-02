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
    if (selectLogOperator.where != null) {// the table has no index, so just use regular scan, this is okay
      rootOperator =
          new SelectOperator(
              rootOperator.getOutputSchema(), (ScanOperator) rootOperator, selectLogOperator.where);
    }
    else if (selectLogOperator.indexedExpr == null) {//only have unindexed expressions, should also be okay
      rootOperator =
          new SelectOperator(
              rootOperator.getOutputSchema(),
              (ScanOperator) rootOperator,
              selectLogOperator.unIndexedExpr);
    }
    //if we reach this point, then we know that there are indexes and that we have expressions we can use them on
    //so have to check which is more optimal, a full scan or index scan
    else if (selectLogOperator.unIndexedExpr == null) {//only have expressions that can be evaluated with an index
      rootOperator =
          new IndexScanOperator(
              selectLogOperator.outputSchema,
              selectLogOperator.table_path,
              selectLogOperator.table_name,
              selectLogOperator.index_file,
              selectLogOperator.ind,
              selectLogOperator.lowKey,
              selectLogOperator.highKey,
              selectLogOperator.clustered);
    } else { //some expressions have indexes, the other's don't
      System.out.println("unindexed and indexed expressions");
      System.out.println("unindexed: " + selectLogOperator.unIndexedExpr);
      System.out.println("indexed low, high: " +
              selectLogOperator.lowKey+", " + selectLogOperator.highKey);
      //TODO: change lowkey from Integer.MIN_VALUE to the minimum value of that column
      ScanOperator childOperator =
          new IndexScanOperator(
              selectLogOperator.outputSchema,
              selectLogOperator.table_path,
              selectLogOperator.table_name,
              selectLogOperator.index_file,
              selectLogOperator.ind,
              selectLogOperator.lowKey,
              selectLogOperator.highKey,
              selectLogOperator.clustered);
      rootOperator =
          new SelectOperator(
              rootOperator.getOutputSchema(), childOperator, selectLogOperator.unIndexedExpr);
    }
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
}
