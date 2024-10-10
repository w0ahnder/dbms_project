import common.DBCatalog;
import common.QueryPlanBuilder;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
import org.apache.logging.log4j.core.util.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.PrintStream;

public class P1Debug {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;

    @BeforeAll
    static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
        ClassLoader classLoader = P1UnitTests.class.getClassLoader();
        URI path = Objects.requireNonNull(classLoader.getResource("binary_samples/input")).toURI();
        Path resourcePath = Paths.get(path);
        //System.out.println("DB path" + resourcePath.resolve("db").toString());
        DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples/input/P1debug.sql")).toURI();

    statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
  }

  @Test
  public void testQuery8()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(0);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        Assertions.assertEquals(30, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query8");
        plan.dump(new PrintStream(outfile));
    }

  @Test
  public void testQuery14()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(1);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        Assertions.assertEquals(5, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query14");
        plan.dump(new PrintStream(outfile));
    }
    @Test
    public void testQuery24()
            throws ExecutionControl.NotImplementedException,
            JSQLParserException,
            IOException,
            URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(2);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        //List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
        Assertions.assertEquals(4, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query24");
        plan.dump(new PrintStream(outfile));
    }
    @Test
    public void testQuery21()
            throws ExecutionControl.NotImplementedException,
            JSQLParserException,
            IOException,
            URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(3);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        //List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
        Assertions.assertEquals(4, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query21");
        plan.dump(new PrintStream(outfile));
    }

  @Test
  public void testQuery35()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(4);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        Assertions.assertEquals(6, HelperMethods.collectAllTuples(plan).size());

        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query35");
        plan.dump(new PrintStream(outfile));
    }
    @Test
    public void testQuery36()
            throws ExecutionControl.NotImplementedException,
            JSQLParserException,
            IOException,
            URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(5);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        //List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
        Assertions.assertEquals(6, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query36");
        plan.dump(new PrintStream(outfile));
    }

  @Test
  public void testQuery37()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(6);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        Assertions.assertEquals(6, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query37");
        plan.dump(new PrintStream(outfile));
    }
    @Test
    public void testQuery39()
            throws ExecutionControl.NotImplementedException,
            JSQLParserException,
            IOException,
            URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(7);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        //List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
        Assertions.assertEquals(6, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query39");
        plan.dump(new PrintStream(outfile));
    }

  @Test
  public void testQuery30()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(8);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        //List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
        Assertions.assertEquals(100, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        File outfile = new File(outputDir + "/query30");
        plan.dump(new PrintStream(outfile));
    }

    @Test
    public void testQuery11()
            throws ExecutionControl.NotImplementedException,
            JSQLParserException,
            IOException,
            URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(9);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        //List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
        String outputDir = "src/test/resources/samples/expected_output/p1debugout";
        Assertions.assertEquals(5, HelperMethods.collectAllTuples(plan).size());
        File outfile = new File(outputDir + "/query11");
        plan.dump(new PrintStream(outfile));

    }
}
