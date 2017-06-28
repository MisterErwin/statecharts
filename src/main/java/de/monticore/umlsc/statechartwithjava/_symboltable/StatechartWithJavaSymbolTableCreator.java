package de.monticore.umlsc.statechartwithjava._symboltable;

import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.umlsc.statechart._symboltable.StatechartSymbolTableCreator;
import de.monticore.umlsc.statechartwithjava._visitor.StatechartWithJavaVisitor;

import java.util.Deque;

/**
 * Created by eikermann on 28.06.2017.
 */
public class StatechartWithJavaSymbolTableCreator extends StatechartSymbolTableCreator implements StatechartWithJavaVisitor {
  private StatechartWithJavaSymbolTableCreator realThis = this;

  public StatechartWithJavaSymbolTableCreator(ResolvingConfiguration resolvingConfig, MutableScope enclosingScope) {
    super(resolvingConfig, enclosingScope);
  }


  public StatechartWithJavaSymbolTableCreator(final ResolvingConfiguration resolvingConfig, final Deque<MutableScope> scopeStack) {
    super(resolvingConfig, scopeStack);
  }

  @Override
  public StatechartWithJavaVisitor getRealThis() {
    return this.realThis;
  }
}