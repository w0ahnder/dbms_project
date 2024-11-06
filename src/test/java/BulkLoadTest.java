import common.*;
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
        URI path = Objects.requireNonNull(classLoader.getResource("binary_samples/input")).toURI();
        Path resourcePath = Paths.get(path);
        // System.out.println("DB path" + resourcePath.resolve("db").toString());
        DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
        DBCatalog.getInstance().config_file(resourcePath.toString());

        URI queriesFile =
                Objects.requireNonNull(classLoader.getResource("samples-2/input/queries.sql")).toURI();

        //statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
        //queryPlanBuilder = new QueryPlanBuilder();
    }

    @Test
    public void testQuery1()
            throws ExecutionControl.NotImplementedException,
            JSQLParserException,
            IOException,
            URISyntaxException {
       // Statement stmt = statementList.get(0);
        //Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, configList);

        TupleReader tr = new TupleReader( new File("src/test/resources/samples-2/input/db/data/Boats"));
        File Boats = new File("src/test/resources/samples-2/input/db/data/Boats");
        BulkLoad bl  = new BulkLoad(Boats, 10, 1, false);
        //bl.load();
        //BTree tree = new BTree();

    }
}