package UnionFind;

import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;

/** Union class is a collection of elements */
public class UnionFind {
  public ArrayList<Element> elements;

  public ArrayList<Expression> joins;

  public ArrayList<Expression> sameTableSelect;

  public UnionFind() {
    this.elements = new ArrayList<>();
    this.joins = new ArrayList<>();
    this.sameTableSelect = new ArrayList<>();
  }

  /** Finds an element in the collection given an atttribute. If element not found, creates one */
  public Element findElement(String attr, boolean makeNew) {
    for (Element e : elements) {
      if (e.attributes.contains(attr)) {
        return e;
      }
    }
    if (makeNew) {
      return new Element(null, null, null, attr);
    }
    return null;
  }

  /** Adds an element to the UnionFind collection */
  public void updateElementsList(Element elem) {
    this.elements.add(elem); // remember to call when you do a setfunction and mergeElements
  }

  /** Removes an element from the UnionFind collection */
  public void removeElementFromList(Element elem) {
    this.elements.remove(elem); // remember to call when you do a findfunction
  }

  /** Sets the lower bound for an element containing an attribute */
  public void setLower(String attr, Integer lower) {
    Element elem = findElement(attr, true);
    removeElementFromList(elem);
    elem.setLower(lower);
    updateElementsList(elem);
  }

  /** Sets the upper bound for an element containing an attribute */
  public void setUpper(String attr, Integer higher) {
    Element elem = findElement(attr, true);
    removeElementFromList(elem);
    elem.setUpper(higher);
    updateElementsList(elem);
  }

  /** Sets the equality contraint for an element containing an attribute */
  public void setEquality(String attr, Integer eq) {
    Element elem = findElement(attr, true);
    removeElementFromList(elem);
    elem.setUpper(eq);
    elem.setLower(eq);
    elem.setEquality(eq);
    updateElementsList(elem);
  }

  /** Merges the corresponding elements for the attributes */
  public void mergeElements(String attr_1, String attr_2) {
    Element left = findElement(attr_1, true);
    Element right = findElement(attr_2, true);
    removeElementFromList(left);
    removeElementFromList(right);
    Element merged = left.merge(right);
    updateElementsList(merged);
  }

  /** Converts the UnionFind Collection into a list of expressions */
  public ArrayList<Expression> generateExpr() {
    ArrayList<Expression> expr = new ArrayList<>();
    for (Element e : elements) {
      ArrayList<Expression> elem_expr = e.generateExpression();
      expr.addAll(elem_expr);
    }
    return expr;
  }

  public String printLogHelper(String attr) {
    StringBuilder line = new StringBuilder();
    Element elem = findElement(attr, false);
    if (elem != null) {
      line.append("[[");
      for (String att : elem.attributes) {
        line.append(att + ",");
      }
      line.append("], ");
      line.append("equals ").append(elem.equality).append(", ");
      line.append("min ").append(elem.lower).append(", ");
      line.append("max ").append(elem.upper).append(" ]");

      return line.toString();
    }
    return "";
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Element e : elements) {
      builder.append(e.toString());
    }
    return builder.toString();
  }
}
