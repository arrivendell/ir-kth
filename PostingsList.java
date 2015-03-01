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
import java.util.Collections;


/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {

    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();
    public long frequency =0;

    /**  Number of postings in this list  */
    public int size() {
     return list.size();
     }
     public PostingsList(){};
     public PostingsList( LinkedList<PostingsEntry> inList){
        list = inList;
     }

     public boolean containsDocID(int docID){
        return true;
     }
     /**  Returns the ith posting */
     public PostingsEntry get( int i ) {
         return list.get( i );
     }

       //removes an element
     public void removeEntry( int i ) {
         list.remove(i);
     }

     public void sort(){
        Collections.sort(list);
     }
     public void insertElement(int docID, double score, int offset){

        //if we are in the right order, last docID should be smaller thant new docID
        if ((size() == 0 ) || (get(size()-1).docID < docID)){
            list.add(new PostingsEntry(docID, score, offset));
            frequency++;
        }
            //if it is equal, we just add the offset
         else if (get(size()-1).docID == docID){
            get(size()-1).insertOffset(offset);
            frequency++;
        }
            // if it is greater, then the order is wrong, so we find the right index, add offset or create new
       // else if (get(size()-1).docID > docID){
       //     int indexToAdd = this.findIndex(docID);
       //     if (indexToAdd != -1){
       //         if (get(indexToAdd).docID == docID){
       //             get(indexToAdd).insertOffset(offset);
       //             frequency++;
       //         }
       //         else{
       //             list.add(indexToAdd, new PostingsEntry(docID, score, offset));
       //             frequency++;
       //         }
       //     }
       // }
    }
    public void insertElement(PostingsEntry pe){
        list.add(pe);
    }
    //add new doc at the end of the list
    public void addDocument(int docID, double score,  LinkedList<Integer>  listOffset){
            list.add(new PostingsEntry(docID, score, listOffset));
            this.frequency += listOffset.size();
    }


    /**
    return the index where should be inserted docID. Does not specifies if docID already exists,
    index must be checked. o(log(n)) since it is dichotomy search.
    **/
    private int findIndex(int docID){
        int startIndex = 0;
        int endIndex = size()-1;
        boolean found = false;
        int docIDFound=-1;
        if ( size() == 0){
            return 0;
        }
        else{
            while(!found){
                docIDFound = get((endIndex + startIndex)/2).docID;
                //System.out.format("docIDFound : %d, docID : %d, startIndex : %d, endIndex:%d", docIDFound,docID,startIndex,endIndex);

                //System.out.println("");

                if(docIDFound == docID){
                    return -1;
                }
                if (docIDFound > docID){
                    endIndex=(endIndex + startIndex)/2;
                }
                else if (docIDFound < docID) {
                    startIndex = (endIndex + startIndex)/2;
                }
                if ((endIndex - startIndex <= 1) ){
                    found = true;
                }
            }
            if (docIDFound < docID){
                return startIndex+1;
            }
            else if (docIDFound > docID)
            {
                return startIndex;
            }
            else {
                System.out.println("doc already exists");
            }
        }
        return -2;
    }

}



