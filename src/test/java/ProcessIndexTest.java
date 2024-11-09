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
import tree.BulkLoad;

public class ProcessIndexTest {
  private static List<Statement> statementList;
  private static QueryPlanBuilder queryPlanBuilder;
  private static Statements statements;
  private static String tempDir;
  private static List<List<Integer>> configList;

  @BeforeAll
  static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
    ClassLoader classLoader = ProcessIndexTest.class.getClassLoader();
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
  public void testProcessIndex() throws IOException {
    File Boats = new File("src/test/resources/samples-2/input/db/data/Boats");
    BulkLoad bl = new BulkLoad(Boats, 10, 1, false);

    DBCatalog.getInstance().config_file("src/test/resources/samples-2/input");
    DBCatalog.getInstance()
        .setInterpreter("src/test/resources/samples-2/interpreter_config_file.txt");
    DBCatalog.getInstance().processIndex();
  }
}
