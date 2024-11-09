package common;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tree.BTree;
import tree.BulkLoad;

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
  private boolean fullScan;
  private boolean buildIndex = false;
  private boolean evalQuery = false;
  private HashMap<String, Tuple> index_info = new HashMap<>();
  ; // <table.col, (clustered, order)>
  private HashMap<String, File> availableIndex = new HashMap<>();
  ; // <table.col, file for the index>

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

  /**
   * Parses the config file to determine the type of join and the block size (if applicable) and the
   * type of sort to use and number of buffer pages for it if external sort
   *
   * @param input_dir is the path for the config file
   */
  public void config_file(String input_dir) {
    try {
      String txt = "/plan_builder_config.txt";
      String config = input_dir + txt;
      BufferedReader br = new BufferedReader(new FileReader(config));
      String l1 = br.readLine();
      String l2 = br.readLine();
      String l3 = br.readLine();
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

      String[] line3 = l3.split("\\s");
      fullScan = Integer.parseInt(line3[0]) == 0; // if we do a full scan then it is 0, no index
      br.close();

    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * @return sort_buff is the number of buffer pages for sorting if external
   */
  public int getSortBuff() {
    return sort_buff;
  }

  /**
   * @return BNLJ is true if the loop type is a block nested
   */
  public boolean if_BNLJ() {
    return BNLJ;
  }

  /**
   * @return TNLJ is true if the loop type is a tuple nested
   */
  public boolean if_TNLJ() {
    return TNLJ;
  }

  /**
   * @return SMJ is true if the loop type is a sort merge join
   */
  public boolean if_SMJ() {
    return SMJ;
  }

  /**
   * @return BNLJ_buff is the size of the block for a block nested loop
   */
  public int blockSize() {
    return BNLJ_buff;
  }

  /**
   * @return true if we have to build an index, false if provided
   */
  public boolean ifBuild() {
    return buildIndex;
  }

  /**
   * @return true if we have to do a full scan instead of using an index
   */
  public boolean isFullScan() {
    return fullScan;
  }

  /**
   * @return true if we have to actually evaluate query
   */
  public boolean isEvalQuery() {
    return evalQuery;
  }

  /**
   * Reads interpreter configuration file
   *
   * @param path is path to interpreter configuration file
   * @return
   */
  public void setInterpreter(String path) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(path));
      String inputDir = br.readLine(); // contains db, plan_builder, queries
      setDataDirectory(inputDir + "/db");
      String outputDir = br.readLine();
      String tempDir = br.readLine();
      buildIndex = Integer.parseInt(br.readLine()) == 1;
      evalQuery = Integer.parseInt(br.readLine()) == 1;
      // 1,0 means build index, but dont run query
      // 1,1 means build index, run queries
      // 0,1 means don't build(index given), and run query
      br.close();
    } catch (Exception e) {
      System.out.println("Failed to read Interpreter Configuration File");
    }
  }

  /**
   * We use this when fullScan is false We use this to find out for which tables we have available
   * indexes, for which we have to build an index. Each line has tablename attribute clustered order
   */
  public void processIndex() {
    try {
      BufferedReader br = new BufferedReader(new FileReader(dbDirectory + "/index_info.txt"));
      String str;
      // has <table.col, (clustered, order)>
      index_info = new HashMap<>();
      // if we build an index has <table.col, tree>
      // HashMap<String, BTree> trees = new HashMap<>();
      // <table.col, file for index relation>
      availableIndex = new HashMap<>();

      while ((str = br.readLine()) != null) {
        String[] splits = str.split("\\s");
        String table = splits[0];
        System.out.println("Table: " + table + ".");
        String attribute = splits[1];
        System.out.println("Attribute: " + attribute + ".");
        int clust = Integer.parseInt(splits[2]);
        System.out.println("Clust: " + clust + ".");
        int order = Integer.parseInt(splits[3]);
        System.out.println("order: " + order + ".");
        // can name file table.col and catch a file not found exception

        // find index of attribute in table schema
        int cindex = colIndex(table, attribute);
        ArrayList<Integer> elements = new ArrayList<>();
        elements.add(clust);
        elements.add(order);
        index_info.put(table + "." + attribute, new Tuple(elements));
        // if index not available, have to build
        if (buildIndex) {
          boolean clustered = clust == 1; // 1 if clustered
          File relation = new File(dbDirectory + "/data/" + table);
          if (clustered) {
            String tablePath = dbDirectory + "/data/" + table;
            BulkLoad.sortRelation(table, tablePath, attribute, tablePath);
          }

          BulkLoad bl = new BulkLoad(relation, order, cindex, clustered);
          bl.load();
          BTree btree = bl.getTree();
          String p = dbDirectory + "/indexes/" + table + "." + attribute;
          btree.tree_to_file(p); // serialize the tree and write to File
          availableIndex.put(table + "." + attribute, new File(p));
        } else {
          File givenIndex = new File(dbDirectory + "/indexes/" + table + "." + attribute);
          availableIndex.put(table + "." + attribute, givenIndex);
        }
        // if index available have to set correct path

      }
      br.close();
    } catch (Exception e) {
      System.out.println("Failed to read Interpreter Configuration File");
    }
  }

  /**
   * Returns the indexed File for table.col if it exists
   *
   * @param table that we want to check if there is an index for
   * @param col in table that we want to check index for
   * @return the File for the index if it exists, null o/w
   */
  public File getAvailableIndex(String table, String col) {
    String indexName = table + "." + col;
    if (availableIndex.containsKey(indexName)) {
      return availableIndex.get(indexName);
    }
    return null;
  }

  /**
   * Return the column name part after "table." if it exists
   *
   * @param table that we want to check if there is an index for
   * @return
   */
  public String getAvailableIndexColumn(String table) {
    String prefix = table + ".";
    for (String indexName : availableIndex.keySet()) {
      if (indexName.startsWith(prefix)) {
        return indexName.substring(prefix.length());
      }
    }
    return null; // Return null if no match found
  }

  /**
   * Returns ta Tuple containing clustered = 1 or 0, and order of the index for an index on
   * table.col
   *
   * @param table of the index we want to check
   * @param col in table that we want to get index ifo for
   * @returns Tuple with (int clustered, order)
   */
  public Tuple getClustOrd(String table, String col) {
    String indexName = table + "." + col;
    if (index_info.containsKey(indexName)) {
      return index_info.get(indexName);
    }
    return null;
  }

  /**
   * Finds the index of a column in a table's schema
   *
   * @param table is the table whose column index we want to find
   * @param col the column to find the index of
   * @return index of col in table
   */
  public int colIndex(String table, String col) {
    ArrayList<Column> cols = tables.get(table);
    for (int i = 0; i < cols.size(); i++) {
      if (cols.get(i).getColumnName().equalsIgnoreCase(col)) {
        return i;
      }
    }
    System.out.println("Column index not found");
    return -1;
  }

  /*************************   Benchmarking Functions    *************/
  /******Functions used to set parameters in order to do Benchmarking ***********/

  /**
   * @param jointype is to set the type of join
   */
  public void setLoop(int jointype) {
    TNLJ = jointype == 0;
    BNLJ = jointype == 1;
    SMJ = jointype == 2;
  }

  /**
   * sets the block size for a block nested loop for benchmarking
   *
   * @param b is the block size for the block nested loop join
   */
  public void setBNLbuff(int b) {
    BNLJ_buff = b;
  }

  /**
   * @param b is to set the number of buffer pages when using external sort
   */
  public void setSortBuff(int b) {
    sort_buff = b;
  }
}
