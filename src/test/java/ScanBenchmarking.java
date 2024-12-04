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

public class ScanBenchmarking {

  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;
  private static String tempDir;
  private static List<List<Integer>> configList;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = ScanBenchmarking.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples-2/input")).toURI();
    Path resourcePath = Paths.get(path);
    // System.out.println("DB path" + resourcePath.resolve("db").toString());
    String configFile = "src/test/resources/samples-2/interpreter_config_file.txt";
    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
    DBCatalog.getInstance().config_file(resourcePath.toString());
    DBCatalog.getInstance().setInterpreter(configFile);
    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples-2/input/scan_bench.sql")).toURI();

    statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    queryPlanBuilder = new QueryPlanBuilder();
    statementList = statements.getStatements();
    tempDir = "src/test/resources/samples-2/temp";
    configList = new ArrayList<>();
    ArrayList<Integer> firstList = new ArrayList<>();
    firstList.add(0); // Add [0]
    configList.add(firstList);
    ArrayList<Integer> secondList = new ArrayList<>();
    secondList.add(1); // Add [1]
    secondList.add(2); // Add [2]
    configList.add(secondList);
  }

  public void runBenchmark(int s_ind, String type, int index, int query)
      throws ExecutionControl.NotImplementedException, IOException {
    System.out.println("Running query " + s_ind + " with " + type);
    Statement stmt = statementList.get(s_ind);
    String outputDir = "src/test/resources/samples-2/scan_benchmark";
    TupleWriter tw = new TupleWriter(outputDir + "/query" + s_ind + type);
    Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, index, query);
    long start = System.currentTimeMillis();
    plan.dump(tw);
    // tw.close();
    long end = System.currentTimeMillis();
    long elapsed = end - start;
    System.out.println("Elapsed time for query " + s_ind + " with type " + type + " is " + elapsed);
    outputDir = "src/test/resources/samples-2/scan_benchmark_human";
    String out2 = "src/test/resources/samples-2/scan_benchmark";
    File outFile = new File(outputDir + "/query" + s_ind + type + "human");

    Convert c = new Convert(out2 + "/query" + s_ind + type, new PrintStream(outFile));
    c.bin_to_human();
    System.out.println("Finished running query " + s_ind + "with " + type);
  }

  // @Test
  public void testBenchmarkFullScan() throws ExecutionControl.NotImplementedException, IOException {
    for (int i = 0; i < 3; i += 1) {
      runBenchmark(i, "fullscan-final", 0, 1);
    }
  }

  // @Test
  public void testBenchmarkIndex() throws ExecutionControl.NotImplementedException, IOException {
    DBCatalog.getInstance().processIndex();
    for (int i = 0; i < 3; i += 1) {
      runBenchmark(i, "index-clustered", 1, 1);
    }
  }
}
