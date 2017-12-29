package com.incorta.essbaseutils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.Key;
import java.util.logging.Level;

import oracle.core.ojdl.logging.ODLLogger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.essbase.api.base.EssException;
import com.essbase.api.dataquery.IEssCubeView;
import com.essbase.api.dataquery.IEssMdDataSet;
import com.essbase.api.dataquery.IEssOpMdxQuery;
import com.essbase.api.dataquery.IEssOpMdxQuery.EEssMemberIdentifierType;
import com.essbase.api.session.IEssbase;

public class EsbDataQuery {
	
	private static CommandLine line = null;
	private static String user = null; 			
	private static String password = null;			
	private static String SvrName = null;		
	private static String AppName = null; 		
	private static String DbName = null;
	private static String MDXQuery = null;
	private static String Delimiter = null;
	private static String provider= "Embedded";					
	private static IEssbase ess = null;
	private static IEssCubeView cubeview = null;
	private static IEssMdDataSet esbDataSet = null;
	private static int querycubeerror=0;
	private static Key esbSecKey = null;
	private static String basepath = null;
	
	public static void main(String[] args) throws Exception {
		
		// Get the jar file location	
		String path1 = EsbFunctions.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		File jarfile = new File(URLDecoder.decode(path1,"UTF-8"));
		basepath = jarfile.getParentFile().getPath();
		
		// create the command line parser
		CommandLineParser parser = new GnuParser();
		
		GenFunctions generalFunction = new GenFunctions();

		Options options = new Options();
	
		OptionBuilder.withArgName("-u=username");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Username for connecting to Essbase <required>");
		options.addOption(OptionBuilder.create("u"));
		
		OptionBuilder.withArgName("-p=password");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Password for connecting to Essbase <required>");
		options.addOption(OptionBuilder.create("p"));
		
		OptionBuilder.withArgName("-S=servername");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Essbase Server Name");
		options.addOption(OptionBuilder.create("S"));
		
		OptionBuilder.withArgName("-App=applicationname");
		OptionBuilder.hasArg(true);
	    OptionBuilder.withDescription("Essbase Application <required>");
		options.addOption(OptionBuilder.create("App"));
		
		OptionBuilder.withArgName("-Db=databasename");
	    OptionBuilder.hasArg(true);
	    OptionBuilder.withDescription("Essbase Database <required>");
		options.addOption(OptionBuilder.create("Db"));
		
		OptionBuilder.withArgName("-getData");
	    OptionBuilder.hasArg(false);
	    OptionBuilder.withDescription("Get data from the Essbase database <required>");
		options.addOption(OptionBuilder.create("getData"));		

		OptionBuilder.withArgName("-checkSyntax");
	    OptionBuilder.hasArg(false);
	    OptionBuilder.withDescription("Check MDX syntax");
		options.addOption(OptionBuilder.create("checkSyntax"));		
		
		OptionBuilder.withArgName("-key=key");
	    OptionBuilder.hasArg(true);
	    OptionBuilder.withDescription("Key to encrypt strings");
		options.addOption(OptionBuilder.create("key"));		
		
		OptionBuilder.withArgName("-MDX=MDXQuery");
	    OptionBuilder.hasArg(true);
	    OptionBuilder.withDescription("MDX query to get data <required>");
		options.addOption(OptionBuilder.create("MDX"));		
		
		OptionBuilder.withArgName("-delimiter=delimitercharacter");
	    OptionBuilder.hasArg(true);
	    OptionBuilder.withDescription("Delimiter character");
		options.addOption(OptionBuilder.create("delimiter"));		
		
		OptionBuilder.withArgName("-encrypt=<encryptstring>");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Use this for encrypting strings");
		options.addOption(OptionBuilder.create("encrypt"));
		
		OptionBuilder.withArgName("-D");
		OptionBuilder.hasArg(true);
		OptionBuilder.withDescription("Use this if you are using encryption");
		options.addOption(OptionBuilder.create("D"));			
	
		OptionBuilder.withArgName("-help");
		OptionBuilder.hasArg(false);
		OptionBuilder.withDescription("prints usage");
		options.addOption(OptionBuilder.create("help"));

	    // parse the command line arguments
	    line = parser.parse( options, args );
	    
	    esbSecKey = AESencryption.generateKey(line.getOptionValue("key"));
	    
	    if( line.hasOption("help") ) { 
	    	generalFunction.showHelp(options);
		} else if (line.hasOption("encrypt")) {		
			String encryptstring = line.getOptionValue("encrypt");
			line = parser.parse(options, encryptstring.split(" "));
			if(line.hasOption("key")){
				if(line.hasOption("u") && line.hasOption("p") && line.hasOption("App") && line.hasOption("Db") && line.hasOption("getData") && line.hasOption("MDX")){
						System.out.println(AESencryption.encrypt(encryptstring,esbSecKey));    		
			    } else {
			    	System.out.println(generalFunction.getCurrentTimeStamp() + " Provide all required parameters.");
			    	generalFunction.showHelp(options);
			    }

			} else {
				System.out.println(generalFunction.getCurrentTimeStamp() + " Provide a key to encrypt the string.");
			}
			
		} else if (line.hasOption("D")) {
								
			String[] argsarray = AESencryption.decrypt(line.getOptionValue("D"),esbSecKey).split(" (?=(([^'\"]*['\"]){2})*[^'\"]*$)");
			line = parser.parse(options, argsarray);
			parseargs(options,generalFunction);
		} else {
	    	parseargs(options,generalFunction);
	    }

	}
	
