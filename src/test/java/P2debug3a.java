import common.Convert;
import common.DBCatalog;
import common.QueryPlanBuilder;
import common.TupleWriter;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.*;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.*;
import operator.PhysicalOperators.Operator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.*;

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

public class P2debug3a {
    private static List<Statement> statementList;
    private static QueryPlanBuilder queryPlanBuilder;
    private static Statements statements;
    private static String tempDir;
    private static List<List<Integer>> configList;

    @BeforeAll
    static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
        ClassLoader classLoader = P2debug3a.class.getClassLoader();
        URI path = Objects.requireNonNull(classLoader.getResource("P2debugging/3a/input")).toURI();
        Path resourcePath = Paths.get(path);
        // System.out.println("DB path" + resourcePath.resolve("db").toString());
        DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
        DBCatalog.getInstance().config_file(resourcePath.toString());

        URI queriesFile =
                Objects.requireNonNull(classLoader.getResource("P2debugging/3a/input/queries.sql")).toURI();

        statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
        queryPlanBuilder = new QueryPlanBuilder();
        statementList = statements.getStatements();
        tempDir = "src/test/resources/P2debugging/3a/temp";
        configList = new ArrayList<>();
        ArrayList<Integer> firstList = new ArrayList<>();
        firstList.add(0);
        configList.add(firstList);
        ArrayList<Integer> secondList = new ArrayList<>();
        secondList.add(1);
        secondList.add(6);
        configList.add(secondList);
    }

    @Test
    public void testQuery1()
            throws ExecutionControl.NotImplementedException,
            IOException{

        File outFile = new File("src/test/resources/P2debugging/3a/human_expected/query1_human");
        Convert c = new Convert("src/test/resources/P2debugging/3a/expected/query1", new PrintStream(outFile));
        c.bin_to_human();

        Statement stmt = statementList.get(0);
        Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, configList);
        // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/P2debugging/3a/binary_output";
        // File outfile = new File(outputDir + "/query1");
        TupleWriter tw = new TupleWriter(outputDir + "/query1");
        plan.dump(tw);

        outputDir = "src/test/resources/P2debugging/3a/human_output";
        String out2 = "src/test/resources/P2debugging/3a/binary_output";
        File outFile2 = new File(outputDir + "/query1_human");

        Convert c2 = new Convert(out2 + "/query1", new PrintStream(outFile2));
        c2.bin_to_human();

    }

    @Test
    public void testQuery2()
            throws ExecutionControl.NotImplementedException,
            IOException{
        File outFile = new File("src/test/resources/P2debugging/3a/human_expected/query2_human");
        Convert c = new Convert("src/test/resources/P2debugging/3a/expected/query2", new PrintStream(outFile));
        c.bin_to_human();

        Statement stmt = statementList.get(1);
        Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, configList);
        String outputDir = "src/test/resources/P2debugging/3a/binary_output";
        TupleWriter tw = new TupleWriter(outputDir + "/query2");
        plan.dump(tw);
        outputDir = "src/test/resources/P2debugging/3a/human_output";
        String out2 = "src/test/resources/P2debugging/3a/binary_output";
        File outFile2 = new File(outputDir + "/query2_human");

        Convert c2 = new Convert(out2 + "/query2", new PrintStream(outFile2));
        c2.bin_to_human();

    }


    @Test
    public void testQuery3()
            throws ExecutionControl.NotImplementedException,
            IOException{
        File outFile = new File("src/test/resources/P2debugging/3a/human_expected/query3_human");
        Convert c = new Convert("src/test/resources/P2debugging/3a/expected/query3", new PrintStream(outFile));
        c.bin_to_human();

        Statement stmt = statementList.get(2);
        Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, configList);
        // Assertions.assertEquals(1000, HelperMethods.collectAllTuples(plan).size());
        String outputDir = "src/test/resources/P2debugging/3a/binary_output";
        // File outfile = new File(outputDir + "/query1");
        TupleWriter tw = new TupleWriter(outputDir + "/query3");
        plan.dump(tw);
        outputDir = "src/test/resources/P2debugging/3a/human_output";
        String out2 = "src/test/resources/P2debugging/3a/binary_output";
        File outFile2 = new File(outputDir + "/query3_human");

        Convert c2 = new Convert(out2 + "/query3", new PrintStream(outFile2));
        c2.bin_to_human();
    }


    @Test
    public void testQuery4()
            throws ExecutionControl.NotImplementedException,
            IOException {
        File outFile = new File("src/test/resources/P2debugging/3a/human_expected/query4_human");
        Convert c = new Convert("src/test/resources/P2debugging/3a/expected/query4", new PrintStream(outFile));
        c.bin_to_human();

        Statement stmt = statementList.get(3);
        Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, configList);
        String outputDir = "src/test/resources/P2debugging/3a/binary_output";
        TupleWriter tw = new TupleWriter(outputDir + "/query4");
        plan.dump(tw);
        outputDir = "src/test/resources/P2debugging/3a/human_output";
        String out2 = "src/test/resources/P2debugging/3a/binary_output";
        File outFile2 = new File(outputDir + "/query4_human");

        Convert c2 = new Convert(out2 + "/query4", new PrintStream(outFile2));
        c2.bin_to_human();
    }


    @Test
    public void testQuery5()
            throws ExecutionControl.NotImplementedException,
            IOException {
        File outFile = new File("src/test/resources/P2debugging/3a/human_expected/query5_human");
        Convert c = new Convert("src/test/resources/P2debugging/3a/expected/query5", new PrintStream(outFile));
        c.bin_to_human();

        Statement stmt = statementList.get(4);
        Operator plan = queryPlanBuilder.buildPlan(stmt, tempDir, configList);
        String outputDir = "src/test/resources/P2debugging/3a/binary_output";
        TupleWriter tw = new TupleWriter(outputDir + "/query5");
        plan.dump(tw);
        outputDir = "src/test/resources/P2debugging/3a/human_output";
        String out2 = "src/test/resources/P2debugging/3a/binary_output";
        File outFile2 = new File(outputDir + "/query5_human");

        Convert c2 = new Convert(out2 + "/query5", new PrintStream(outFile2));
        c2.bin_to_human();
    }

}
