package common.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import common.DBCatalog;
import common.QueryPlanBuilder;
import common.Tuple;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.Operator;
import operator.ProjectOperator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProjectOperatorTest {

  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = JoinOperatorTest.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path resourcePath = Paths.get(path);

    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples/input/custom_queries.sql")).toURI();

    Statements statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
  }

  @Test
  public void TestOneColumnProjection() throws ExecutionControl.NotImplementedException {
    ProjectOperator plan = (ProjectOperator) queryPlanBuilder.buildPlan(statementList.get(10));
    assertEquals(1, plan.getOutputSchema().size());
    Operator childOperator = plan.getChildOperator();
    Tuple childTuple = childOperator.getNextTuple();
    childOperator.reset();
    Tuple firstTuple = plan.getNextTuple();
    assertEquals(1, firstTuple.getAllElements().size());
    assertEquals(childTuple.getElementAtIndex(0), firstTuple.getElementAtIndex(0));
    assertNotEquals(childTuple, firstTuple);
  }

  @Test
  public void TestMultipleColumnProjection() throws ExecutionControl.NotImplementedException {
    ProjectOperator plan = (ProjectOperator) queryPlanBuilder.buildPlan(statementList.get(11));
    assertEquals(2, plan.getOutputSchema().size());
    Operator childOperator = plan.getChildOperator();
    Tuple childTuple = childOperator.getNextTuple();
    childOperator.reset();
    Tuple firstTuple = plan.getNextTuple();
    assertEquals(2, firstTuple.getAllElements().size());
    assertEquals(childTuple.getElementAtIndex(0), firstTuple.getElementAtIndex(0));
    assertEquals(childTuple.getElementAtIndex(3), firstTuple.getElementAtIndex(1));
    assertNotEquals(childTuple, firstTuple);
  }

  @Test
  public void TestAllColumnProjection() throws ExecutionControl.NotImplementedException {
    ProjectOperator plan = (ProjectOperator) queryPlanBuilder.buildPlan(statementList.get(12));
    assertEquals(5, plan.getOutputSchema().size());
    Operator childOperator = plan.getChildOperator();
    Tuple childTuple = childOperator.getNextTuple();
    childOperator.reset();
    Tuple firstTuple = plan.getNextTuple();
    assertEquals(5, firstTuple.getAllElements().size());
    assertEquals(childTuple.getElementAtIndex(0), firstTuple.getElementAtIndex(0));
    assertEquals(childTuple, firstTuple);
  }

  @Test
  public void TestProjectionInSwappedOrder() throws ExecutionControl.NotImplementedException {
    ProjectOperator plan = (ProjectOperator) queryPlanBuilder.buildPlan(statementList.get(13));
    assertEquals(5, plan.getOutputSchema().size());
    Operator childOperator = plan.getChildOperator();
    Tuple childTuple = childOperator.getNextTuple();
    childOperator.reset();
    Tuple firstTuple = plan.getNextTuple();
    assertEquals(childTuple.getAllElements().size(), firstTuple.getAllElements().size());
    for (int i = 0; i < childTuple.getAllElements().size(); i++) {
      assertEquals(
          childTuple.getElementAtIndex(i),
          firstTuple.getElementAtIndex(childTuple.getAllElements().size() - i - 1));
    }
    assertNotEquals(childTuple, firstTuple);
  }

  public static List<Tuple> collectAllTuples(Operator operator) {
    Tuple tuple;
    List<Tuple> tuples = new ArrayList<>();
    while ((tuple = operator.getNextTuple()) != null) {
      tuples.add(tuple);
    }
    return tuples;
  }
}
