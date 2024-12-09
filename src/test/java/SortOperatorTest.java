import common.DBCatalog;
import common.QueryPlanBuilder;
import common.Tuple;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.Operator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SortOperatorTest {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    Statements statements;
    ClassLoader classLoader = SortOperatorTest.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path resourcePath = Paths.get(path);

    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples/input/custom_queries.sql")).toURI();

    statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
  }

  @Test
  public void assertNullGetNextTuple() throws ExecutionControl.NotImplementedException {
    Operator plan = queryPlanBuilder.buildPlan(statementList.get(7));

    List<Tuple> tuples = HelperMethods.collectAllTuples(plan);

    int expectedSize = 0;
    Assertions.assertEquals(expectedSize, tuples.size(), "Unexpected number of rows.");

    Assertions.assertNull(plan.getNextTuple());
  }
}
