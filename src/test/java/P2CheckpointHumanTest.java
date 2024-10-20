import common.DBCatalog;
import common.QueryPlanBuilder;
import common.TupleWriter;
import common.*;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.PhysicalOperators.Operator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


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

public class P2CheckpointHumanTest {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = P2CheckpointHumanTest.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("binary_samples/input")).toURI();
    Path resourcePath = Paths.get(path);
    // System.out.println("DB path" + resourcePath.resolve("db").toString());
    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
    DBCatalog.getInstance().config_file(resourcePath.toString());

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("binary_samples/input/queries.sql")).toURI();

    statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
  }


  //@Test
  public void testQuery16()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(15);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outfile = new File(outputDir + "/BoatsRead_human");
    TupleWriter tw = new TupleWriter(outputDir + "/BoatsRead");
    plan.dump(tw);
    tw.close();
  }
  /**diff -qr <(sort src/test/resources/binary_samples/expected_human/query15_humanreadable)
  <(sort src/test/resources/binary_samples/p2human/query15_human)
  **/

  /** queries 13,14,15 order by is good
   * diff -qr src/test/resources/binary_samples/expected_human/query15_humanreadable
   * src/test/resources/binary_samples/p2human/query15_human
   */
  @Test
  public void testQuery1()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    setupBeforeAllTests();
    Statement stmt = statementList.get(0);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query1_human");
    Convert c = new Convert(out2 + "/query1", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery2()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(1);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query2_human");
    Convert c = new Convert(out2 + "/query2", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery3()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(2);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query3_human");
    Convert c = new Convert(out2 + "/query3", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery4()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(3);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query4_human");
    Convert c = new Convert(out2 + "/query4", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery5()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(4);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(481, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query5_human");
    Convert c = new Convert(out2 + "/query5", new PrintStream(outFile));
    c.bin_to_human();

  }

  @Test
  public void testQuery6()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(5);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(481, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query6_human");
    Convert c = new Convert(out2 + "/query6", new PrintStream(outFile));
    c.bin_to_human();

  }

  @Test
  public void testQuery7()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(6);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(0, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query7_human");
    Convert c = new Convert(out2 + "/query7", new PrintStream(outFile));
    c.bin_to_human();

  }

  @Test
  public void testQuery8()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(7);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(5019, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query8_human");
    Convert c = new Convert(out2 + "/query8", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery9()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(8);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(25224, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query9_human");
    Convert c = new Convert(out2+ "/query9", new PrintStream(outFile));
    c.bin_to_human();
  }

  // @Test
  public void testQuery10()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(9);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(19225, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";

  }

  @Test
  public void testQuery11()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(10);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query11_human");
    Convert c = new Convert(out2+ "/query11", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery12()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(11);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(496964, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query12_human");
    Convert c = new Convert(out2 + "/query12", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery13()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(12);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query13_human");
    Convert c = new Convert(out2+ "/query13", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery14()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(13);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(25224, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query14_human");
    Convert c = new Convert(out2+ "/query14", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery15()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {

    setupBeforeAllTests();
    Statement stmt = statementList.get(14);
    Operator plan = queryPlanBuilder.buildPlan(stmt);
    // Assertions.assertEquals(24764, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/binary_samples/p2human";
    String out2  = "src/test/resources/binary_samples/p2checkpoint_outputs";
    File outFile = new File(outputDir + "/query15_human");
    Convert c = new Convert(out2 + "/query15", new PrintStream(outFile));
    c.bin_to_human();
  }
}
