package utilities;

import common.Tuple;
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

  public DataGenerator(int range, int size, int tupleSize) {
    this.range = range;
    this.total = size;
    this.tupleSize = tupleSize;
    this.random = new Random();
    generateTuples();
  }

  public void generateTuples() {
    for (int i = 0; i < total; i += 1) {
      ArrayList<Integer> elements = new ArrayList<>();

      for (int j = 0; i < tupleSize; i++) {
        int num = random.nextInt(range) + 1;
        elements.add(num);

      }
      data.add(new Tuple(elements));
      elements.clear();
    }

  }

  public ArrayList<Tuple> allTuples() {
    return data;
  }
}
