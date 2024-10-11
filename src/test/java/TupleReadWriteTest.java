import common.*;
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
import operator.PhysicalOperators.Operator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TupleReadWriteTest {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = TupleReadWriteTest.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("binary_samples/input")).toURI();
    Path resourcePath = Paths.get(path);

    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("binary_samples/input/test_queries.sql"))
            .toURI();

    statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
  }

  @Test
  public void testBoat() throws ExecutionControl.NotImplementedException, IOException {
    Operator plan = queryPlanBuilder.buildPlan(statementList.get(0));
    TupleReader tr = new TupleReader(DBCatalog.getInstance().getFileForTable("Boats"));
    TupleWriter tw = new TupleWriter("src/test/resources/binary_samples/BoatRead");
    Tuple t = tr.read();
    while (t != null) {
      tw.write(t);
      t = tr.read();
    }
    tw.close();
  }

  @Test
  public void testSailors() throws ExecutionControl.NotImplementedException, IOException {
    Operator plan = queryPlanBuilder.buildPlan(statementList.get(1));
    TupleReader tr = new TupleReader(DBCatalog.getInstance().getFileForTable("Sailors"));
    TupleWriter tw = new TupleWriter("src/test/resources/binary_samples/SailorsRead");
    Tuple t = tr.read();
    while (t != null) {
      tw.write(t);
      t = tr.read();
    }
    tw.close();
  }
}
