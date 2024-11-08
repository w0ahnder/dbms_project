import common.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tree.BTree;
import tree.BulkLoad;

public class BulkLoadTest {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;
  private static String tempDir;
  private static List<List<Integer>> configList;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = BulkLoadTest.class.getClassLoader();
    URI path = Objects.requireNonNull(classLoader.getResource("samples-2/input")).toURI();
    Path resourcePath = Paths.get(path);
    DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
    DBCatalog.getInstance().config_file(resourcePath.toString());

    URI queriesFile =
        Objects.requireNonNull(classLoader.getResource("samples-2/input/queries.sql")).toURI();

    // statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
    // queryPlanBuilder = new QueryPlanBuilder();
  }

  @Test
  public void testBoatsE() throws IOException {
    File Boats = new File("src/test/resources/samples-2/input/db/data/Boats");
    BulkLoad bl = new BulkLoad(Boats, 10, 1, false);
    bl.load();
    BTree tree = bl.getTree();
    String path = "src/test/resources/samples-2/bulkload";
    tree.tree_to_file(path + "/Boats.E_bin");
  }

  @Test
  public void testSailorsA() throws IOException {

    String tablepath = "src/test/resources/samples-2/input/db/data/Sailors";
    String sortedFile = "src/test/resources/samples-2/bulkload/SailorsSort";

    BulkLoad.sortRelation("Sailors", tablepath, "A", sortedFile);
    File SortedSailors = new File("src/test/resources/samples-2/bulkload/SailorsSort");
    BulkLoad bl = new BulkLoad(SortedSailors, 15, 0, true);
    bl.load();
    BTree tree = bl.getTree();
    String path = "src/test/resources/samples-2/bulkload";
    tree.tree_to_file(path + "/SailorsA_bin");
  }
}
