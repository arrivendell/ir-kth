/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.Math;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndexFiles implements Index, Serializable {


    /** The index as a hashtable. */
    private HashMap<String,String> index = new HashMap<String,String>();

    private static final int MAX_ENTRIES = 400000;
    //LinkedHashMap fits the role of a cache, deleting eldest entries when size()>MAX_ENTRIES
    private LinkedHashMap<String,PostingsList> cacheIndex = new LinkedHashMap<String,PostingsList>(MAX_ENTRIES + 1){

        protected boolean removeEldestEntry(Map.Entry eldest) {
            boolean isRemoved = size() > MAX_ENTRIES;
            if(isRemoved){
                String path = writeTermToFile((String) eldest.getKey(),(PostingsList) eldest.getValue());
                index.put((String) eldest.getKey(), path);
            }
            return isRemoved;
        }
    };

    // this is the default path for index on disk
    public  String PATHINDEX =  "./index/index.ser";
    public String pathIndex =  PATHINDEX;

    //Enable serialization and deserialization of Interface attributes
    public HashMap<String, String> docIDsSeria = new HashMap<String,String>();
    public HashMap<String,Integer> docLengthsSeria = new HashMap<String,Integer>();

    public HashedIndexFiles(){
        new File("./index/").mkdir();
    }

    /* update the index path. To be use if we load an index from a location,
     * to save it at the same location
     */
    public void setPathToIndex(String path){
        this.pathIndex = path;
    }
    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {
        PostingsList posting = this.getPostings(token);
        if(posting != null){
            posting.insertElement(docID, (double)0, offset);
            cacheIndex.put(token, posting);
        }
        else{
            PostingsList newPosting = new PostingsList();
            newPosting.insertElement(docID, (double)0, offset);
            cacheIndex.put(token, newPosting);
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
        //System.out.println(cacheIndex.size());
        PostingsList listInCache = cacheIndex.get(token);
        if(listInCache != null)
        {
            return listInCache;
        }
        else{
            String pathToTerm = index.get(token) ;
            if (pathToTerm != null)
            {
                return getTermFromFile(pathToTerm);
            }
            return null;
        }
        
    }

    /* retrieves a file containing a posting list for a term, and return it
       if the files does not exists, return null
     */
       private PostingsList getTermFromFile(String path){
        try{
            File fileTerm = new File(path);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileTerm)) ;
            PostingsList returnResult = (PostingsList) ois.readObject();
            //fileTerm.close();
            ois.close();
            return returnResult;
        }
        catch(IOException i)
        {
            System.out.println(i);
            return null;
        }
        catch(ClassNotFoundException c)
        {
            System.out.println(c);
            return null;
        }
    }

    /*
      write a posting list associated to the term "termname" in a file located in a folder
      named as the first letter of the term. Create the folder if does not exist.
      return the path to the file.
     */
      private String writeTermToFile(String termName, PostingsList postingList){
        new File("./index/" + termName.charAt(0)).mkdir();
        String path = "./index/" + termName.charAt(0) + "/" + termName + ".ser";
        try{
            File file = new File(path);

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(postingList);
           // file.close();
            oos.close();
            return path;
        }
        catch(IOException i) {
            System.out.println(i);
            return null;
        }
        
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

            case Index.RANKED_QUERY :
            //System.out.println("coucou");
            return searchRank( query, rankingType, structureType);
            default:
            break;
        }

        return null;
    }

    private PostingsList searchRank(Query query, int rankingType, int structureType){
         //case of null query
        if(query.size() == 0){
            return null;
        }

        PostingsList listReference = getPostings(query.terms.get(0));

        int dFt = listReference.size(); 
            System.out.println(dFt);
        if(listReference == null){
            return null;
        }
        for(int i = 0; i< listReference.size(); i++){
            PostingsEntry temp = listReference.get(i);
            int tFdt =  temp.offsetList.size();
            System.out.println(tFdt);
            int lenD = this.docLengths.get(Integer.toString(temp.docID));
            System.out.println(lenD);
            int n = docIDs.keySet().size();
            System.out.println(n);
            double iDFt = Math.log((double)n/dFt);
            System.out.println(iDFt);
            temp.score = ( tFdt * iDFt ) / lenD;
        }

        listReference.sort();

        return listReference;
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
        //then we "and" the base list with all incoming list to a new list
        for (PostingsList listForTerm : termList){
        PostingsList resultList = new PostingsList();
            ////if there is a null list, then intersection is null
        if(listForTerm == null){
            return null;
        }

        int i = 0;
        int j =0;
        //AND between the lists
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
        //first we take the docs with same ID
        while( (j< tupleListeReference.list.size()) && (k< listOfDocs.list.size()) ){

            if(tupleListeReference.list.get(j).docID < listOfDocs.list.get(k).docID){
                j++;
            }
            else if (listOfDocs.list.get(k).docID < tupleListeReference.list.get(j).docID){
                k++;
            }
            else if(listOfDocs.list.get(k).docID == tupleListeReference.list.get(j).docID){
                //then we search for phrases, from the reference only
                LinkedList<Integer> listOffsetResult = hasRightPositionedWords(tupleListeReference.list.get(j).offsetList, listOfDocs.list.get(k).offsetList,(listOfDocs.position - tupleListeReference.position));
                if (listOffsetResult.size() != 0){
                    listResult.addDocument(tupleListeReference.list.get(j).docID, tupleListeReference.list.get(j).score, listOffsetResult);
                }
                j++;
                k++;      
            }
        }
        tupleListeReference.list = listResult;

    }


    return tupleListeReference.list;
}

