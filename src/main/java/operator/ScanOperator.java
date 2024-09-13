package operator;

import common.DBCatalog;
import common.Tuple;
import java.io.*;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/**
 * Scan operator supports queries that are full table scans know which base table is it scanning
 * Column constructor Column(Table table, String columnName) Table(String schemaName, String name)
 * what is difference between the two names construct a Scan operator for the table in the fromItem
 */
public class ScanOperator extends Operator {
  // table we're scanning
  // private Table table;
  public DBCatalog db;
  public BufferedReader br;
  public String table_path;

  // path is path for table
  public ScanOperator(ArrayList<Column> outputSchema, String path) throws FileNotFoundException {
    super(outputSchema);
    db = DBCatalog.getInstance();
    table_path = path;
    // get the table names from the outputSchema
    // Table.setAlias(Alias alias)
    // get path to schema and load table names and columns for all into hashmap
    br = new BufferedReader(new FileReader(table_path));
  }

  public void reset() {
    try {
      br.close();
      br = new BufferedReader(new FileReader(table_path));
    } catch (Exception e) {
      return;
    }
  }

  public Tuple getNextTuple() {
    try {
      // do we account for null or empty column values
      String line = br.readLine();
      return new Tuple(line);
    } catch (Exception e) {
      reset();
      return null;
    }
  }
}
