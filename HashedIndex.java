/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
        PostingsList posting = this.getPostings(token);
        if(posting != null){
            posting.insertElement(docID, (double)0, offset);
            index.put(token, posting);
        }
        else{
            PostingsList newPosting = new PostingsList();
            newPosting.insertElement(docID, (double)0, offset);
            index.put(token, newPosting);
        }
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
        return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        PostingsList element = index.get(token) ;
        if (element != null)
        {
            return element;
        }
        return null;
    }
    public void setPathToIndex(String path){
        
    }

    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
        switch(queryType){
            case Index.INTERSECTION_QUERY : 
            return searchIntersection( query, rankingType, structureType);

            case Index.PHRASE_QUERY :
            return searchPhrase( query, rankingType, structureType);
            default:
            break;
        }

        return null;
    }

    private PostingsList searchIntersection(Query query, int rankingType, int structureType){
        LinkedList<PostingsList> resultIntersection = new LinkedList();

        LinkedList<PostingsList> termList = new LinkedList();
        int maxSize=0;

        //case of null query
        if(query.size() == 0){
            return null;
        }

        //we store the first result
        PostingsList listReference = getPostings(query.terms.get(0));

        //if there is not the first word, then intersection is null
        if(listReference == null){
            return null;
        }

        //We get all the list from terms and create a ordered list.
        for(String term : query.terms){
            PostingsList listResult = getPostings(term);

          if(listResult == null){
              return null;
          }

           //DEBUG
            for(int i = 0; i<listResult.size();i++){
                System.out.format("%d|",listResult.get(i).docID);

            }
            System.out.println("");

          int i = 0;
          int termPos = 0;
          //We add the list in the frequency order
          while((i<termList.size()) && (termList.get(i).size()<= listResult.size())){
              i++;
          }
          termList.add(i,listResult);
          termPos++;
        }


        

        int listReferenceSize = listReference.size();
        query.removeTerm(0);
        //then we "and" the base list with all incoming list
        for (PostingsList listForTerm : termList){
            PostingsList resultList = new PostingsList();
            ////if there is a null list, then intersection is null
            if(listForTerm == null){
                return null;
            }

            int i = 0;
            int j =0;
            while((i < listReference.size()) && (j < listForTerm.size()))
            {
                if(listReference.get(i).docID < listForTerm.get(j).docID){
                    i++;
                }
                else if (listForTerm.get(j).docID < listReference.get(i).docID){
                    j++;
                }
                else if(listForTerm.get(j).docID == listReference.get(i).docID){
                    resultList.addDocument(listReference.get(i).docID,listReference.get(i).score, listReference.get(i).offsetList );
                    i++;
                    j++;
                }

            }
            listReference = resultList;
        }
        return listReference;
    }



    private PostingsList searchPhrase(Query query, int rankingType, int structureType){
        LinkedList<TermTuple> termList = new LinkedList();

        int termPos=0;
        //We get all the list from terms and create a ordered list.
        for(String term : query.terms){
            PostingsList listResult = getPostings(term);

            if(listResult == null){
                return null;
            }

            int i = 0;
            //We add the list in the frequency order
            while((i<termList.size()) && (termList.get(i).list.frequency<= listResult.frequency)){
                i++;
            }
            termList.add(i,new TermTuple(listResult, termPos));
            termPos++;
        }

        TermTuple tupleListeReference = termList.get(0);
        termList.remove(0);

        for(TermTuple listOfDocs : termList){
            int j = 0;
            int k = 0;
            PostingsList listResult = new PostingsList();
            while( (j< tupleListeReference.list.size()) && (k< listOfDocs.list.size()) ){

                if(tupleListeReference.list.get(j).docID < listOfDocs.list.get(k).docID){
                    j++;
                }
                else if (listOfDocs.list.get(k).docID < tupleListeReference.list.get(j).docID){
                    k++;
                }
                else if(listOfDocs.list.get(k).docID == tupleListeReference.list.get(j).docID){
                    if (hasRightPositionedWords(tupleListeReference.list.get(j).offsetList, listOfDocs.list.get(k).offsetList,(listOfDocs.position - tupleListeReference.position))){
                        listResult.addDocument(tupleListeReference.list.get(j).docID, tupleListeReference.list.get(j).score, tupleListeReference.list.get(j).offsetList);
                    }
                    j++;
                    k++;      
                 }
            }
            tupleListeReference.list = listResult;

        }


        return tupleListeReference.list;
    }

    //return true if there is an occurence of term corresponding to p2 <distance> words relatively to p1 term ( distance can be positive or negative)
    private boolean hasRightPositionedWords(LinkedList<Integer> p1, LinkedList<Integer> p2, int distance){
        int j = 0;
        int k = 0;


        while( (j< p1.size() ) && (k< p2.size()) ){
             
                if(p1.get(j) + distance < p2.get(k)){
                    j++;
                }
                else if (p2.get(k) < p1.get(j) + distance){
                    k++;
                }
                else if(p2.get(k) == p1.get(j) + distance){
                    return true;
                }
            }
            return false;
    }

    public void loadIndex(String pathToIndex){
        //try{
        //    File fileIndex = new File(pathToIndex);
        //    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileIndex)) ;
        //    index = (HashMap<String,String>) ois.readObject();
        //    ois.close();
        //}
        //catch(IOException i)
        //{
        //    System.out.println(i);
        //}
        //catch(ClassNotFoundException c)
        //{
        //    System.out.println(c);
        //}
    }
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
