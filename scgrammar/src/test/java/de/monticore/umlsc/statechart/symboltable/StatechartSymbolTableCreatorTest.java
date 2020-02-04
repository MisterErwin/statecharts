/* (c) https://github.com/MontiCore/monticore */
package de.monticore.umlsc.statechart.symboltable;

import de.monticore.io.paths.ModelPath;
//import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.IScopeSpanningSymbol;
import de.monticore.umlsc.statechartwithjava._symboltable.StatechartWithJavaGlobalScope;
//import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.symboltable.IScope;
import de.monticore.umlsc.statechart._symboltable.StatechartSymbol;
import de.monticore.umlsc.statechartwithjava._symboltable.StatechartWithJavaLanguage;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @since   TODO: add version number
 *
 */
public class StatechartSymbolTableCreatorTest {
	
	private StatechartWithJavaGlobalScope globalScope;



	@Before
	public void setup() {
		final StatechartWithJavaLanguage statechartLanguage = new StatechartWithJavaLanguage();
		

		
		final ModelPath modelPath = new ModelPath(Paths.get("src/test/resources"));
		
		globalScope = new StatechartWithJavaGlobalScope(modelPath, statechartLanguage);
		
		
	}
	
	
	@Test
	public void testStatechartSymbolTableCreation() {

		StatechartWithJavaGlobalScope globalScope2;

		final StatechartWithJavaLanguage statechartLanguage2 = new StatechartWithJavaLanguage();


		final ModelPath modelPath2 = new ModelPath(Paths.get("src/test/resources/"));

		globalScope2 = new StatechartWithJavaGlobalScope(modelPath2, statechartLanguage2);


		//final StatechartSymbol scSymbol = globalScope.<StatechartSymbol>resolve("Test1",StatechartSymbol.KIND).orElse(null);
		final StatechartSymbol scSymbol = globalScope2.resolveStatechart("Test1").orElse(null);
		System.out.println(scSymbol);
		assertNotNull(scSymbol);
	}

	@Test
	public void testStatechartSymbolTableCreationInPackage() {
		final String fullName =  "de.monticore.umlsc.symboltable.Test1";
		//final ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/de/monticore/umlsc/symboltable/Test1.sc"));
		//final StatechartSymbol scSymbol = globalScope.<StatechartSymbol>resolve(fullName ,StatechartSymbol.KIND).orElse(null);
		final StatechartSymbol scSymbol = globalScope.resolveStatechart(fullName).orElse(null);
		assertNotNull(scSymbol);
		assertEquals(fullName, scSymbol.getFullName());
	}










}
