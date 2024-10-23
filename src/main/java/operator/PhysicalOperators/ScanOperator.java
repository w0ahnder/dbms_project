package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import common.TupleReader;
import java.io.*;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

/**
 * Scan operator supports queries that are full table scans know which base table is it scanning
 * Column constructor Column(Table table, String columnName) Table(String schemaName, String name)
 * what is difference between the two names construct a Scan operator for the table in the fromItem
 */
public class ScanOperator extends Operator {
  public DBCatalog db;
  public BufferedReader br;
  public String table_path;
  public TupleReader reader;

  // path is path for table
  public ScanOperator(ArrayList<Column> outputSchema, String path) throws FileNotFoundException {
    super(outputSchema);
    db = DBCatalog.getInstance();
    table_path = path;
    // br = new BufferedReader(new FileReader(table_path));
    reader = DBCatalog.getInstance().getReader(path);
  }

  /** close the Buffered Reader after we reach the end of the file */
  public void reset() {
    try {
      reader.reset();
      // br.close();
      // br = new BufferedReader(new FileReader(table_path));
    } catch (Exception e) {
      return;
    }
  }

  /**
   * keeps the getting the next record from the file, until we reach the end
   *
   * @return Tuple that corresponds to the values in a record from the file at table_path
   */
  public Tuple getNextTuple() {
    try {
      Tuple t = reader.read();
      System.out.println("scan output");
      System.out.println(t.getAllElements());
      // return new Tuple (br.readLine());
      return t;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
