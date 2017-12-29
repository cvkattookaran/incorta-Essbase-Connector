package com.incorta.essbaseutils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;

import com.essbase.api.base.EssException;
import com.essbase.api.dataquery.IEssCubeView;
import com.essbase.api.dataquery.IEssMdAxis;
import com.essbase.api.dataquery.IEssMdDataSet;
import com.essbase.api.dataquery.IEssMdMember;
import com.essbase.api.dataquery.IEssOpMdxQuery;
import com.essbase.api.domain.IEssDomain;
import com.essbase.api.session.IEssbase;

public class EsbFunctions{
	
	private static IEssDomain olapDomain = null;
	private static IEssCubeView cubeView = null;
	protected Stack<String> stack = new Stack<String>();
	protected PrintStream printStream = null;
	
	protected IEssCubeView prepareQuery (IEssbase ess, String user, String password, String provider, String SvrName, String AppName, String DbName) throws EssException{

		GenFunctions essGenFunction = new GenFunctions();
		olapDomain = ess.signOn(user,password, false, null, provider);
    	System.out.println(essGenFunction.getCurrentTimeStamp() + " "+user+" Connected to essbase domain.");
    	cubeView = olapDomain.openCubeView("Essbase Query", SvrName, AppName, DbName);
    	System.out.println(essGenFunction.getCurrentTimeStamp() + " Cube view opened for querying.");
    	
    	return cubeView;
	}
	
	protected void closeCubeView(IEssCubeView cv) throws EssException{
		if (cv != null){
			cv.close();
		}
	}
	
	protected IEssMdDataSet performQuery (IEssCubeView cv, boolean dataLess, boolean hideRestrictedData, String mdxquery, boolean needCellAttributes, IEssOpMdxQuery.EEssMemberIdentifierType idtype) throws EssException{
		IEssOpMdxQuery opMDXQuery = cv.createIEssOpMdxQuery();
		opMDXQuery.setQuery(dataLess, hideRestrictedData, mdxquery, needCellAttributes, idtype);
		/*		
  		opMDXQuery.setDataless(dataLess);
		opMDXQuery.setHideRestrictedData(hideRestrictedData);
		opMDXQuery.setMemberIdentifierType(idtype);
		opMDXQuery.setNeedCellAttributes(needCellAttributes);
		*/
		opMDXQuery.setNeedMeaninglessCells(false);
		opMDXQuery.setNeedFormattedCellValue(true);
		opMDXQuery.setNeedFormatString(true);
		opMDXQuery.setNeedProcessMissingCells(false);
		opMDXQuery.setNeedSmartlistName(true);
		opMDXQuery.setXMLAMode(false);
		cv.performOperation(opMDXQuery);
		IEssMdDataSet dataSet = cv.getMdDataSet();
		
		return dataSet;
	}
	
