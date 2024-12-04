import common.Convert;
import common.DBCatalog;
import common.QueryPlanBuilder;
import common.TupleWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
import operator.PhysicalOperators.Operator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SelectPlanTest {

  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;
  private static String tempDir;
  private static List<List<Integer>> configList;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = SelectPlanTest.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples-2/input")).toURI();
    Path resourcePath = Paths.get(path);
    // System.out.println("DB path" + resourcePath.resolve("db").toString());
    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
    DBCatalog.getInstance().config_file(resourcePath.toString());
    DBCatalog.getInstance().setEvalQuery(true);
    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples-2/input/selectplan.sql")).toURI();

    DBCatalog.getInstance().createStatsFile("src/test/resources/samples-2/input");

    statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
    tempDir = "src/test/resources/binary_samples/temp";
    configList = new ArrayList<>();
    ArrayList<Integer> firstList = new ArrayList<>();
    firstList.add(0); // Add [0]
    configList.add(firstList);
    ArrayList<Integer> secondList = new ArrayList<>();
    secondList.add(1); // Add [1]
    secondList.add(2); // Add [2]
    configList.add(secondList);
  }

  @Test
  public void testQuery1()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(0);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, 0, 1);
    String binaryDir =
        "src/test/resources/samples-2/selectPlanTest/output"; // directory for binary files
    TupleWriter tw = new TupleWriter(binaryDir + "/query1");
    plan.dump(tw);
    File humanFile = new File("src/test/resources/samples-2/selectPlanTest/human/query1_human");
    Convert c = new Convert(binaryDir + "/query1", new PrintStream(humanFile));
    c.bin_to_human();
    queryPlanBuilder.printLogicalPlan("src/test/resources/samples-2/selectPlanTest/query1_");
  }

  @Test
  public void testQuery2()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(1);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, 0, 1);
    String binaryDir =
        "src/test/resources/samples-2/selectPlanTest/output"; // directory for binary files
    TupleWriter tw = new TupleWriter(binaryDir + "/query2");
    plan.dump(tw);
    File humanFile = new File("src/test/resources/samples-2/selectPlanTest/human/query2_human");
    Convert c = new Convert(binaryDir + "/query2", new PrintStream(humanFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery3()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(2);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, 0, 1);
    String binaryDir =
        "src/test/resources/samples-2/selectPlanTest/output"; // directory for binary files
    TupleWriter tw = new TupleWriter(binaryDir + "/query3");
    plan.dump(tw);
    File humanFile = new File("src/test/resources/samples-2/selectPlanTest/human/query3_human");
    Convert c = new Convert(binaryDir + "/query3", new PrintStream(humanFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery4()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(3);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, 0, 1);
    String binaryDir =
        "src/test/resources/samples-2/selectPlanTest/output"; // directory for binary files
    TupleWriter tw = new TupleWriter(binaryDir + "/query4");
    plan.dump(tw);
    File humanFile = new File("src/test/resources/samples-2/selectPlanTest/human/query4_human");
    Convert c = new Convert(binaryDir + "/query4", new PrintStream(humanFile));
    c.bin_to_human();
    queryPlanBuilder.printLogicalPlan("src/test/resources/samples-2/selectPlanTest/query4_");
  }

  @Test
  public void testQuery5()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(4);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, 0, 1);
    String binaryDir =
        "src/test/resources/samples-2/selectPlanTest/output"; // directory for binary files
    TupleWriter tw = new TupleWriter(binaryDir + "/query5");
    plan.dump(tw);
    File humanFile = new File("src/test/resources/samples-2/selectPlanTest/human/query5_human");
    Convert c = new Convert(binaryDir + "/query5", new PrintStream(humanFile));
    c.bin_to_human();
    queryPlanBuilder.printLogicalPlan("src/test/resources/samples-2/selectPlanTest/query5_");
  }

  @Test
  public void testQuery6()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(5);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, 0, 1);
    String binaryDir =
        "src/test/resources/samples-2/selectPlanTest/output"; // directory for binary files
    TupleWriter tw = new TupleWriter(binaryDir + "/query6");
    plan.dump(tw);
    File humanFile = new File("src/test/resources/samples-2/selectPlanTest/human/query6_human");
    Convert c = new Convert(binaryDir + "/query6", new PrintStream(humanFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery7()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(6);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, 0, 1);
    String binaryDir =
        "src/test/resources/samples-2/selectPlanTest/output"; // directory for binary files
    TupleWriter tw = new TupleWriter(binaryDir + "/query7");
    plan.dump(tw);
    File humanFile = new File("src/test/resources/samples-2/selectPlanTest/human/query7_human");
    Convert c = new Convert(binaryDir + "/query7", new PrintStream(humanFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery8()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(7);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, 0, 1);
    String binaryDir =
        "src/test/resources/samples-2/selectPlanTest/output"; // directory for binary files
    TupleWriter tw = new TupleWriter(binaryDir + "/query8");
    plan.dump(tw);
    File humanFile = new File("src/test/resources/samples-2/selectPlanTest/human/query8_human");
    Convert c = new Convert(binaryDir + "/query8", new PrintStream(humanFile));
    c.bin_to_human();
  }
}
