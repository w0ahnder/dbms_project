package operator;

import common.DBCatalog;
import common.Tuple;
import java.io.*;
import java.util.ArrayList;
import net.sf.jsqlparser.schema.Column;

public class SelectOperator extends Operator{
    public SelectOperator(ArrayList<Column> outputSchema, String path){

        super(outputSchema);
    }
    public void reset(){}
    public Tuple getNextTuple(){
        return null;
    }
}
