package common;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import operator.LogicalOperators.LogicalOperator;
import operator.LogicalOperators.SelectLogOperator;
import tree.Index;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectPlan {
    ArrayList<Expression> expressions;
    String tableName;
    HashMap<String, Integer[]> colMinMax = new HashMap<>();
    int numTuples;
    int numCols;
    TableStats stats;
    HashMap<String, Double> colIndexCost = new HashMap<>();
    HashMap<String, Expression> indexedExpressions =  new HashMap<>();
    ArrayList<Expression> unindexedExpressions = new ArrayList<>();
    LogicalOperator scan;
    String tablePath;
    ArrayList<Column> schema;

    //if we have indexed expressions, then we can check if indexes are better than full scan
    //have to check if we have an index for the column tho
    //if expressions can't be indexed, then full scan?
    public SelectPlan(String tableName, ArrayList<Column> schema, String tablePath, ArrayList<Expression> expressions, LogicalOperator scan) {
        this.expressions = expressions;
        this.tableName = tableName;
        stats = DBCatalog.getInstance().getTableStats(tableName);
        numTuples = DBCatalog.getInstance().getTableStats(tableName).getNumTuples();
        numCols = DBCatalog.getInstance().getTableStats(tableName).numCols();
        this.scan  =scan;
        this.tablePath = tablePath;
        this.schema = schema;
    }

    public void plan(Expression expression){
        //each expression in here will have the table in it?
        ArrayList<Expression> expressions = getAndExpressions(expression);
        for(Expression exp: expressions) {
            Expression left = ((BinaryExpression) exp).getLeftExpression();
            Expression right = ((BinaryExpression) exp).getRightExpression();
            boolean isLeftCol = left instanceof Column;
            boolean isRightCol = right instanceof Column;
            boolean checkIndex = false;
            String colToCheck = null;
            if ( isLeftCol && !isRightCol) { // table.col = long
                String[] left_col = ((Column) left).toString().split("\\.");
                colToCheck = left_col[1];
            }
            else if (!isLeftCol && isRightCol) { // long =  table.col
                String[] right_col = ((Column) right).toString().split("\\.");
                colToCheck = right_col[1];
            }
            else{
                unindexedExpressions.add(exp);
                continue;
            }
                boolean tableHasIndex = DBCatalog.getInstance().getAvailableIndexColumn(tableName)!=null;
                boolean colHasIndex = DBCatalog.getInstance().getAvailableIndex(tableName, colToCheck)!=null;
                if(tableHasIndex && colHasIndex) {
                    //means we have an available index, now check if expression can be indexed on it
                    ScanVisitor visitor = new ScanVisitor(exp, tableName + "." + colToCheck);
                    if (visitor.evaluate_expr()) {
                        Integer low = visitor.getLowKey();
                        Integer high = visitor.getHighKey();
                        colRange(colToCheck, low, high);

                        if(indexedExpressions.containsKey(colToCheck)){
                            ArrayList<Expression> colExp = new ArrayList<>();
                            colExp.add(exp);
                            Expression currExpression  = indexedExpressions.get(colToCheck);
                            colExp.add(currExpression);
                            indexedExpressions.put(colToCheck, createAndExpression(colExp));
                        }
                        else{
                            indexedExpressions.put(colToCheck, exp);
                        }
                    }
                    else{
                        unindexedExpressions.add(exp);
                    }
                }
        }
        //optimalPlan();
    }

    public LogicalOperator optimalPlan(){
        int pages = ((numTuples*numCols*4)/4096) +1;//
        double indexScan = pages;
        for(String col: colMinMax.keySet()){
            /***computing reduction factor*/
            Integer[] colRange = stats.getColumnInfo(col);
            int rangeSize = colRange[1] - colRange[0];

            Integer[] selectRange =  colMinMax.get(col);
            double redFactor = (double) (selectRange[1] - selectRange[0]) /rangeSize;
            redFactor = redFactor<1 ? 1.0 : redFactor;
            //calc cost for clustered and unclustered indexes
            Tuple index_info = DBCatalog.getInstance().getIndexInfo(tableName, col);
            boolean clustered = index_info.getElementAtIndex(0) ==1;
            //tree_height + pages*reduction factos
            if(clustered){
                indexScan = 3 + pages*redFactor;
            }
            else{
                int numLeaves = stats.getNumLeaves(col);
                indexScan = 3 + (numLeaves*numTuples)/redFactor;
            }
            //now keep track of cost for each index on available column
            colIndexCost.put(col, indexScan);
        }

        if(colIndexCost.size() ==0){
            //means we just do a full scan
            ArrayList<Expression>  allExp = new ArrayList<>();
            allExp.addAll(unindexedExpressions);
            for(String col: indexedExpressions.keySet()){
                allExp.add((indexedExpressions.get(col)));
            }
            Expression finalExp = createAndExpression(allExp);
            System.out.println("Just doing regular full scan");
            return new SelectLogOperator(finalExp, scan);
        }
        String minCol = (String) colIndexCost.keySet().toArray()[0];
        double minCost = colIndexCost.get(minCol);
        for(String c: colIndexCost.keySet()){
            double curr = colIndexCost.get(c);
            if(curr<minCost){
                minCost = curr;
                minCol = c;
            }
        }
        //now we have to separate all of the expressions into the one we can use
        //an index for (indexed expressions)
        //and the ones we cant unindexed exp
        Expression unindexed = combineUnindexed(minCol);
        return createOP(unindexed, minCol);


        //actually want to return the column we want to index on and
    }
    public LogicalOperator createOP(Expression unindexed, String col){
        Expression indexedExpr = indexedExpressions.get(col);
        Expression unIndexedExpr = unindexed;
        int low = colMinMax.get(col)[0];
        int high = colMinMax.get(col)[1];
        Integer ind = DBCatalog.getInstance().colIndex(tableName, col);
        boolean clustered =
                DBCatalog.getInstance().getClustOrd(tableName, col).getElementAtIndex(0) == 1;
        File indexFile = DBCatalog.getInstance().getAvailableIndex(tableName, col);
        return new SelectLogOperator(indexedExpr, unIndexedExpr, schema, tablePath, tableName,
                ind, clustered, low, high, indexFile, scan);
    }



    public Expression combineUnindexed(String indexedCol){
        ArrayList<Expression> unindexed = new ArrayList<>();
        for(String col: indexedExpressions.keySet()){
            if(!col.equalsIgnoreCase(indexedCol)){
                unindexed.add(indexedExpressions.get(col));
            }
        }
        unindexed.addAll(unindexedExpressions);
        return createAndExpression(unindexed);
    }
    public void colRange(String col, int low, int high){
        if(!colMinMax.containsKey(col)){
            Integer [] range = new Integer[2];
            range[0] = low;
            range[1] = high;
            colMinMax.put(col, range);
        }
        else{
            Integer [] range = colMinMax.get(col);
            range[0] = Math.max(range[0], low);
            range[1] = Math.min(range[1], high);
            colMinMax.put(col, range);
        }
    }

    private Expression createAndExpression(List<Expression> expressions) {
        if (expressions.size() < 1) {
            return null;
        } else if (expressions.size() == 1) {
            return expressions.get(0);
        }
        AndExpression andExpression = new AndExpression(expressions.get(0), expressions.get(1));
        expressions.remove(0);
        expressions.remove(0);
        while (!expressions.isEmpty()) {
            andExpression = new AndExpression(andExpression, expressions.get(0));
            expressions.remove(0);
        }
        return andExpression;
    }


    public ArrayList<Expression> getAndExpressions(Expression where) {
        ArrayList<Expression> ands = new ArrayList<>();
        while (where instanceof AndExpression) {
            ands.add(((AndExpression) where).getRightExpression());
            where = ((AndExpression) where).getLeftExpression();
        }
        ands.add(where);
        return ands;
    }
}
