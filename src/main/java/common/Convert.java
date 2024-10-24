package common;

import java.io.*;

public class Convert {

  private String path;
  private TupleReader reader;
  private PrintStream printStream;

  /**
   *  Class to convert binary files into Human readable versions
   * @param path is the bianry file to read
   * @param ps is the file to write the human readable version
   * @throws FileNotFoundException
   */
  public Convert(String path, PrintStream ps) throws FileNotFoundException {
    this.path = path;
    reader = new TupleReader(new File(this.path));
    printStream = ps;
  }

  /**
   * Converts the binary file at path into a human reader version
   * @throws IOException
   */
  public void bin_to_human() throws IOException {
    Tuple t = reader.read();
    while (t != null) {
      printStream.println(t);
      t = reader.read();
    }
    reader.reset();
  }

  /**
   * This function is used to test the reset index function in the Tuple Reader. It resets to index and prints the
   * tuple at that index
   * @param i is the index to reset the binary file to
   * @throws IOException
   */
  public void bin_to_human_reset(int i) throws IOException {
    int count = 0;
    Tuple t = reader.read();
    while (count < i) {
      t = reader.read();
      count++;
    }
    reader.reset(i);
    t = reader.read();
    printStream.println(t);
  }
}
