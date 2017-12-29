package com.incorta.essbaseutils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class GenFunctions{
	protected String getCurrentTimeStamp() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa"); 
		Date now = new Date();
		final String strDate = sdfDate.format(now);
		return strDate;
	}	
	
	protected void showHelp(Options options){
	    HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp("EsbDataQuery", options );
	}	
}
