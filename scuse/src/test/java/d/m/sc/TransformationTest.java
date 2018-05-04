package d.m.sc;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.umlcd4a.prettyprint.CDPrettyPrinterConcreteVisitor;
import de.monticore.umlsc.statechart._ast.ASTSCArtifact;
import de.monticore.umlsc.statechart._ast.ASTStatechart;
import de.monticore.umlsc.statechart.prettyprint.StatechartPrettyPrinter;
import de.monticore.umlsc.statechartwithjava._parser.StatechartWithJavaParser;
import mc.tf.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransformationTest {
  
  String input = "src/test/models/Banking.sc";
  
  @Test
  public void testRenameSC() throws IOException {
    Optional<ASTSCArtifact> ast = new StatechartWithJavaParser().parse(input);
    
    assertTrue(ast.isPresent());
    
    RenameSC rename = new RenameSC(ast.get());
    
    System.out.println(ast.get().getStatechart().getName());
    
    assertTrue(rename.doPatternMatching());
    
    rename.doReplacement();
    
    assertTrue(ast.get().getStatechart().getName().isPresent());
    assertEquals("Auftrag2", ast.get().getStatechart().getName().get());
    System.out.println(new StatechartPrettyPrinter().prettyPrint(ast.get()));
  }

  @Test
  public void testSCToCD() throws IOException {
    Optional<ASTSCArtifact> ast = new StatechartWithJavaParser().parse(input);

    assertTrue(ast.isPresent());

    State2CD state2CD = new State2CD(ast.get());

    System.out.println(ast.get().getStatechart().getName());

    assertTrue(state2CD.doPatternMatching());
    
    state2CD.doReplacement();

    assertEquals("Banking", state2CD.get_$CD().getName());

    System.out.println(new CDPrettyPrinterConcreteVisitor(new IndentPrinter()).prettyprint(state2CD.get_$CD()));

  }
  
  @Test
  public void testAddState() throws IOException {
   
    Optional<ASTSCArtifact> ast = new StatechartWithJavaParser().parse(input);
    
    assertTrue(ast.isPresent());
  
    AddState tf = new AddState(ast.get());
  
    System.out.println("#States: " + ast.get().getStatechart().getSCStates().size());
    
    assertTrue(tf.doPatternMatching());
    
    tf.doReplacement();
  
    System.out.println("#States: " + ast.get().getStatechart().getSCStates().size());
    System.out.println(new StatechartPrettyPrinter().prettyPrint(ast.get()));
    
  }
  
  @Test
  public void testRemoveTransition() throws IOException {
   
    Optional<ASTSCArtifact> ast = new StatechartWithJavaParser().parse(input);
    
    assertTrue(ast.isPresent());
    
    assertEquals(7, ast.get().getStatechart().getSCTransitions().size());
    System.out.println("#Transitions: " + ast.get().getStatechart().getSCTransitions().size());
    
    RemoveTransition rename = new RemoveTransition(ast.get());
  
    assertTrue(rename.doPatternMatching());
  
    rename.doReplacement();
    
    assertEquals(6, ast.get().getStatechart().getSCTransitions().size());
    System.out.println("#Transitions: " + ast.get().getStatechart().getSCTransitions().size());
    System.out.println(new StatechartPrettyPrinter().prettyPrint(ast.get()));
  }
  
  @Test
  public void testChangeInitial() throws IOException {
   
    Optional<ASTSCArtifact> ast = new StatechartWithJavaParser().parse(input);
    
    assertTrue(ast.isPresent());
  
    ChangeInitial tf = new ChangeInitial(ast.get());
    
    assertEquals("Offer", ast.get().getStatechart().getInitialState().getName());
    
    assertTrue(tf.doPatternMatching());
    
    tf.doReplacement();
  
    assertEquals("Production", ast.get().getStatechart().getInitialState().getName());
    System.out.println(new StatechartPrettyPrinter().prettyPrint(ast.get()));
    
  }
  @Test
  public void testChangeTransition() throws IOException {
   
    Optional<ASTSCArtifact> ast = new StatechartWithJavaParser().parse(input);

    assertTrue(ast.isPresent());

    ChangeTransition tf = new ChangeTransition(ast.get());


    assertTrue(tf.doPatternMatching());

    tf.doReplacement();
    
    assertEquals("Error", tf.get_$T().getTargetName());

    System.out.println(new StatechartPrettyPrinter().prettyPrint(ast.get()));

  }
  @Test
  public void testAddTransition() throws IOException {
   
    Optional<ASTSCArtifact> ast = new StatechartWithJavaParser().parse(input);
    
    assertTrue(ast.isPresent());
    
    AddTransition tf = new AddTransition(ast.get());
    
    System.out.println(ast.get().getStatechart().getInitialState().getName());
    
    assertTrue(tf.doPatternMatching());
    
    tf.doReplacement();
    
    System.out.println(new StatechartPrettyPrinter().prettyPrint(ast.get()));
    
  }
  @Test
  public void testIntroduceErrorState() throws IOException {
   
    Optional<ASTSCArtifact> ast = new StatechartWithJavaParser().parse(input);

    assertTrue(ast.isPresent());

    AddErrorState tf = new AddErrorState(ast.get());

    assertEquals(6, ast.get().getStatechart().getSCStates().size());
    System.out.println("#States: " + ast.get().getStatechart().getSCStates().size());

    assertTrue(tf.doPatternMatching());

    tf.doReplacement();

    AddTransition tf_trans = new AddTransition(ast.get());
    while(tf_trans.doPatternMatching()) {
      tf_trans.doReplacement();
      tf_trans = new AddTransition(ast.get());
    }
  
    assertEquals(7, ast.get().getStatechart().getSCStates().size());
    System.out.println("#States: " + ast.get().getStatechart().getSCStates().size());
    System.out.println(new StatechartPrettyPrinter().prettyPrint(ast.get()));

  }
}