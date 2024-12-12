import common.Convert;
import common.DBCatalog;
import common.QueryPlanBuilder;
import common.TupleWriter;
import jdk.jfr.DataAmount;
import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.PhysicalOperators.Operator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utilities.DataGenerator;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.random.RandomGenerator;

public class P5Benchmarking {

        private static List<Statement> statementList;
        private static QueryPlanBuilder queryPlanBuilder;
        private static Statements statements;
        private static String tempDir;
        private static List<List<Integer>> configList;

        @BeforeAll
        static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
            ClassLoader classLoader = P5Benchmarking.class.getClassLoader();
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
        public void createTables()
                throws ExecutionControl.NotImplementedException,
                JSQLParserException,
                IOException,
                URISyntaxException {
            //3 tables 500 and 1000 and 5000
            String db = "/Users/savitta/Desktop/cs4320/p5/input/db/data";
            //small table
            DataGenerator smallTable = new DataGenerator(1000, 500, 3, db +"/small");
            smallTable.generateTuples();
            //medium table
            DataGenerator medTable = new DataGenerator(1000, 1000, 3, db + "/medium");
            medTable.generateTuples();
            //large table
            DataGenerator largeTable = new DataGenerator(1000, 5000, 3, db+"/large") ;
            largeTable.generateTuples();


            Convert smallBin =  new Convert(db+ "/small", new PrintStream(db+"/smallHuman"));
            smallBin.bin_to_human();

            Convert medBin =  new Convert(db+ "/medium", new PrintStream(db+"/mediumHuman"));
            medBin.bin_to_human();

            Convert largeBin =  new Convert(db+ "/large", new PrintStream(db+"/largeHuman"));
            largeBin.bin_to_human();

        }

        @Test
    public void runTheQueries() throws IOException, ExecutionControl.NotImplementedException {

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

}
