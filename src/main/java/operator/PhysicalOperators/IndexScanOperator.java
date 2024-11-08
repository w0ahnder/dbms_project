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
public class IndexScanOperator extends ScanOperator {
  public DBCatalog db;
  public BufferedReader br;
  public String table_path;
  public TupleReader reader;

  // path is path for table
  /**
   * @param outputSchema
   * @param table_file
   * @param ind index of the indexed column
   * @param clustered return 0/1
   * @param low
   * @param high
   * @param index_file the file from the dbcatalog method
   * @throws FileNotFoundException
   */
  public IndexScanOperator(
      ArrayList<Column> outputSchema,
      String table_path,
      File table_file,
      int ind,
      boolean clustered,
      int low,
      int high,
      File index_file)
      throws FileNotFoundException {
    super(outputSchema, table_path);
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
      // return new Tuple (br.readLine());
      return t;
    } catch (Exception e) {
      return null;
    }
  }
}