	private static void parseargs(Options options,GenFunctions generalFunction) throws Exception{
			
		EsbFunctions essbaseFunction = new EsbFunctions();
	
	    if(line.hasOption("u") && line.hasOption("p") && line.hasOption("App") && line.hasOption("Db") && line.hasOption("getData") && line.hasOption("MDX")){

    		ODLLogger logger = ODLLogger.getODLLogger("oracle.EPMOHPS");
			logger.setLevel(Level.SEVERE);
			
	    	user = line.getOptionValue( "u" );
	    	password = line.getOptionValue( "p" );
	    	SvrName = line.getOptionValue( "S" );
	    	AppName = line.getOptionValue( "App" );
	    	DbName = line.getOptionValue( "Db" );
	    	MDXQuery = line.getOptionValue( "MDX");
	    	Delimiter = line.getOptionValue( "delimiter");
	    	
	    	boolean needCellAttributes = false;
			boolean hideRestrictedData = true;
			EEssMemberIdentifierType idtype = IEssOpMdxQuery.EEssMemberIdentifierType.NAME; 
/*			
			MDXDataExport export = new MDXDataExport();
			export.exportToFile(user, password, false, basepath+"\\data\\EssbaseExport.txt", AppName, DbName, SvrName, MDXQuery, null);
*/

	    	try {
				ess = IEssbase.Home.create(IEssbase.JAPI_VERSION);
			} catch (EssException japierr) {
				System.out.println(generalFunction.getCurrentTimeStamp() + " Error getting Essbase JAPI version.");
				japierr.printStackTrace(essbaseFunction.printStream);
			}
	    	try {
				cubeview = essbaseFunction.prepareQuery(ess, user, password, provider, SvrName, AppName, DbName);
			} catch (EssException loginerr) {
				System.out.println(generalFunction.getCurrentTimeStamp() + " Error opening Essbase cube view for querying data.");
				loginerr.printStackTrace(essbaseFunction.printStream);
			}
	    	
	    	if (line.hasOption("checkSyntax")){

		    	try {
		    		boolean dataLess = true;
					esbDataSet=essbaseFunction.performQuery(cubeview, dataLess, hideRestrictedData, MDXQuery, needCellAttributes, idtype);
					System.out.println(generalFunction.getCurrentTimeStamp() + " MDX syntax is correct.");
					essbaseFunction.closeCubeView(cubeview);
				} catch (EssException queryerr) {
					System.out.println(generalFunction.getCurrentTimeStamp() + " Error in MDX Syntax.");
					queryerr.printStackTrace(essbaseFunction.printStream);
				}
		    	
	    	} else {
		    	try {
		    		boolean dataLess = false;
					esbDataSet=essbaseFunction.performQuery(cubeview, dataLess, hideRestrictedData, MDXQuery, needCellAttributes, idtype);
					System.out.println(generalFunction.getCurrentTimeStamp() + " Essbase queried successfully.");
					essbaseFunction.closeCubeView(cubeview);
				} catch (EssException queryerr) {
					System.out.println(generalFunction.getCurrentTimeStamp() + " Error setting MDX query.");
					queryerr.printStackTrace(essbaseFunction.printStream);
					querycubeerror=queryerr.getCode();
				}
		    			    	
				if (querycubeerror==0){
					System.out.println(generalFunction.getCurrentTimeStamp() + " Export MDX output to file.");
			    	try {
			    		if(Delimiter == null){
			    			essbaseFunction.exportQuery(esbDataSet,"\t",basepath);
			    			System.out.println(generalFunction.getCurrentTimeStamp() + " Export completed.");
			    		} else {
			    			essbaseFunction.exportQuery(esbDataSet,Delimiter,basepath);
			    			System.out.println(generalFunction.getCurrentTimeStamp() + " Export completed.");
			    		}
					} catch (EssException | IOException printdataerr) {
						System.out.println(generalFunction.getCurrentTimeStamp() + " Error exporting query to file.");
						printdataerr.printStackTrace(essbaseFunction.printStream);
					}
				}
	    	}
	    	
	    	essbaseFunction.printStream.close();
	    	
	    	
	    } else {
	    	
	    	System.out.println(generalFunction.getCurrentTimeStamp() + " Provide all required parameters.");
	    	generalFunction.showHelp(options);
	    }
	    
	}
	
}