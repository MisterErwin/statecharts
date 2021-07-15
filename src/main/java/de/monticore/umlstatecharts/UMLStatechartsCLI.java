/* (c) https://github.com/MontiCore/monticore */
package de.monticore.umlstatecharts;

import de.monticore.cd4code.prettyprint.CD4CodeFullPrettyPrinter;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.generating.GeneratorEngine;
import de.monticore.generating.GeneratorSetup;
import de.monticore.generating.templateengine.GlobalExtensionManagement;
import de.monticore.io.paths.MCPath;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.UMLStatechartsFullPrettyPrinter;
import de.monticore.sc2cd.SC2CDConverter;
import de.monticore.scbasis.BranchingDegreeCalculator;
import de.monticore.scbasis.InitialStateCollector;
import de.monticore.scbasis.ReachableStateCollector;
import de.monticore.scbasis.StateCollector;
import de.monticore.scbasis._ast.ASTSCArtifact;
import de.monticore.scbasis._ast.ASTSCState;
import de.monticore.scbasis._cocos.*;
import de.monticore.scbasis._symboltable.SCStateSymbol;
import de.monticore.scevents._cocos.NonCapitalEventNames;
import de.monticore.scevents._cocos.NonCapitalParamNames;
import de.monticore.scevents._symboltable.SCEventsSTCompleter;
import de.monticore.scstatehierarchy.HierarchicalStateCollector;
import de.monticore.scstatehierarchy.NoSubstatesHandler;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.DeriveSymTypeOfUMLStatecharts;
import de.monticore.types.SynthesizeSymType;
import de.monticore.types.check.TypeCheck;
import de.monticore.umlstatecharts._cocos.UMLStatechartsCoCoChecker;
import de.monticore.umlstatecharts._symboltable.IUMLStatechartsArtifactScope;
import de.monticore.umlstatecharts._symboltable.UMLStatechartsScopesGenitorDelegator;
import de.monticore.umlstatecharts._visitor.UMLStatechartsTraverser;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UMLStatechartsCLI extends UMLStatechartsCLITOP {

  @Override
  public void run(String[] args){
    Options options = initOptions();

    try {
      // create CLI parser and parse input options from command line
      CommandLineParser cliparser = new DefaultParser();
      CommandLine cmd = cliparser.parse(options, args);

      // help: when --help
      if (cmd.hasOption("h")) {
        printHelp(options);
        // do not continue, when help is printed
        return;
      }

      // if -i input is missing: also print help and stop
      if (!cmd.hasOption("i")) {
        printHelp(options);
        // do not continue, when help is printed
        return;
      }

      // we need the global scope for symbols and cocos
      MCPath symbolPath = new MCPath(Paths.get(""));
      if (cmd.hasOption("path")) {
        symbolPath = new MCPath(Arrays.stream(cmd.getOptionValues("path")).map(x -> Paths.get(x)).collect(Collectors.toList()));
      }
      UMLStatechartsMill.globalScope().setSymbolPath(symbolPath);
      BasicSymbolsMill.initializePrimitives();

      // parse input file, which is now available
      // (only returns if successful)
      ASTSCArtifact scartifact = parse(cmd.getOptionValue("i"));

      IUMLStatechartsArtifactScope scope = createSymbolTable(scartifact);

      // check context conditions
      runDefaultCoCos(scartifact);

      if (cmd.hasOption("s")) {
        String path = cmd.getOptionValue("s", StringUtils.EMPTY);
        storeSymbols(scope, path);
      }

      // -option pretty print
      if (cmd.hasOption("pp")) {
        String path = cmd.getOptionValue("pp", StringUtils.EMPTY);
        prettyPrint(scartifact, path);
      }

      // -option reports
      if (cmd.hasOption("r")) {
        String path = cmd.getOptionValue("r", StringUtils.EMPTY);
        report(scartifact, path);
      }

      // -option generate to CD
      if (cmd.hasOption("gen")) {
        String path = cmd.getOptionValue("gen", StringUtils.EMPTY);
        generateCD(scartifact, path);
      }


    } catch (ParseException e) {
      // ann unexpected error from the apache CLI parser:
      Log.error("0xA5C01 Could not process CLI parameters: " + e.getMessage());
    }
  }

  /**
   * Creates the symbol table from the parsed AST.
   *
   * @param ast The top statechart model element.
   * @return The artifact scope derived from the parsed AST
   */
  @Override
  public IUMLStatechartsArtifactScope createSymbolTable(ASTSCArtifact ast) {

    // create scope and symbol skeleton
    UMLStatechartsScopesGenitorDelegator genitor = UMLStatechartsMill.scopesGenitorDelegator();
    IUMLStatechartsArtifactScope symTab = genitor.createFromAST(ast);

    // complete symbols including type check
    UMLStatechartsTraverser completer = UMLStatechartsMill.traverser();
    TypeCheck typeCheck = new TypeCheck(new SynthesizeSymType(),new DeriveSymTypeOfUMLStatecharts());
    completer.add4SCEvents(new SCEventsSTCompleter(typeCheck));
    ast.accept(completer);

    return symTab;
  }

  /**
   * Creates reports for the Statechart-AST to stdout or a specified file.
   *
   * @param scartifact The Statechart-AST for which the reports are created
   * @param path The target path of the directory for the report artifacts. If
   *          empty, the contents are printed to stdout instead
   */
  @Override
  public void report(ASTSCArtifact scartifact, String path) {
    // calculate and print reports
    String reachable = reportReachableStates(scartifact);
    print(reachable, path, REPORT_REACHABILITY);

    String branching = reportBranchingDegree(scartifact);
    print(branching, path, REPORT_BRANCHING_DEGREE);

    String stateNames = reportStateNames(scartifact);
    print(stateNames, path, REPORT_STATE_NAMES);
  }

  // names of the reports:
  public static final String REPORT_REACHABILITY = "reachability.txt";
  public static final String REPORT_BRANCHING_DEGREE = "branchingDegree.txt";
  public static final String REPORT_STATE_NAMES = "stateNames.txt";

  public String reportReachableStates(ASTSCArtifact ast) {
    UMLStatechartsTraverser traverser = UMLStatechartsMill.traverser();
    // collect all states
    // HierarchicalStateCollector vs StateCollector
    HierarchicalStateCollector stateCollector = new HierarchicalStateCollector();
    traverser.add4SCBasis(stateCollector);
    traverser.add4SCStateHierarchy(stateCollector);
    ast.accept(traverser);
    Set<String> statesToBeChecked = stateCollector.getStates()
      .stream().map(e -> e.getName()).collect(Collectors.toSet());

    // collect all initial states
    traverser = UMLStatechartsMill.traverser();
    InitialStateCollector initialStateCollector = new InitialStateCollector();
    traverser.add4SCBasis(initialStateCollector);
    //  only find real initial states
    traverser.setSCStateHierarchyHandler(new NoSubstatesHandler());
    ast.accept(traverser);
    Set<String> reachableStates = initialStateCollector.getStates();

    // calculate reachable states
    Set<String> currentlyChecked = new HashSet<>(reachableStates);
    statesToBeChecked.removeAll(reachableStates);
    while (!currentlyChecked.isEmpty()) {
      // While the open list is not empty, check which states can be reached from it
      String from = currentlyChecked.iterator().next();
      currentlyChecked.remove(from);
      ReachableStateCollector reachableStateCollector = new ReachableStateCollector(from);
      traverser = UMLStatechartsMill.traverser();
      traverser.add4SCBasis(reachableStateCollector);
      ast.accept(traverser);
      for (String to : reachableStateCollector.getReachableStates()) {
        if (!reachableStates.contains(to)) {
          // In case a new reachable state is found, add it to the open list
          // and mark it as reachable
          reachableStates.add(to);
          currentlyChecked.add(to);
          statesToBeChecked.remove(to);
        }
      }
      // Handle all inner initial states
      Optional<SCStateSymbol> stateSymbol = ast.getEnclosingScope().resolveSCState(from);
      stateCollector.getStatesMap().clear();
      traverser.add4SCBasis(stateCollector);
      traverser.add4SCStateHierarchy(stateCollector);
      if (!stateSymbol.isPresent())
        throw new IllegalStateException("Failed to resolve state symbol " + from);
      stateSymbol.get().getAstNode().accept(traverser);
      for (ASTSCState innerReachableState : stateCollector.getStates(1)) {
        if (innerReachableState.getSCModifier().isInitial()) {
          reachableStates.add(innerReachableState.getName());
          currentlyChecked.add(innerReachableState.getName());
          statesToBeChecked.remove(innerReachableState.getName());
        }
      }

    }
    return "reachable: " + String.join(",", reachableStates) + System.lineSeparator()
      + "unreachable: " + String.join(",", statesToBeChecked) + System.lineSeparator() ;
  }

  public String reportBranchingDegree(ASTSCArtifact ast) {
    BranchingDegreeCalculator branchingDegreeCalculator = new BranchingDegreeCalculator();
    UMLStatechartsTraverser traverser = UMLStatechartsMill.traverser();
    traverser.add4SCBasis(branchingDegreeCalculator);
    ast.accept(traverser);
    return branchingDegreeCalculator.getBranchingDegrees().entrySet().stream()
      .map(e -> e.getKey() + ": " + e.getValue())
      .collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();
  }

  public String reportStateNames(ASTSCArtifact ast) {
    StateCollector stateCollectorVisitor = new StateCollector();
    UMLStatechartsTraverser traverser = UMLStatechartsMill.traverser();
    traverser.add4SCBasis(stateCollectorVisitor);
    ast.accept(traverser);
    return String.join(", ", stateCollectorVisitor.getStates()
      .stream().map(e -> e.getName()).collect( Collectors.toSet())) + System.lineSeparator();
  }

  /**
   * Checks whether ast satisfies all CoCos.
   *
   * @param ast The ast of the SC.
   */
  @Override
  public void runDefaultCoCos(ASTSCArtifact ast) {
    UMLStatechartsCoCoChecker checker = new UMLStatechartsCoCoChecker();
    checker.addCoCo(new UniqueStates());
    checker.addCoCo(new TransitionSourceTargetExists());
    UMLStatechartsTraverser t = UMLStatechartsMill.traverser();
    t.setSCStateHierarchyHandler(new NoSubstatesHandler());
    checker.addCoCo(new AtLeastOneInitialState(t));
    checker.addCoCo(new CapitalStateNames());
    checker.addCoCo(new PackageCorrespondsToFolders());
    checker.addCoCo(new SCFileExtension());
    checker.addCoCo(new SCNameIsArtifactName());
    checker.addCoCo(new NonCapitalEventNames());
    checker.addCoCo(new NonCapitalParamNames());
    checker.checkAll(ast);
  }

  /**
   * Prints the contents of the SC-AST to stdout or a specified file.
   *
   * @param scartifact The SC-AST to be pretty printed
   * @param file The target file name for printing the SC artifact. If empty,
   *          the content is printed to stdout instead
   */
  @Override
  public void prettyPrint(ASTSCArtifact scartifact, String file) {
    // pretty print AST
    UMLStatechartsFullPrettyPrinter prettyPrinterDelegator
      = new UMLStatechartsFullPrettyPrinter();
    String prettyOutput = prettyPrinterDelegator.prettyprint(scartifact);
    print(prettyOutput, file);
  }

  public void print(String content, String path, String file) {
    print(content, path.isEmpty()?path : path + "/"+ file);
  }

  /**
   * Prints the contents of the SD-AST to stdout or
   * generates the java classes for the generated SD-AST into a directory
   *
   * @param scartifact The SC-AST to be converted
   * @param outputDirectory The target directory name for outputting the CD artifact. If empty,
   *          the content is printed to stdout instead
   */
  public void generateCD(ASTSCArtifact scartifact, String outputDirectory) {
    // pretty print AST
    SC2CDConverter converter = new SC2CDConverter();

    GeneratorSetup config = new GeneratorSetup();
    GlobalExtensionManagement glex = new GlobalExtensionManagement();
    config.setGlex(glex);
    if (!outputDirectory.isEmpty()){
      // Prepare CD4C
      File targetDir = new File(outputDirectory);
      if (!targetDir.exists())
        targetDir.mkdirs();
      config.setOutputDirectory(targetDir);
      config.setTracing(false);
    }

    ASTCDCompilationUnit cd = converter.doConvert(scartifact, config);
    if (outputDirectory.isEmpty()) {
      CD4CodeFullPrettyPrinter prettyPrinter = new CD4CodeFullPrettyPrinter();
      String prettyOutput = prettyPrinter.prettyprint(cd);
      print(prettyOutput, outputDirectory);
    }else{
      final CD4CodeFullPrettyPrinter printer = new CD4CodeFullPrettyPrinter(new IndentPrinter());
      GeneratorEngine generatorEngine = new GeneratorEngine(config);

      Path packageDir = Paths.get(".");
      for (String pn : cd.getCDPackageList()){
        packageDir = Paths.get(packageDir.toString(), pn);
      }
      if (!packageDir.toFile().exists())
        packageDir.toFile().mkdirs();

      for (ASTCDClass clazz : cd.getCDDefinition().getCDClassesList()) {
        Path out = Paths.get(packageDir.toString(),clazz.getName() + ".java");
        generatorEngine.generate("de.monticore.sc2cd.gen.Class", out,
          clazz, printer, cd.getCDPackageList());
      }

    }
  }

  /**
   * Initializes the standard CLI options for the Statechart tool.
   *
   * @return The CLI options with arguments.
   */
  @Override
  public Options addStandardOptions(Options options) {
    // help dialog
    options.addOption(Option.builder("h")
      .longOpt("help")
      .desc("Prints this help dialog")
      .build());

    // parse input file
    options.addOption(Option.builder("i")
      .longOpt("input")
      .argName("file")
      .hasArg()
      .desc("Reads the source file (mandatory) and parses the contents as a statechart")
      .build());

    // pretty print SC
    options.addOption(Option.builder("pp")
      .longOpt("prettyprint")
      .argName("file")
      .optionalArg(true)
      .numberOfArgs(1)
      .desc("Prints the Statechart-AST to stdout or the specified file (optional)")
      .build());

    // pretty print SC
    options.addOption(Option.builder("s")
      .longOpt("symboltable")
      .argName("file")
      .hasArg()
      .desc("Serialized the Symbol table of the given Statechart")
      .build());

    // reports about the SC
    options.addOption(Option.builder("r")
      .longOpt("report")
      .argName("dir")
      .hasArg(true)
      .desc("Prints reports of the statechart artifact to the specified directory. Available reports:"
        + System.lineSeparator() + "reachable states, branching degree, and state names")
      .build());

    // model paths
    options.addOption(Option.builder("path")
      .hasArgs()
      .desc("Sets the artifact path for imported symbols, space separated.")
      .build());

    return options;
  }

  /**
   * Initializes the additional CLI options for the Statechart tool.
   *
   * @return The CLI options with arguments.
   */
  @Override
  public Options addAdditionalOptions(Options options) {
    // convert to state pattern CD
    options.addOption(Option.builder("gen")
      .longOpt("generate")
      .argName("file")
      .optionalArg(true)
      .numberOfArgs(1)
      .desc("Prints the state pattern CD-AST to stdout or the generated java classes to the specified folder (optional)")
      .build());
    return options;
  }
}
