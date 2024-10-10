package utilities;

import common.Tuple;
import operator.PhysicalOperators.Operator;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private static Logger instance;
    private BufferedWriter writer;

    private Logger() {
        try {
            writer = new BufferedWriter(new FileWriter("debug.txt", true));
        } catch (IOException e) {
            System.err.println("Error initializing log file: " + e.getMessage());
        }
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(Operator op) {
        try {
            Tuple t = op.getNextTuple();
            while ( t != null) {
                writer.write(t.toString());
                writer.newLine();
                writer.flush();
                t = op.getNextTuple();
            }

        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing log file: " + e.getMessage());
        }
    }
}

