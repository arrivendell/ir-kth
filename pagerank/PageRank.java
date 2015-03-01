/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,HashSet<Integer>> link = new Hashtable<Integer,HashSet<Integer>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    private int highestDocNum = 0 ;
    /* --------------------------------------------- */


    public PageRank( String filename ) {
       int noOfDocs = readDocs( filename );
       computePagerank( noOfDocs );
   }


   /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
       int fileIndex = 0;
       try {
           System.err.print( "Reading file... " );
           BufferedReader in = new BufferedReader( new FileReader( filename ));
           String line;
           while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
              int index = line.indexOf( ";" );
              String title = line.substring( 0, index );
              Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
              if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
                  fromdoc = fileIndex++;
                  docNumber.put( title, fromdoc );
                  docName[fromdoc] = title;
            //update highest doc
                  highestDocNum = (fromdoc > highestDocNum) ? fromdoc : highestDocNum;
              }
		// Check all outlinks.
              StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
              while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
                  String otherTitle = tok.nextToken();
                  Integer otherDoc = docNumber.get( otherTitle );
                  if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
                     otherDoc = fileIndex++;
                     docNumber.put( otherTitle, otherDoc );
                     docName[otherDoc] = otherTitle;
            //update highest doc
                     highestDocNum = (otherDoc > highestDocNum) ? otherDoc : highestDocNum;
                 }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
                 if ( link.get(fromdoc) == null ) {
                     link.put(fromdoc, new HashSet<Integer>());
                 }
                 if ( link.get(fromdoc).contains(otherDoc) == false ) {
                     link.get(fromdoc).add( otherDoc );
                     out[fromdoc]++;
                 }
             }
         }
         if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
          System.err.print( "stopped reading since documents table is full. " );
      }
      else {
          System.err.print( "done. " );
      }
	    // Compute the number of sinks.
      for ( int i=0; i<fileIndex; i++ ) {
          if ( out[i] == 0 )
              numberOfSinks++;
      }
  }
  catch ( FileNotFoundException e ) {
   System.err.println( "File " + filename + " not found!" );
}
catch ( IOException e ) {
   System.err.println( "Error reading file " + filename );
}
System.err.println( "Read " + fileIndex + " number of documents" );
return fileIndex;
}


/* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
        System.out.println(highestDocNum);
       //double[][] matrixTrans = new double[highestDocNum][highestDocNum];
       double matrixJValue = (double)1/(numberOfDocs);
       System.out.println(matrixJValue);
       double c = 0.85;
        /*for(int i : link.keySet()){
            double proba = (out[i] > 0) ? (double) 1 / out[i] : 0;
            for (int j : link.get(i)){
                matrixTrans[i][j] = proba * c + ((1-c)*matrixJValue);
            }
        }*/
        boolean converged = false;
        double[] initVect = new double[highestDocNum+1];
        initVect[0] = (double)1;
        double[] tempVect = new double[highestDocNum+1];
        int iteration = 0;
        System.out.format("nbre de doc total : %d, nombre de doc retenus : %d", docNumber.values().size(), numberOfDocs);
        while ((!converged) && ( iteration < 10 MAX_NUMBER_OF_ITERATIONS)){
            tempVect = initVect.clone();
            //initVect = matrixProduct(tempVect, matrixTrans);
            initVect = new double[highestDocNum+1];
           
            for(int i : docNumber.values()){
                for( int j : docNumber.values()){
                    if(out[i]==0){
                        if (i!=j){
                            initVect[j] += (double)tempVect[i]   /(double) (numberOfDocs-1);
                        }
                       
                    }
                    else{
                        initVect[j] += tempVect[i] * ((1-c)*matrixJValue);
                    }
                }
            }
            for(int i : link.keySet()){
                for( int j : link.get(i))
                {
                    double proba = 1 / (double)out[i] ;
                    if(i==0){
                        System.out.println(tempVect[i]);
                    }
                    initVect[j] += tempVect[i] * (proba *c);
                   
                   // System.out.println(tempVect[i]);
                }
            }

            /*for(int j : docNumber.values()){
                double sum = 0;
                for( int i : docNumber.values()){
                    if(out[i]==0){
                        if (i!=j){
                            sum += (double) tempVect[i]  /(double) (numberOfDocs-1);
                        }
                       
                    }
                    else{
                        double proba =0;
                        if(link.get(i) != null){
                            proba += (double ) c/out[i];
                        }
                        proba += (1-c) / numberOfDocs ;
                        sum += tempVect[i] * proba;
                    }
                }
            }*/


            converged = testConverge(initVect, tempVect);
            iteration++;
            System.out.println("Iteration");
        }
        
        if (converged || !converged){
            LinkedList<PairKeyValue> vectResult = new LinkedList<PairKeyValue>();
            for(int docID : docNumber.values()){
                vectResult.add(new PairKeyValue(docID,initVect[docID]/*/31748*/));
            }
            Collections.sort(vectResult);
            for(int i = 0; i< 50 ; i++){
                System.out.format(" doc : %s  | %f" , docName[vectResult.get(i).key],vectResult.get(i).value );
            }
        }  
        else{
            System.out.println("not converged");
        }


   }
   private boolean testConverge(double [] x, double[] y ){
        for(int i = 0; i< x.length ; i++){
            double diff = (x[i] - y[i]) > 0 ? (x[i] - y[i]) : (y[i] - x[i]) ;
            if ((diff> EPSILON)  ){
                return false;
            }
        }
        return true;
   }
   private double[] matrixProduct(double[] x, double[][] matrix){
            double[] result = new double[highestDocNum];
            for(int i : link.keySet()){
                for( int j : link.keySet()){
                    result[j] += x[i] * matrix[i][j];
                }
            }
            return result;
    }

   /* --------------------------------------------- */


   public static void main( String[] args ) {
       if ( args.length != 1 ) {
           System.err.println( "Please give the name of the link file" );
       }
       else {
           new PageRank( args[0] );
       }
   }
}