	protected void exportQuery (IEssMdDataSet dataSet, String delim,String basepath) throws EssException, IOException{
		
		IEssMdAxis[] axes = dataSet.getAllAxes();
		int axesCnt =axes.length;
		IEssMdMember[] povdims = axes[0].getAllDimensions();
	
		printStream = new PrintStream(basepath+"\\data\\EssbaseExport.txt", "UTF-8");
		
		ArrayList<String> povdimnames = new ArrayList<String>();
		ArrayList<String> rowdimnames = new ArrayList<String>();
		ArrayList<String> povmbrnames = new ArrayList<String>();
		ArrayList<String> colmbrnames = new ArrayList<String>();
		
		int pov = axes[0].getTupleCount();
		int cols = axes[1].getTupleCount();
		
		
		for (int povdimcnt=0; povdimcnt< povdims.length; povdimcnt++){
			IEssMdMember povdimmbr = povdims[povdimcnt];
			povdimnames.add(povdimmbr.getName());
		}					
		
		for (int povcnt=0; povcnt<pov; povcnt++){
			IEssMdMember[] povmbrs = axes[0].getAllTupleMembers(povcnt);
			for (int povmbrcnt=0; povmbrcnt<povmbrs.length;povmbrcnt++){
				IEssMdMember povmbr = povmbrs[povmbrcnt];
				povmbrnames.add(povmbr.getName());
			}
		}
				
		for (int colcnt=0; colcnt<cols; colcnt++){
			IEssMdMember[] colmbrs = axes[1].getAllTupleMembers(colcnt);
			for (int colmbrcnt=0; colmbrcnt<colmbrs.length;colmbrcnt++){
				IEssMdMember colmbr = colmbrs[colmbrcnt];
				colmbrnames.add(colmbr.getName());
			}
		}		
		
		if(axesCnt == 3){
			int cell=0;		
			IEssMdAxis axis = axes[2];
			IEssMdMember[]rowdims = axis.getAllDimensions();
				
			for (int rowDimInd = 0; rowDimInd < rowdims.length; rowDimInd++) {
				IEssMdMember rowdim = rowdims[rowDimInd];
				rowdimnames.add(rowdim.getName());
			}
									
			printStream.println(StringUtils.join(povdimnames, delim)+delim+StringUtils.join(rowdimnames, delim)+delim+StringUtils.join(colmbrnames, delim));		
			
		    int nTuples = axis.getTupleCount();
		    for (int tupleInd = 0; tupleInd < nTuples; tupleInd++)
		    {
		      IEssMdMember[] mbrs = axis.getAllTupleMembers(tupleInd);
		      for (int mbrInd = 0; mbrInd < mbrs.length; mbrInd++) {
		    	  stack.push(mbrs[mbrInd].getName());
		      }
				ArrayList<Double> cellvals = new ArrayList<Double>();
				for (int colcnt=0; colcnt<cols; colcnt++){
					IEssMdMember[] colmbrs = axes[1].getAllTupleMembers(colcnt);
					for (int colmbrcnt=0; colmbrcnt<colmbrs.length;colmbrcnt++){
						Double cellval;
						if(dataSet.isMissingCell(cell) || dataSet.isNoAccessCell(cell)){
							cellval = null;
						} else {
							cellval = dataSet.getCellValue(cell);
						}
						cellvals.add(cellval);
					}
					cell++;
				}
		      printStream.println(StringUtils.join(povmbrnames, delim)+delim+StringUtils.join(stack, delim)+delim+StringUtils.join(cellvals, delim));
		      
		      for (int mbrInd = 0; mbrInd < mbrs.length; mbrInd++) {
		    	  stack.pop();
		      }
		    }
		    
		} else {
			/*			
			for (int axisInd = 2; axisInd < axesCnt; axisInd++) {
				IEssMdAxis axis = axes[axisInd];
				IEssMdMember[]rowdims = axis.getAllDimensions();
				for (int axisDimInd = 0; axisDimInd < rowdims.length; axisDimInd++) {
					IEssMdMember rowdim = rowdims[axisDimInd];
					rowdimnames.add(rowdim.getName());
				}
				axis = null;
				rowdims = null;
			}
			printStream.println(StringUtils.join(povmbrnames, delim)+delim+StringUtils.join(rowdimnames, delim)+delim+StringUtils.join(colmbrnames, delim));
			
			for (int axisInd = 2; axisInd < axesCnt; axisInd++) {
				IEssMdAxis axis = axes[axisInd];
			    int nTuples = axis.getTupleCount();
			    int cell = 0;
				
			    for (int tupleInd = 0; tupleInd < nTuples; tupleInd++)
			    {
			      IEssMdMember[] mbrs = axis.getAllTupleMembers(tupleInd);
			      for (int mbrInd = 0; mbrInd < mbrs.length; mbrInd++) {
			    	  stack.push(mbrs[mbrInd].getName());
			      }
			      
					ArrayList<Double> cellvals = new ArrayList<Double>();
					for (int colcnt=0; colcnt<cols; colcnt++){
						IEssMdMember[] colmbrs = axes[1].getAllTupleMembers(colcnt);
						for (int colmbrcnt=0; colmbrcnt<colmbrs.length;colmbrcnt++){
							Double cellval;
							if(dataSet.isMissingCell(cell) || dataSet.isNoAccessCell(cell)){
								cellval = null;
							} else {
								cellval = dataSet.getCellValue(cell);
							}
							cellvals.add(cellval);
						}
						cell++;
					}
					
			      printStream.println(StringUtils.join(stack, delim)+delim+StringUtils.join(cellvals, delim));
			      
			      System.out.println(stack);
			      for (int mbrInd = 0; mbrInd < mbrs.length; mbrInd++) {
			    	  stack.pop();
			      }
			    }
			}	
			*/	
			printStream.println("Use a row and column MDX query");
		}
		
	    rowdimnames = null;
	    povmbrnames = null;
	    colmbrnames = null;
		
	}
	
}