////return true if there is an occurence of term corresponding to p2 <distance> words relatively to p1 term ( distance can be positive or negative)
//private boolean hasRightPositionedWords(LinkedList<Integer> p1, LinkedList<Integer> p2, int distance){
//    int j = 0;
//    int k = 0;
//
//
//    while( (j< p1.size() ) && (k< p2.size()) ){
//
//        if(p1.get(j) + distance < p2.get(k)){
//            j++;
//        }
//        else if (p2.get(k) < p1.get(j) + distance){
//            k++;
//        }
//        else if(p2.get(k) == p1.get(j) + distance){
//            return true;
//        }
//    }
//    return false;
//}

//return true if there is an occurence of term corresponding to p2 <distance> words relatively to p1 term ( distance can be positive or negative)
private LinkedList<Integer> hasRightPositionedWords(LinkedList<Integer> p1, LinkedList<Integer> p2, int distance){
    int j = 0;
    int k = 0;
    LinkedList<Integer> listResult = new LinkedList();

    while( (j< p1.size() ) && (k< p2.size()) ){

        if(p1.get(j) + distance < p2.get(k)){
            j++;
        }
        else if (p2.get(k) < p1.get(j) + distance){
            k++;
        }
        else if(p2.get(k) == p1.get(j) + distance){
            listResult.add(p1.get(j));
            j++;
            k++;
        }
    }
    return listResult;
}

//save the cache on the disk, to avoid too big serialization
private void saveCache(){
   for(Map.Entry<String, PostingsList> en: cacheIndex.entrySet()){
    String path = writeTermToFile((String) en.getKey(),(PostingsList) en.getValue());
    index.put((String) en.getKey(),path);
}
}
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
        //private String writeTermToFile(String termName, PostingsList postingList){
        new File("./index/").mkdir();
        String path = pathIndex;
        try{
            File file = new File(path);
            docIDsSeria = docIDs;
            docLengthsSeria = docLengths;
            saveCache();
            cacheIndex.clear();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(this);
           // file.close();
            oos.close();
        }
        catch(IOException i) {
            System.out.println(i);
        }
       // catch(FileNotFoundException c)
       // {
       //     System.out.println(c);
       //     return null;
       // }
        
    }

    private  void writeObject(ObjectOutputStream oos)
    throws IOException {

      oos.writeObject(docIDs) ;
      oos.writeObject(docLengths) ;
      oos.writeObject(index) ;
      oos.writeObject(cacheIndex) ;
      oos.writeObject(PATHINDEX) ;
      oos.writeObject(pathIndex) ;
  }
  private  void readObject(ObjectInputStream ois)
  throws IOException, ClassNotFoundException {

     this.docIDsSeria = (HashMap<String,String>) ois.readObject() ;
     this.docLengthsSeria = (HashMap<String,Integer>) ois.readObject() ;
     this.index = (HashMap<String,String>) ois.readObject() ;
     this.cacheIndex = (LinkedHashMap<String, PostingsList>) ois.readObject() ;
     this.PATHINDEX = (String) ois.readObject() ;
     this.pathIndex = (String) ois.readObject() ;
     docIDs.putAll(docIDsSeria);
     docLengths.putAll(docLengthsSeria);
 }

}
