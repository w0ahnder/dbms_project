import common.*;

import java.io.*;
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
import tree.BulkLoad;

public class P3Benchmarking {
    private static List<Statement> statementList;
    private static QueryPlanBuilder queryPlanBuilder;
    private static Statements statements;
    private static String tempDir;
    private static List<List<Integer>> configList;

    @BeforeAll
    static void setupBeforeAllTests() throws IOException, JSQLParserException, URISyntaxException {
        ClassLoader classLoader = P3Benchmarking.class.getClassLoader();
        URI path = Objects.requireNonNull(classLoader.getResource("samples-2/input")).toURI();
        Path resourcePath = Paths.get(path);
        DBCatalog.getInstance().setDataDirectory(resourcePath.resolve("db").toString());
        DBCatalog.getInstance().config_file(resourcePath.toString());

        URI queriesFile =
                Objects.requireNonNull(classLoader.getResource("samples-2/input/p3queries.sql")).toURI();


        statements = CCJSqlParserUtil.parseStatements(Files.readString(Paths.get(queriesFile)));
        queryPlanBuilder = new QueryPlanBuilder();
    }

    public static List<List<Integer>> readNumbersFromFile(String filePath) {
        List<List<Integer>> numbers = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                List<Integer> lines = new ArrayList<>();
                for (String token : tokens) {
                    try {
                        int number = Integer.parseInt(token);
                        lines.add(number);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing number: " + token);
                    }
                }
                numbers.add(lines);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return numbers;
    }


    @Test
    public void testFullScan() throws ExecutionControl.NotImplementedException, IOException {
        String configFile = "src/test/resources/samples-2/interpreter_config_file.txt";
        String inputDir = "src/test/resources/samples-2/input";
        DBCatalog.getInstance().setDataDirectory(inputDir + "/db");
        DBCatalog.getInstance().config_file(inputDir);
        DBCatalog.getInstance().setInterpreter(configFile);
        String temp = "src/test/resources/samples-2/temp";
        List<List<Integer>> configList  = readNumbersFromFile(configFile);
        //plan config is whether we use full scan or not
        //first run full scan
        //public Operator buildPlan(
        //      Statement stmt,
        //      String tempDir,
        //      List<List<Integer>> planConfList,
        //      Integer indexFlag,
        //      Integer queryFlag)
        //      throws ExecutionControl.NotImplementedException {
        Statement s = statements.getStatements().get(0);
        Operator op = queryPlanBuilder.buildPlan(s, temp, configList, 0,1);
        File scan1 = new File("src/test/resources/samples-2/p3/fullScan/query1");
        TupleWriter tw = new TupleWriter("src/test/resources/samples-2/p3/fullScan/query1");
        op.dump(tw);
        //tw.close();
        String outputDir = "src/test/resources/samples-2/p3/fullScan/query1human";
        File outFile = new File(outputDir);
        Convert c = new Convert("src/test/resources/samples-2/p3/fullScan/query1", new PrintStream(outFile));
        c.bin_to_human();

    }
    @Test
    public void testFullScan2(){
        String configFile = "src/test/resources/samples-2/interpreter_config_file.txt";
        String inputDir = "src/test/resources/samples-2/input";
        DBCatalog.getInstance().setDataDirectory(inputDir + "/db");
        DBCatalog.getInstance().config_file(inputDir);
        DBCatalog.getInstance().setInterpreter(configFile);
        //plan config is whether we use full scan or not
        //first run full scan
        Statement s = statements.getStatements().get(1);
        //Operator op = queryPlanBuilder.

    }
    @Test
    public void testFullScan3(){
        String configFile = "src/test/resources/samples-2/interpreter_config_file.txt";
        String inputDir = "src/test/resources/samples-2/input";
        DBCatalog.getInstance().setDataDirectory(inputDir + "/db");
        DBCatalog.getInstance().config_file(inputDir);
        DBCatalog.getInstance().setInterpreter(configFile);
        //plan config is whether we use full scan or not
        //first run full scan
        Statement s = statements.getStatements().get(2);
        //Operator op = queryPlanBuilder.

    }

}




