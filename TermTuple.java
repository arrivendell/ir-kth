/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;


/**
 *   A list of postings for a given word.
 */
public class TermTuple {

    public  PostingsList list = new PostingsList();
    public int position =0;

    public TermTuple(PostingsList inList, int inPosition ){
        list = inList;
        position = inPosition;
    }
}



