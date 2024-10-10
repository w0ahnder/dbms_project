import common.DBCatalog;
import common.QueryPlanBuilder;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.PhysicalOperators.Operator;
import org.junit.jupiter.api.Assertions;
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

public class P2CheckpointTest {
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
                Objects.requireNonNull(classLoader.getResource("binary_samples/input/queries.sql")).toURI();

        statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
        queryPlanBuilder = new QueryPlanBuilder();
        statementList = statements.getStatements();
    }

    @Test
    public void testQuery1()
            throws ExecutionControl.NotImplementedException,
            JSQLParserException,
            IOException,
            URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(0);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query1");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query2");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query3");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query4");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(481, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query5");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(481, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query6");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(0, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query7");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(5019, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query8");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(25224, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query9");
        plan.dump(new PrintStream(outfile));
    }

    @Test
    public void testQuery10()
            throws ExecutionControl.NotImplementedException,
            JSQLParserException,
            IOException,
            URISyntaxException {

        setupBeforeAllTests();
        Statement stmt = statementList.get(9);
        Operator plan = queryPlanBuilder.buildPlan(stmt);
        Assertions.assertEquals(19225, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query10");
        plan.dump(new PrintStream(outfile));
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
            Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
            String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
            File outfile = new File(outputDir + "/query11");
            plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(496964, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query12");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query13");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(25224, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query14");
        plan.dump(new PrintStream(outfile));
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
        Assertions.assertEquals(24764, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/binary_samples/p2checkpoint_outputs";
        File outfile = new File(outputDir + "/query15");
        plan.dump(new PrintStream(outfile));
    }











}
