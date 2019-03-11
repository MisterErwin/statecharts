/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */

package de.monticore.umlsc.statechart.prettyprint;

import de.monticore.java.javadsl._ast.ASTJavaBlock;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.prettyprint.JavaDSLPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.prettyprint.TypesPrettyPrinterConcreteVisitor;
import de.monticore.types.types._ast.ASTImportStatement;
import de.monticore.types.types._ast.ASTReferenceType;
import de.monticore.types.types._ast.ASTTypesNode;
import de.monticore.umlsc.statechart._ast.*;
import de.monticore.umlsc.statechartwithjava._ast.ASTSCExpression;
import de.monticore.umlsc.statechartwithjava._ast.ASTSCInvariantContent;
import de.monticore.umlsc.statechartwithjava._ast.ASTSCStatements;
import de.monticore.umlsc.statechartwithjava._visitor.StatechartWithJavaVisitor;

public class StatechartPrettyPrinter implements StatechartWithJavaVisitor {

  protected StatechartWithJavaVisitor realThis;

  protected IndentPrinter printer;

  public StatechartPrettyPrinter() {
    this.printer = new IndentPrinter();
    this.realThis = this;
  }

  @Override
  public void setRealThis(StatechartWithJavaVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public StatechartWithJavaVisitor getRealThis() {
    return realThis;
  }

  public IndentPrinter getPrinter() {
    return this.printer;
  }

  public String prettyPrint(ASTStatechartNode node) {
    node.accept(this);
    String result = getPrinter().getContent();
    getPrinter().clearBuffer();
    return result;
  }

  public String prettyPrint(ASTTypesNode node) {
    TypesPrettyPrinterConcreteVisitor v = new TypesPrettyPrinterConcreteVisitor(new IndentPrinter());
    return v.prettyprint(node);
  }

  public String prettyprint(ASTJavaDSLNode node) {
    JavaDSLPrettyPrinter pp = new JavaDSLPrettyPrinter(new IndentPrinter());
    return pp.prettyprint(node);
  }

  @Override
  public void handle(ASTReferenceType ref) {

  }

  @Override
  public void handle(ASTSCStereotype node) {
    getPrinter().print(" << ");
    boolean first = true;
    for (ASTSCStereoValue astStereoValue : node.getValuesList()) {
      if (first) {
        astStereoValue.accept(getRealThis());
        first = false;
      } else {
        getPrinter().print(", ");
        astStereoValue.accept(getRealThis());
      }
    }
    getPrinter().print(" >> ");
  }

  @Override
  public void handle(ASTSCStereoValue node) {
    getPrinter().print(node.getName());
    if (node.isPresentValue()) {
      getPrinter().print(" = \"" + node.getValue() + "\"");
    }
  }

  @Override
  public void handle(ASTSCInternTransition node) {
    if (node.isPresentStereotype()) {
      node.getStereotype().accept(getRealThis());
      getPrinter().print(" ");
    }
    getPrinter().print("-> ");
    node.getSCTransitionBody().accept(getRealThis());
  }

  @Override
  public void handle(ASTSCTransition node) {
    if (node.isPresentStereotype()) {
      node.getStereotype().accept(getRealThis());
    }
    getPrinter().print(node.getSourceName());
    getPrinter().print(" -> ");
    getPrinter().print(node.getTargetName());
    getPrinter().print(" ");
    if (node.isPresentSCTransitionBody()) {
      node.getSCTransitionBody().accept(getRealThis());
    }
    getPrinter().println();
  }

  @Override
  public void handle(ASTSCTransitionBody node) {
    if (node.isPresentPreCondition()) {
      node.getPreCondition().accept(getRealThis());
    }
    if (node.isPresentSCEvent()) {
      if (node.isPresentPreCondition()) {
        getPrinter().print(" ");
      }
      node.getSCEvent().accept(getRealThis());
    }
    getPrinter().print(" / ");
    if (node.isPresentSCStatements()) {
      node.getSCStatements().accept(getRealThis());
      if (node.isPresentPostCondition()) {
        node.getPostCondition().accept(getRealThis());
      }
    }
  }

  @Override
  public void handle(ASTInvariant node) {
    getPrinter().print("[");
    node.getContent().accept(getRealThis());
    getPrinter().print("]");
  }

  @Override
  public void handle(ASTSCMethodCall node) {
    getPrinter().print(node.getName());
    if (node.isPresentSCArguments()) {
      node.getSCArguments().accept(getRealThis());
    }
  }

  @Override
  public void handle(ASTSCReturnStatement node) {
    getPrinter().print("return");
    if (node.isPresentSCExpression()) {
      getPrinter().print("( ");
      node.getSCExpression().accept(getRealThis());
      getPrinter().print(" )");
    }
  }

  @Override
  public void handle(ASTSCArguments node) {
    getPrinter().print("(");
    boolean first = true;
    for (ASTSCExpressionExt exp : node.getSCExpressionList()) {
      if (first) {
        exp.accept(getRealThis());
        first = false;
      } else {
        getPrinter().print(", ");
        exp.accept(getRealThis());
      }
    }
    getPrinter().print(")");
  }

  @Override
  public void handle(ASTSCAction node) {
    if (node.isPresentPreCondition()) {
      node.getPreCondition().accept(getRealThis());
    }
    if (node.isPresentSlash()) {
      getPrinter().print(" / ");
      if (node.isPresentSCStatements()) {
        node.getSCStatements().accept(getRealThis());
      }
      if (node.isPresentPostCondition()) {
        node.getPostCondition().accept(getRealThis());
      }
    }
  }

  @Override
  public void handle(ASTSCStatementsExt node) {
    if (node instanceof ASTSCStatements) {
      ((ASTSCStatements) node).accept(getRealThis());
    }
  }

  @Override
  public void handle(ASTSCExpressionExt node) {
    if (node instanceof ASTSCExpression) {
      ((ASTSCExpression) node).accept(getRealThis());
    }
  }

  @Override
  public void handle(ASTSCInvariantContentExt node) {
    if (node instanceof ASTSCInvariantContent) {
      ((ASTSCInvariantContent) node).accept(getRealThis());
    }
  }

  @Override
  public void handle(ASTSCStatements node) {
    JavaDSLPrettyPrinter pp = new JavaDSLPrettyPrinter(new IndentPrinter());
    if (node.getBlockStatement() instanceof ASTJavaBlock) {
      ASTJavaBlock b = (ASTJavaBlock) node.getBlockStatement();
      pp.handle(b);
    } else {
      pp.handle(node.getBlockStatement());
    }
    String block = pp.getPrinter().getContent();
    block = block.replaceAll("\\n", "");
    block = block.replaceAll(" ", "");
    getPrinter().print(block);
  }

  @Override
  public void handle(ASTSCExpression node) {
    JavaDSLPrettyPrinter pp = new JavaDSLPrettyPrinter(new IndentPrinter());
    pp.handle(node.getExpression());
    getPrinter().print(pp.getPrinter().getContent());
  }

  @Override
  public void handle(ASTSCInvariantContent node) {
    JavaDSLPrettyPrinter pp = new JavaDSLPrettyPrinter(new IndentPrinter());
    pp.handle(node.getExpression());
    getPrinter().print(pp.getPrinter().getContent());
  }

  @Override
  public void handle(ASTCompleteness node) {
    if (node.isComplete()) {
      getPrinter().print(" (c) ");
    }
    if (node.isIncomplete()) {
      getPrinter().print(" (...) ");
    }
  }

  @Override
  public void handle(ASTSCState node) {
    if (node.isPresentCompleteness()) {
      node.getCompleteness().accept(getRealThis());
    }
    node.getSCModifier().accept(getRealThis());
    getPrinter().print("state " + node.getName());
    if (node.isPresentBracket()) {
      getPrinter().println(" {");
      getPrinter().indent();
      if (node.isPresentInvariant()) {
        node.getInvariant().accept(getRealThis());
      }
      if (node.isPresentEntryAction()) {
        getPrinter().print("entry ");
        node.getEntryAction().accept(getRealThis());
      }
      if (node.isPresentDoAction()) {
        getPrinter().print("do ");
        node.getDoAction().accept(getRealThis());
      }
      if (node.isPresentExitAction()) {
        getPrinter().print("exit");
        node.getExitAction().accept(getRealThis());
      }
      for (ASTSCState astscState : node.getSCStateList()) {
        astscState.accept(getRealThis());
      }
      for(ASTSCTransition astscTransition : node.getSCTransitionList()){
        astscTransition.accept(getRealThis());
      }
      for (ASTSCCode astscCode : node.getSCCodeList()) {
        astscCode.accept(getRealThis());
      }
      for (ASTSCInternTransition astscInternTransition : node.getSCInternTransitionList()) {
        astscInternTransition.accept(getRealThis());
      }
      getPrinter().println();
      getPrinter().unindent();
      getPrinter().print("}");
    }
    getPrinter().println();
  }

  @Override
  public void handle(ASTSCCode node) {
    getPrinter().print("code ");
    node.getSCStatements().accept(getRealThis());
  }

  @Override
  public void handle(ASTSCModifier node) {
    if (node.isPresentStereotype()) {
      node.getStereotype().accept(getRealThis());
    }
    if (node.isInitial()) {
      getPrinter().print("initial ");
    }
    if (node.isFinal()) {
      getPrinter().print("final ");
    }
    if (node.isLocal()) {
      getPrinter().print("local ");
    }
  }

  @Override
  public void handle(ASTStatechart node) {
    if(node.isPresentCompleteness()){
      node.getCompleteness().accept(getRealThis());
    }
    if(node.isPresentStereotype()){
      node.getStereotype().accept(getRealThis());
    }
    getPrinter().print("statechart ");
    if (node.isPresentName()) {
      getPrinter().print(node.getName()+ " ");
    }
    if(node.isPresentClassName()){
      getPrinter().print("for ");
      TypesPrettyPrinterConcreteVisitor pp = new TypesPrettyPrinterConcreteVisitor(new IndentPrinter());
      getPrinter().print(pp.prettyprint(node.getClassName()));
    }
    if(node.isPresentSuperSC()){
      getPrinter().print("refines ");
      TypesPrettyPrinterConcreteVisitor pp = new TypesPrettyPrinterConcreteVisitor(new IndentPrinter());
      getPrinter().print(pp.prettyprint(node.getSuperSC()));
    }
    getPrinter().print("{");
    getPrinter().println();
    getPrinter().indent();

    for (ASTSCState s : node.getSCStateList()) {
      s.accept(getRealThis());
    }
    for (ASTSCTransition t : node.getSCTransitionList()) {
      t.accept(getRealThis());
    }
    getPrinter().unindent();
    getPrinter().println("}");
  }

  @Override
  public void handle(ASTSCArtifact node) {
    if (!node.getPackageList().isEmpty()) {
      getPrinter().print("package ");
      boolean first = true;
      for (String s : node.getPackageList()) {
        if (first) {
          getPrinter().print(s);
          first = false;
        } else {
          getPrinter().print("." + s);
        }
      }
      getPrinter().println(";");
    }
    if (!node.getImportStatementList().isEmpty()) {
      for (ASTImportStatement importStatement : node.getImportStatementList()) {
        getPrinter().print("import ");
        boolean first = true;
        for(String name: importStatement.getImportList()){
          if(first){
            getPrinter().print(name);
            first =false;
          }else{
            getPrinter().print("."+name);
          }
        }
        if(importStatement.isStar()){
          getPrinter().print(".*");
        }
        getPrinter().println(";");
      }
    }
    node.getStatechart().accept(getRealThis());
  }


}
