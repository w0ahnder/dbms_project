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
import operator.Operator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class P1Debug {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = P1UnitTests.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples/input")).toURI();
    Path resourcePath = Paths.get(path);
    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db2").toString());
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
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query8");
    System.out.println("dumping");
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
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query14");
    System.out.println("dumping");
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
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query24");
    System.out.println("dumping");
    plan.dump(new PrintStream(outfile));
  }

  // 21 , AND 24 failed
  @Test
  public void testQuery21()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(3);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query21");
    System.out.println("dumping");
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
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query35");
    System.out.println("dumping");
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
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query36");
    System.out.println("dumping");
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
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query37");
    System.out.println("dumping");
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
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query39");
    System.out.println("dumping");
    plan.dump(new PrintStream(outfile));
  }

  @Test
  public void testQuerymine()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(8);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/querymine");
    System.out.println("dumping");
    plan.dump(new PrintStream(outfile));
  }

  @Test
  public void testQuery30()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(9);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // List<Tuple> tuples = HelperMethods.collectAllTuples(plan);
    String outputDir = "src/test/resources/samples/expected_output/p1debugout";
    File outfile = new File(outputDir + "/query30");
    System.out.println("dumping");
    plan.dump(new PrintStream(outfile));
  }
}
