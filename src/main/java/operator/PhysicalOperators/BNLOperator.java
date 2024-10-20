package operator.PhysicalOperators;

import common.DBCatalog;
import common.SelectVisitor;
import common.Tuple;
import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

public class BNLOperator extends Operator{
    public Operator left;
    public Operator right;
    public int block;
    public int cols;
    public int num_tup;
    public ArrayList<Tuple> buffer;
    public boolean filled = false;
    public int reads;
    public int tot_elements;
    public Expression condition;
    public Tuple s;
    public Tuple check;
    public Tuple check_t;

    public BNLOperator(ArrayList<Column> schema, Operator left, Operator right, Expression cond){
        super(schema);
        this.left = left;
        this.right = right;
        condition = cond;
        cols = schema.size();
        block = DBCatalog.getInstance().blockSize();
        num_tup = ((4096) / (cols *4)) * block;
        reads=0; //havent called get next tuple yet
        tot_elements = 0; //keep track of how many tuples we have in B bc sometimes the block may not be filled up
        buffer  =new ArrayList<>();
        //fill();//get block B from left operators

    }

    public void reset(){
        reads=0;
        tot_elements=0;
        filled=false;
        left.reset();
        right.reset();
        fill();
    }

    //get the total number of elements that can fit in the buffer where each page is 4096 bytes
    // (4096/ (num of col * 4) ) * block = total number of tuples we should have in the block B
    //intiialize the block
    public void fill(){
        s = right.getNextTuple();
        reads = 0;
        tot_elements = 0;
        buffer.clear();
        //left.reset();
        Tuple t;
        while( (tot_elements<num_tup) && (t = left.getNextTuple())!=null){
            buffer.add(t);
            tot_elements++;
            check_t = t;
        }
        filled = tot_elements>0; //if any elements were read
    }


    //if 2 is another BNL, then when we reset, we want to get the next block of pages, not
    // 1. FOR each B
    // 2.     For each s in INNER
    // 3.           FOR each r in B
    //what happens when right is BNL???????????
    public Tuple getNextTuple(){
        //System.out.println(condition.toString());
        if(!filled)
            fill();
        //keep same s until we exit 3. =>> (when reads>=tot_elements, we then get the next s)
        //when s= null, we exit 2; get the next set of tuples worth B pages (do we consider the metadata as part of the block size)
        while(s!=null){
            if(filled){
                if(reads < tot_elements) { //still have more elements in block to read
                    Tuple r = buffer.get(reads);
                    ArrayList<Integer> elements = new ArrayList<>();
                    elements.addAll(r.getAllElements());
                    elements.addAll(s.getAllElements());
                    Tuple curr = new Tuple(elements);
                    reads++;
                    if (this.condition == null) {
                        return curr;
                    }
                    SelectVisitor sv = new SelectVisitor(curr, concatSchema(), this.condition);
                    if (sv.evaluate_expr()) {
                        return curr;
                    }

                }
                //all tuples in the block were read, now get next s and keep going through same block
                else{
                    check = s;
                    s = right.getNextTuple();
                    reads=0;
                }
            }//filled = false means Tuple reader doesn't return any tuples; exhausted all of them
            else{
                return null;
            }
        }
        //when s is null, we get the next block and go again
        right.reset();
        fill();
        return this.getNextTuple();
    }


    public ArrayList<Column> concatSchema() {
        ArrayList<Column> conc = new ArrayList<Column>();
        conc.addAll(left.getOutputSchema());
        conc.addAll(right.getOutputSchema());
        return conc;
    }

}
