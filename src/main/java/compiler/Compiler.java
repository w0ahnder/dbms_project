package compiler;

import common.DBCatalog;
import common.QueryPlanBuilder;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import operator.Operator;
import org.apache.logging.log4j.*;

/**
 * Top level harness class; reads queries from an input file one at a time, processes them and sends
 * output to file or to System depending on flag.
 */
public class Compiler {
  private static final Logger logger = LogManager.getLogger();

  private static String outputDir;
  private static String inputDir;
  private static final boolean outputToFiles = true; // true = output to

  // files, false = output
  // to System.out

  /**
   * Reads statements from queriesFile one at a time, builds query plan and evaluates, dumping
   * results to files or console as desired.
   *
   * <p>If dumping to files result of ith query is in file named queryi, indexed stating at 1.
   */
  public static void main(String[] args) {

    inputDir = args[0];
    outputDir = args[1];
    // inputDir = "src/test/resources/samples/input";
    // outputDir = "src/test/resources/samples/expected_output/outputs";
    DBCatalog.getInstance().setDataDirectory(inputDir + "/db");
    try {
      logger.info("reading queries");
      String str = Files.readString(Paths.get(inputDir + "/queries.sql"));
      Statements statements = CCJSqlParserUtil.parseStatements(str);
      logger.info("Parsing statement");
      QueryPlanBuilder queryPlanBuilder = new QueryPlanBuilder();
      logger.info("Finished builder");

      if (outputToFiles) {
        for (File file : (new File(outputDir).listFiles())) file.delete(); // clean output directory
      }

      int counter = 1; // for numbering output files
      logger.info("entering statement loop");
      for (Statement statement : statements.getStatements()) {

        logger.info("Processing query " + counter + ": " + statement);

        try {
          Operator plan = queryPlanBuilder.buildPlan(statement);

          if (outputToFiles) {
            File outfile = new File(outputDir + "/query" + counter);
            plan.dump(new PrintStream(outfile));
          } else {
            plan.dump(System.out);
          }
        } catch (Exception e) {
          logger.error(e.getMessage());
        }

        ++counter;
      }
    } catch (Exception e) {
      System.err.println("Exception occurred in interpreter");
      logger.error(e.getMessage());
    }
  }
}
