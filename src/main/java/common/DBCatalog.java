package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

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
  // key is alias, and value is table name
  private final HashMap<String, String> aliasmap;
  private final HashMap<String, ArrayList<Column> >aliasSchema;
  private static boolean useAlias = false;

  private String dbDirectory;

  /** Reads schemaFile and populates schema information */
  private DBCatalog() {
    tables = new HashMap<>();
    aliasmap = new HashMap<>();
    aliasSchema = new HashMap<>();
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
   if(useAlias){
     tableName = aliasmap.get(tableName);
     return new File(dbDirectory + "/data/" + tableName);
   }
    return new File(dbDirectory + "/data/" + tableName);
  }

  /**
   * gives you the name of the table where tableName is either an alias
   * or the actual table name
   * */
  public ArrayList<Column> get_Table(String tableName) {
    if(useAlias){
      return aliasSchema.get(tableName);
    }
    System.out.println("getting table ");
    return tables.get(tableName);
  }
  public boolean getUseAlias(){
    return useAlias;
  }
  /**
   * Adds (alias, tableName) to a hashmap, and updates schemaName
   * for all columns in the table
   * */
  public void setTableAlias(String tableName, String alias) {
    // ArrayList<Column> columns =tables.get(tableName);
    aliasmap.put(alias, tableName);
    ArrayList<Column> columns = new ArrayList<>();
    for(Column c :tables.get(tableName)){
      columns.add(new Column(new Table(alias, tableName), c.getColumnName()));
    }
    aliasSchema.put(alias, columns);
  }

  public void setUseAlias(boolean is){
    useAlias = is;
  }

  public String getTableName(String name){
    if(useAlias){
      return aliasmap.get(name);
    }
    return name;
  }

}
//get all aliases
//to support self joins create function to take alias and return new table
//with same schema as before