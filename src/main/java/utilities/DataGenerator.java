package utilities;

import common.Tuple;
import common.TupleWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

public class DataGenerator {
  // range we want each value in a row to fall between
  int range;

  // total number of tuples we want
  int total;

  // how many columns in a tuple
  int tupleSize;

  // all the generated tuple
  ArrayList<Tuple> data;
  Random random;
  public String out;

  public DataGenerator(int range, int size, int tupleSize, String out) throws IOException {
    this.range = range;
    this.total = size;
    this.tupleSize = tupleSize;
    this.random = new Random();
    this.out   = out;
    generateTuples();
  }

  public void generateTuples() throws IOException {
    TupleWriter tw = new TupleWriter(out);
    for (int i = 0; i < total; i ++) {
      ArrayList<Integer> elements = new ArrayList<>();

      for (int j = 0; j < tupleSize; j++) {
        int num = random.nextInt(range) + 1;
        elements.add(num);
      }
      Tuple t = new Tuple(elements);
      tw.write(t);
      elements.clear();
    }
    tw.close();

  }


  public ArrayList<Tuple> allTuples() {
    return data;
  }
}
