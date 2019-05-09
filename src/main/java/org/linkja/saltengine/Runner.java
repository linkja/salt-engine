package org.linkja.saltengine;

import org.apache.commons.cli.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.linkja.core.LinkjaException;

import java.io.FileNotFoundException;
import java.security.Security;

public class Runner {
  public static void main(String[] args) {
    Options options = setUpCommandLine();
    CommandLine cmd = parseCommandLine(options, args);

    // If the command line initialization failed, we will stop the program now.  Displaying instructions to the user
    // is already handled by the setup and parser methods.
    if (cmd == null) {
      System.exit(1);
    }

    long startTime = System.nanoTime();

    // Perform some initialization - purposefully done after timing has started.
    Security.addProvider(new BouncyCastleProvider());

    SaltEngine engine = new SaltEngine();

    try {
      // Check to make sure the user specified appropriate command line parameters.
      boolean generateProject = cmd.hasOption("generateProject");
      boolean addSites = cmd.hasOption("addSites");

      if ((generateProject && addSites) || (!generateProject && !addSites)) {
        throw new LinkjaException("Please specify either --generateProject or --addSites");
      }

      if (generateProject) {
        engine.setProjectName(cmd.getOptionValue("projectName"));
        engine.setSitesFile(cmd.getOptionValue("siteFile"));
        engine.generate();
      }
      else {
        engine.setSitesFile(cmd.getOptionValue("siteFile"));
        engine.setPrivateKey(cmd.getOptionValue("privateKey"));
        engine.setSaltFile(cmd.getOptionValue("saltFile"));
        engine.addSites();
      }
    }
    catch (Exception exc) {
      displayUsage();
      System.out.println();
      System.out.println(exc.getMessage());
      System.exit(1);
    }

    long endTime = System.nanoTime();

    double elapsedSeconds = (double)(endTime - startTime) / 1_000_000_000.0;
    System.out.printf("Total execution time: %2f sec\n", elapsedSeconds);
  }

  /**
   * Helper method to prepare our command line options
   * @return Collection of options to be used at the command line
   */
  public static Options setUpCommandLine() {
    Options options = new Options();

    Option generateProjectOpt = new Option("gen", "generateProject", false, "Create a new set of salt files for sites in a project");
    generateProjectOpt.setRequired(false);
    options.addOption(generateProjectOpt);

    Option addSitesOpt = new Option("add", "addSites", false, "Create a salt file for new sites in an existing project");
    addSitesOpt.setRequired(false);
    options.addOption(addSitesOpt);

    // Parameters for either mode
    Option siteFileOpt = new Option("sf", "siteFile", true, "Control file with the salts to load");
    siteFileOpt.setRequired(true);
    options.addOption(siteFileOpt);

    // Parameters for --generateProject
    Option projectNameOpt = new Option("pn", "projectName", true, "The name of the project to create");
    projectNameOpt.setRequired(false);
    options.addOption(projectNameOpt);

    // Parameters for --addSite
    Option privateKeyOpt = new Option("prv", "privateKey", true, "The path to your private key file for the existing project");
    privateKeyOpt.setRequired(false);
    options.addOption(privateKeyOpt);

    Option saltFileOpt = new Option("salt", "saltFile", true, "The path to your encrypted salt file for the existing project");
    saltFileOpt.setRequired(false);
    options.addOption(saltFileOpt);

    return options;
  }

  /**
   * Helper method to wrap parsing the command line parameters and reconcile them against the required and optional
   * command line options.
   * @param options Allowed options
   * @param args Actual command line arguments
   * @return Reconciled CommandLine container, or null if unable to process
   */
  public static CommandLine parseCommandLine(Options options, String[] args) {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      displayUsage();
      System.out.println();
      System.out.println(e.getMessage());
      return null;
    }

    return cmd;
  }

  /**
   * Helper method to display expected command line parameters
   */
  public static void displayUsage() {
    System.out.println();
    System.out.println("Usage: java -jar SaltEngine.jar [--generateProject | --addSites]");
    System.out.println();
    System.out.println("GENERATE PROJECT");
    System.out.println("-------------");
    System.out.println("Required parameters:");
    System.out.println("  -sf,--siteFile <arg>              The path to a file containing the site definitions");
    System.out.println("  -pn,--projectName <arg>           The name of the project to create");
    System.out.println();
    System.out.println("ADD SITES");
    System.out.println("-------------");
    System.out.println("Required parameters:");
    System.out.println("  -sf,--siteFile <arg>              The path to a file containing the site definitions");
    System.out.println("  -prv,--privateKey <arg>           The path to your private key file for the existing project");
    System.out.println("  -salt,--saltFile <arg>            The path to your encrypted salt file for the existing project");
  }
}