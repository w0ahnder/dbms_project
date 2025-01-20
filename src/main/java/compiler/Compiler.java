package compiler;

import common.Convert;
import common.DBCatalog;
import common.QueryPlanBuilder;
import common.TupleWriter;
import java.io.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.PhysicalOperators.Operator;
import org.apache.logging.log4j.*;

/**
 * Top level harness class; reads queries from an input file one at a time, processes them and sends
 * output to file or to System depending on flag.
 */
public class Compiler {
  private static final Logger logger = LogManager.getLogger();

  private static String outputDir;
  private static String inputDir;
  private static String tempDir;
  private static Integer indexFlag;
  private static Integer queryFlag;
  private static final boolean outputToFiles = true;

  /**
   * Reads statements from queriesFile one at a time, builds query plan and evaluates, dumping
   * results to files or console as desired.
   *
   * <p>If dumping to files result of ith query is in file named queryi, indexed stating at 1.
   */
  public static void main(String[] args) {

    String configFile = args[0];
    readDirectories(configFile);
     inputDir = args[0];
     outputDir = args[1];
     tempDir = args[2];

      // TODO: Get the join and sort methods from the configuration file
    // TODO: get the location of the tmepDir

    DBCatalog.getInstance().setDataDirectory(inputDir + "/db");
    DBCatalog.getInstance().config_file(inputDir);
    DBCatalog.getInstance().setInterpreter(configFile);
    DBCatalog.getInstance().createStatsFile(inputDir);
    DBCatalog.getInstance().processIndex(); // reads

    try {
      String str = Files.readString(Paths.get(inputDir + "/queries.sql"));
      Statements statements = CCJSqlParserUtil.parseStatements(str);
      QueryPlanBuilder queryPlanBuilder = new QueryPlanBuilder();
      if (outputToFiles) {
        for (File file : (new File(tempDir).listFiles())) file.delete();
        for (File file : (new File(outputDir).listFiles())) file.delete();
      }

      int counter = 1; // for numbering output files
      for (Statement statement : statements.getStatements()) {
        try {
          System.out.println(statement);
          Operator plan = queryPlanBuilder.buildPlan(statement, tempDir, indexFlag, queryFlag);
          queryPlanBuilder.printLogicalPlan(outputDir + "/query" + counter + "_");
          if (outputToFiles) {
            TupleWriter tw = new TupleWriter(outputDir + "/query" + counter);
            long start = System.currentTimeMillis();
            if (plan != null) {
              System.out.println(plan);
              plan.dump(tw);
            }
            // plan.dump(new PrintStream(outfile));

            long end = System.currentTimeMillis();
            // tw.close();
            System.out.println("Elapsed time: " + (end - start));
            Convert cv = new Convert(tw.outFile, new PrintStream(tw.outFile + "human"));
            cv.bin_to_human();
          } else {

            plan.dump(System.out);
          }
        } catch (Exception e) {
          logger.error(e.getMessage());
          e.printStackTrace();
        }

        ++counter;
      }
    } catch (Exception e) {
      System.err.println("Exception occurred in interpreter");
      logger.error(e.getMessage());
    }
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

  private static void readDirectories(String filePath) {
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      List<String> lines = new ArrayList<>();
      String line;
      while ((line = br.readLine()) != null) {
        lines.add(line);
      }
      inputDir = lines.get(0);
      outputDir = lines.get(1);
      tempDir = lines.get(2);
    } catch (IOException e) {
      System.err.println("Error reading the file: " + e.getMessage());
    }
  }
}
