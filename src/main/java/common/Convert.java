package common;

import java.io.*;

public class Convert {

  private String path;
  private TupleReader reader;
  private PrintStream printStream;

  public Convert(String path, PrintStream ps) throws FileNotFoundException {
    this.path = path;
    reader = new TupleReader(new File(this.path));
    printStream = ps;
  }

  public void bin_to_human() throws IOException {
    Tuple t = reader.read();
    while (t != null) {
      printStream.println(t);
      t = reader.read();
    }
    reader.reset();
  }
}
