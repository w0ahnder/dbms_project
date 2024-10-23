package common;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to contain information about database - names of tables, schema of each table and file
 * where each table is located. Uses singleton pattern.
 *
 * <p>Assumes dbDirectory has a schema.txt file and a /data subdirectory containing one file per
 * relation, named "relname".
 *
 * <p>Call by using DBCatalog.getInstance();
 */
public class DBCatalog {
  private final Logger logger = LogManager.getLogger();

  private final HashMap<String, ArrayList<Column>> tables;
  private static DBCatalog db;
  private final ConcurrentHashMap<String, String> aliasmap;
  private final ConcurrentHashMap<String, ArrayList<Column>> aliasSchema;
  private boolean useAlias = false;
  private boolean BNLJ = false;
  private boolean TNLJ = false;
  private boolean SMJ = false;
  private int BNLJ_buff;

  private int sort_type; // 0 if in memory
  private int sort_buff;
  private String dbDirectory;

  /** Reads schemaFile and populates schema information */
  private DBCatalog() {
    tables = new HashMap<>();
    aliasmap = new ConcurrentHashMap<>();
    aliasSchema = new ConcurrentHashMap<>();
  }

  /**
   * Instance getter for singleton pattern, lazy initialization on first invocation
   *
   * @return unique DB catalog instance
   */
  public static DBCatalog getInstance() {
    if (db == null) {
      db = new DBCatalog();
    }
    return db;
  }

  /**
   * Sets the data directory for the database catalog.
   *
   * @param directory: The input directory.
   */
  public void setDataDirectory(String directory) {
    try {
      dbDirectory = directory;
      BufferedReader br = new BufferedReader(new FileReader(directory + "/schema.txt"));
      String line;
      while ((line = br.readLine()) != null) {
        String[] tokens = line.split("\\s");
        String tableName = tokens[0];
        ArrayList<Column> cols = new ArrayList<Column>();
        for (int i = 1; i < tokens.length; i++) {
          cols.add(new Column(new Table(null, tableName), tokens[i]));
        }
        tables.put(tokens[0], cols);
      }
      br.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * Gets path to file where a particular table is stored
   *
   * @param tableName table name
   * @return file where table is found on disk
   */
  public File getFileForTable(String tableName) {
    if (useAlias) {
      tableName = aliasmap.get(tableName);
      return new File(dbDirectory + "/data/" + tableName);
    }
    return new File(dbDirectory + "/data/" + tableName);
  }

  /**
   * Given tableName, get_Table Checks first if our query is using aliases and if so returns the
   * schema of the table corresponding to the alias. Otherwise, returns the schema directly from
   * tables
   *
   * @param tableName is an alias or actual table name
   * @return schema for table corresponding to tableName
   */
  public ArrayList<Column> get_Table(String tableName) {
    if (useAlias) {
      return aliasSchema.get(tableName);
    }
    return tables.get(tableName);
  }

  /**
   * return the variable corresponding to if we are using aliases
   *
   * @return variable useAlias
   */
  public boolean getUseAlias() {
    return useAlias;
  }

  /**
   * Adds the name of a table and its alias to aliasmap, and adds the alias name, with the schema of
   * the corresponding table in aliasSchema
   *
   * @param tableName is actual table name
   * @param alias is the alias for the table
   */
  public void setTableAlias(String tableName, String alias) {
    // ArrayList<Column> columns =tables.get(tableName);
    aliasmap.put(alias, tableName);
    ArrayList<Column> columns = new ArrayList<>();
    for (Column c : tables.get(tableName)) {
      columns.add(new Column(new Table(alias, tableName), c.getColumnName()));
    }
    aliasSchema.put(alias, columns);
  }

  /**
   * Reinitializes the values of aliasmap and aliasSchema so that the next query can be read without
   * having information from previous query
   */
  public void resetDB() {
    aliasmap.clear();
    aliasSchema.clear();
  }

  /**
   * Sets useAlias if we are using aliases in the query
   *
   * @param is true when we use aliases, false otherwise
   */
  public void setUseAlias(boolean is) {
    useAlias = is;
  }

  /**
   * If we are using aliases, getTableName treats name as an alias and returns the corresponding
   * table's name. Otherwise, name is just the table's name itself.
   *
   * @param name is an alias or table name
   * @return the name of the table
   */
  public String getTableName(String name) {
    if (useAlias) {
      return aliasmap.get(name);
    }
    return name;
  }

  /**
   * Creates TupleReader for a table
   *
   * @param path is name/alias of table
   * @return TupleReader for table
   * @throws FileNotFoundException
   */
  public TupleReader getReader(String path) throws FileNotFoundException {
    return new TupleReader(new File(path));
  }

  public void config_file(String input_dir) {
    try {
      String txt = "/plan_builder_config.txt";
      String config = input_dir + txt;
      BufferedReader br = new BufferedReader(new FileReader(config));
      String l1 = br.readLine();
      String l2 = br.readLine();
      // first line is join method; 0 for TNLJ, 1 for BNLJ, 2 for SMJ
      // if BNL, second number on line is number of buffer pages
      String[] line1 = l1.split("\\s");
      int join = Integer.parseInt(line1[0]);
      TNLJ = join == 0;
      BNLJ = join == 1;
      SMJ = join == 2;
      if (BNLJ) {
        BNLJ_buff = Integer.parseInt(line1[1]);
      }

      // 0 for in-memory, 1 for external
      // if external, second int is number of buffer pages >=3
      String[] line2 = l2.split("\\s");
      int sort = Integer.parseInt(line2[0]);
      sort_type = sort;
      if (sort_type == 1) sort_buff = Integer.parseInt(line2[1]);
      br.close();

    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  public boolean if_BNLJ() {
    return BNLJ;
  }

  public boolean if_TNLJ() {
    return TNLJ;
  }

  public boolean if_SMJ() {return SMJ;}
  public int blockSize() {
    return BNLJ_buff;
  }
}
