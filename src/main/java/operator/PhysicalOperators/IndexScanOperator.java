package operator.PhysicalOperators;

import common.DBCatalog;
import common.Tuple;
import common.TupleReader;
import net.sf.jsqlparser.schema.Column;
import tree.BTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class IndexScanOperator extends Operator {

    int index;

    boolean isClustered;

    int lowKey;

    int highKey;

    public DBCatalog db;
    public String table_path;
    public TupleReader reader;

    BTree indexTree;

    int page_size = 4096;

    public String indexFilePath;

    public File indexFile;
    public IndexScanOperator(ArrayList<Column> outputSchema, String path, int ind, boolean clustered, int low, int high, BTree tree) throws FileNotFoundException {
        super(outputSchema);
        this.index = ind;
        this.isClustered = clustered;
        this.lowKey = low;
        this.highKey = high;
        this.indexTree = tree;
        this.indexFilePath = indexTree.makeIndexFile();
        db = DBCatalog.getInstance();
        table_path = path;
        reader = DBCatalog.getInstance().getReader(path);
        indexFile = new File(indexFilePath);
    }

    @Override
    public void reset() {

    }

    @Override
    public Tuple getNextTuple() {
        return null;
    }

}
