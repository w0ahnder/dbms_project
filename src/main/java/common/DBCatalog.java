package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.Alias;
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
  //key is alias, and value is table name
  private final HashMap<String, String> aliasmap;

  private String dbDirectory;

  /** Reads schemaFile and populates schema information */
  private DBCatalog() {
    tables = new HashMap<>();
    aliasmap = new HashMap<>();
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
    return new File(dbDirectory + "/data/" + tableName);
  }

  public ArrayList<Column> get_Table(String tableName) {
    System.out.println("getting table ");
    return tables.get(tableName);
  }
  public ArrayList<Column> get_Table(String name, boolean if_alias) {
    if(if_alias){
      String tableName = aliasmap.get(name);
      return tables.get(tableName);
    }
    return tables.get(name);
  }

  public File getFileForTable(String name, boolean if_alias) {
    if(if_alias){
      String tableName = aliasmap.get(name);
      return new File(dbDirectory + "/data/" + tableName);
    }
    return new File(dbDirectory + "/data/" + name);
  }

  public void setTableAlias(String tableName, String alias){
    //ArrayList<Column> columns =tables.get(tableName);
    aliasmap.put(alias, tableName);
    for(Column c: tables.get(tableName)){
        c.getTable().setSchemaName(alias);

    }
  }



  public void getAllAliases(){

  }
  public ArrayList<Column> getTablewithAlias(String alias) {
    return tables.get(aliasmap.get(alias));
  }
}
