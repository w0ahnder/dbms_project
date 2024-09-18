package operator;

import common.Tuple;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectOperator extends Operator {
  Operator childOperator;
  List<SelectItem> selectItems;

  public ProjectOperator(
      ArrayList<Column> outputSchema, Operator childOperator, List<SelectItem> selectItems)
      throws FileNotFoundException {
    super(outputSchema);
    this.childOperator = childOperator;
    this.selectItems = selectItems;
  }

  public void reset() {
    childOperator.reset();
  }

  public Tuple getNextTuple() {
    Tuple nextTuple = childOperator.getNextTuple();
    if (nextTuple == null) {
      reset();
      return nextTuple;
    }
    Set<String> selectedColumnNames =
        selectItems.stream()
            .filter(item -> item instanceof SelectExpressionItem)
            .map(item -> (Column) ((SelectExpressionItem) item).getExpression())
            .map(Column::getColumnName)
            .collect(Collectors.toSet());

    Map<String, Integer> schemaIndexMap = new HashMap<>();
    for (int i = 0; i < this.outputSchema.size(); i++) {
      schemaIndexMap.put(this.outputSchema.get(i).getColumnName(), i);
    }

    // Create the result tuple based on selected columns
    ArrayList<Integer> returnTup =
        selectedColumnNames.stream()
            .map(columnName -> schemaIndexMap.getOrDefault(columnName, -1))
            .filter(index -> index != -1)
            .map(nextTuple::getElementAtIndex)
            .collect(Collectors.toCollection(ArrayList::new));
    return new Tuple(returnTup);
  }
}
