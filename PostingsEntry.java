/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    public LinkedList<Integer> offsetList = new LinkedList();

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
	return Double.compare( other.score, score );
    }


    public void insertOffset(int offset){
        offsetList.add(offset);
    }
    public PostingsEntry(int docIdIn, double scoreIn, int offset){
        docID = docIdIn;
        score = scoreIn;
        insertOffset(offset);
    }
    public PostingsEntry(int docIdIn, double scoreIn, LinkedList<Integer> listOffset){
        docID = docIdIn;
        score = scoreIn;
        offsetList = listOffset;
    }
}

    
