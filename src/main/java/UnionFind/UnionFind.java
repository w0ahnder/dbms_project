package UnionFind;

import java.util.ArrayList;
import net.sf.jsqlparser.expression.Expression;

public class UnionFind {
  ArrayList<Element> elements;

  public UnionFind() {
    this.elements = new ArrayList<>();
  }

  public Element findElement(String attr) {
    for (Element e : elements) {
      if (e.attributes.contains(attr)) {
        return e;
      }
    }
    return new Element(null, null, null, attr);
  }

  public void updateElementsList(Element elem) {
    this.elements.add(elem); // remember to call when you do a setfunction and mergeElements
  }

  public void removeElementFromList(Element elem) {
    this.elements.remove(elem); // remember to call when you do a findfunction
  }

  public void setLower(String attr, Integer lower) {
    Element elem = findElement(attr);
    removeElementFromList(elem);
    elem.setLower(lower);
    updateElementsList(elem);
  }

  public void setUpper(String attr, Integer higher) {
    Element elem = findElement(attr);
    removeElementFromList(elem);
    elem.setUpper(higher);
    updateElementsList(elem);
  }

  public void setEquality(String attr, Integer eq) {
    Element elem = findElement(attr);
    removeElementFromList(elem);
    elem.setUpper(eq);
    elem.setLower(eq);
    elem.setEquality(eq);
    updateElementsList(elem);
  }

  public void mergeElements(String attr_1, String attr_2) {
    Element left = findElement(attr_1);
    Element right = findElement(attr_2);
    removeElementFromList(left);
    removeElementFromList(right);
    Element merged = left.merge(right);
    updateElementsList(merged);
  }

  public ArrayList<Expression> generateExpr() {
    ArrayList<Expression> expr = new ArrayList<>();
    for (Element e : elements) {
      ArrayList<Expression> elem_expr = e.generateExpression();
      expr.addAll(elem_expr);
    }
    return expr;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Element e : elements) {
      builder.append(e.toString());
    }
    return builder.toString();
  }
}
