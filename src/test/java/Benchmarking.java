import jdk.jshell.spi.ExecutionControl;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import common.*;
import operator.PhysicalOperators.Operator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utilities.DataGenerator;

import javax.management.Query;
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
import java.util.Objects;
import java.util.List;


public class Benchmarking {
    public String path;
    public int numtuples;
    public String outpath;
    public static Statements statements;
    public static QueryPlanBuilder queryPlanBuilder;
    public static List<Statement> statementList;
    private static List<List<Integer>> configList = new ArrayList<>();
    private static String time_path;
    private static PrintStream ps;

    @BeforeAll
    public static void setUP() throws URISyntaxException, IOException, JSQLParserException {

        ClassLoader classLoader = Benchmarking.class.getClassLoader();
        URI path = Objects.requireNonNull(classLoader.getResource("benchmarking/input")).toURI();
        Path resourcePath = Paths.get(path);
        // System.out.println("DB path" + resourcePath.resolve("db").toString());
        DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
        DBCatalog.getInstance().config_file(resourcePath.toString());

        URI queriesFile =
                Objects.requireNonNull(classLoader.getResource("benchmarking/input/queries.sql")).toURI();

        statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
        queryPlanBuilder = new QueryPlanBuilder();
        statementList = statements.getStatements();
        ArrayList<Integer> firstList = new ArrayList<>();
        firstList.add(0); // Add [0]
        configList.add(firstList);
        ArrayList<Integer> secondList = new ArrayList<>();
        secondList.add(1); // Add [1]
        secondList.add(2); // Add [2]
        configList.add(secondList);
        time_path = "src/test/resources/benchmarking/time";
        ps = new PrintStream(new File(time_path));
    }

    //can create additional methods in DBCatalog to set the types of join and the buffer page size for external sort
    @Test
    public void runQueries() throws JSQLParserException, URISyntaxException, IOException, ExecutionControl.NotImplementedException {

        String outdir1 = "src/test/resources/benchmarking/input/db/Table1";
        DataGenerator gen1 = new DataGenerator(200, 5000, 3, outdir1);

        String outdir2 = "src/test/resources/benchmarking/input/db/Table2";
        DataGenerator gen2 = new DataGenerator(200, 5000, 3, outdir2);

        //TUPLE NESTED JOIN

        DBCatalog.getInstance().setLoop(0);
        ps.println("Tuple Nested loop join ....");
        execute("src/test/resources/benchmarking/output/TNLJ"); //output directory for this join
        System.out.println("Finished TNLJ....");

        //BLOCK NESTED JOIN  1 page buffer
        DBCatalog.getInstance().setLoop(1);
        DBCatalog.getInstance().setBNLbuff(1);
        ps.println("Block Nested loop join with 1 buffer page ....");
        execute("src/test/resources/benchmarking/output/BNLJ1");
        System.out.println("Finished BNLJ1....");

        ps.println("Block Nested loop join with 5 buffer pages ....");
        //BLOCK NESTED JOIN WITH 5 PAGE BUFFER
        DBCatalog.getInstance().setBNLbuff(5);
        execute("src/test/resources/benchmarking/output/BNLJ5");
        System.out.println("Finished BNLJ5....");

        //SORT MERGE JOIN
        ps.println("Sort Merge Loop Join  ....");
        DBCatalog.getInstance().setLoop(2);
        DBCatalog.getInstance().setSortBuff(5);
        execute("src/test/resources/benchmarking/output/SMJ");
        ps.close();

    }
    public void execute(String join_path) throws ExecutionControl.NotImplementedException, IOException {
        String temp = "src/test/resources/benchmarking/temp";
        for(int i=0; i<3;i++){
            System.out.println("query " +  i + "...");
            TupleWriter tw = new TupleWriter(join_path + "/query" + i);
            Statement s = statementList.get(i);
            Operator plan = queryPlanBuilder.buildPlan(s, temp, configList);
            long start = System.currentTimeMillis();
            plan.dump(tw);
            tw.close();
            long end = System.currentTimeMillis();
            long elapsed = end-start;
            ps.println("Elapsed time for query " + i +" is " + elapsed);

            Convert c = new Convert(join_path + "/query" + i,
                    new PrintStream(new File(join_path + "/query" + i + "_human")));
            c.bin_to_human();
        }
    }



}