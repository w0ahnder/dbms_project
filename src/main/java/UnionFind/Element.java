package UnionFind;

import java.util.ArrayList;
import java.util.HashSet;

import common.DBCatalog;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

public class Element {
  Integer upper;

  Integer lower;

  Integer equality;

  HashSet<String> attributes;

  Element(Integer upper_bound, Integer lower_bound, Integer eq, String attr) {
    this.upper = upper_bound;
    this.lower = lower_bound;
    this.equality = eq;
    this.attributes = new HashSet<>();
    attributes.add(attr);
  }

  void setUpper(Integer val) {
    if (this.upper == null) {
      this.upper = val;
    } else {
      this.upper = Integer.min(val, this.upper);
    }
  }

  void setLower(Integer val) {
    if (this.lower == null) {
      this.lower = val;
    } else {
      this.lower = Integer.max(val, this.lower);
    }
  }

  void setEquality(Integer val) {
    this.equality = val;
  }

  Element merge(Element elem) {
    this.attributes.addAll(elem.attributes);
    if (this.equality == null) {
      setEquality(elem.equality);
    }

    if (this.equality != null) {
      setLower(this.equality);
      setUpper(this.equality);
    } else {
      if (this.lower == null) {
        setLower(elem.lower);
      }
      if (this.lower != null && elem.lower != null) {
        setLower(Integer.max(this.lower, elem.lower));
      }

      if (this.upper == null) {
        setUpper(elem.upper);
      }
      if (this.upper != null && elem.upper != null) {
        setUpper(Integer.min(this.upper, elem.upper));
      }
    }
    return this;
  }

  public String toString() {
    return " The attributes are: "
        + attributes.toString()
        + " The lower bound is "
        + lower
        + " The upper bound is "
        + upper
        + " The equality constraint is "
        + equality;
  }

  public ArrayList<Expression> generateExpression() {
    ArrayList<Expression> expressions = new ArrayList<>();
    for (String attr : attributes) {
      String[] table_col = attr.split("\\.");
      String table = table_col[0].trim();
      String col = table_col[1].trim();
      String alias = null;
      if (DBCatalog.getInstance().getUseAlias()) {
        alias = table;
        table = DBCatalog.getInstance().getTableName(alias);//actual table name
      }

      Column c = new Column(new Table(alias, table), col);
      if (equality != null) {
        Expression expr =
            (new EqualsTo().withLeftExpression(c))
                .withRightExpression(new LongValue(equality));
        expressions.add(expr);
      } else {
        if (lower != null) {
          Expression expr =
              (new GreaterThanEquals().withLeftExpression(c))
                  .withRightExpression(new LongValue(lower));
          expressions.add(expr);
        }
        if (upper != null) {
          Expression expr =
              (new MinorThanEquals().withLeftExpression(c))
                  .withRightExpression(new LongValue(upper));
          expressions.add(expr);
        }
      }
    }
    return expressions;
  }
}
