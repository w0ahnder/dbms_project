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

public class SelectPushTest {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;
  private static String tempDir;
  private static List<List<Integer>> configList;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = SelectPushTest.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples-2/input")).toURI();
    Path resourcePath = Paths.get(path);
    // System.out.println("DB path" + resourcePath.resolve("db").toString());
    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
    // DBCatalog.getInstance().config_file(resourcePath.toString());
    DBCatalog.getInstance().setEvalQuery(true);

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples-2/input/selectPush.sql")).toURI();

    DBCatalog.getInstance().createStatsFile("src/test/resources/binary_samples/input");
    DBCatalog.getInstance().processIndex();

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
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, configList, 0, 1);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/samples-2/selectPush";
    // File outfile = new File(outputDir + "/query1");
    TupleWriter tw = new TupleWriter(outputDir + "/query1");
    plan.dump(tw);
    outputDir = "src/test/resources/samples-2/selectPush_human";
    String out2 = "src/test/resources/samples-2/selectPush";
    File outFile = new File(outputDir + "/query1_human");
    // File outFilereset = new File(outputDir + "/query1_human2");

    Convert c = new Convert(out2 + "/query1", new PrintStream(outFile));
    c.bin_to_human();
  }

  @Test
  public void testQuery2()
      throws ExecutionControl.NotImplementedException,
          JSQLParserException,
          IOException,
          URISyntaxException {
    DBCatalog.getInstance().processIndex();
    Statement stmt = statementList.get(1);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, configList, 0, 1);
    // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
    String outputDir = "src/test/resources/samples-2/selectPush";
    // File outfile = new File(outputDir + "/query1");
    TupleWriter tw = new TupleWriter(outputDir + "/query2");
    plan.dump(tw);
    outputDir = "src/test/resources/samples-2/selectPush_human";
    String out2 = "src/test/resources/samples-2/selectPush";
    File outFile = new File(outputDir + "/query2_human");
    // File outFilereset = new File(outputDir + "/query1_human2");

    Convert c = new Convert(out2 + "/query2", new PrintStream(outFile));
    c.bin_to_human();
  }
}
