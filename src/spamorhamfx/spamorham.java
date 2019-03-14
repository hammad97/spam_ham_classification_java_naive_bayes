package spamorhamfx;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
/**
 *
 * @author Dell
 */
public class spamorham {
    public String evaluationResult="";                                  //Used for gui accuracy output only
    public String className[];                                          //Names of classes stored here
    public int totalVocabSize;                                          //Total number of distinct vocab size
    public int totalDocuments;                                          //Total Messages found in .txt
    double classDocCount[];                                             //Messages count in each class
    public Map<String, ArrayList<Integer>> indexByDatasetVocab;         //Hashmap for containing distinct vocab as key and messageid as values
    public Map<String, ArrayList<String>> indexByCategory;              //Hashmap for containing class name as key and messages in that class as values
    public Map<String, ArrayList<String>> indexByVocabulary;            //Hashmap for containing class name as key and vocab in that class as values
    public double priorValues[];                                        //Prior values for each class
    public List<HashMap<String, ArrayList<Double>>> indexByVocabValues; //Conditional probability calculation of each vocab.
    
    public spamorham(String mainFile,String testFile){
        readFile(mainFile);
        Map<String, ArrayList<String>> tempHash;
        tempHash = new HashMap<String, ArrayList<String>>();
        if(testFile.compareToIgnoreCase("NOTESTFILE")!=0){
            tempHash = getTestData(testFile);
        
        int confusionMatrix[][]= new int [className.length][className.length];
        for(int i=0;i<confusionMatrix.length;i++){
            for(int j=0;j<confusionMatrix.length;j++){
                confusionMatrix[i][j]=0;
            }
        }
        ArrayList<String> v = new ArrayList<String>(tempHash.keySet());
            for (String str : v) {
            ArrayList<String> tmp= tempHash.get(str);
            for(int k=0;k<tmp.size();k++){
                ArrayList<Double> finalValues = new ArrayList<Double>(); 
                String message= tmp.get(k);
                message=message.replaceAll("[^a-zA-Z0-9 ]", "");  //[^a-zA-Z#-)!(.@:_0-9 ]
                message=message.toLowerCase();
                String arr[]= message.split(" ");
                for(int i=0;i<className.length;i++){
                    double priorTmp= priorValues[i];
                    for(int j=0;j<arr.length;j++){
                        if(arr[j].contains(" "))
                            arr[j] = arr[j].replace(" ", "");
                        if(indexByDatasetVocab.containsKey(arr[j])){                 //using multinomial NB to calculate but in case of not found using vocabsize for improved accuracy
                            if(indexByVocabValues.get(i).containsKey(arr[j])){
                                double myScore = indexByVocabValues.get(i).get(arr[j]).get(0);
                                priorTmp = priorTmp * myScore;
                            }
                            else{
                                double t= 1.0/(double)totalVocabSize;
                                priorTmp= priorTmp * (t);
//                                priorTmp= priorTmp * 1.0;
                            }
                        }
                        else{
                            double t= 1.0/(double)totalVocabSize;
                            priorTmp= priorTmp * (t);
//                            priorTmp= priorTmp * 1.0;
                        }
                    }
                    finalValues.add(priorTmp);
                }

                int largestIndex = 0;
                for(int i=1;i<finalValues.size();i++){
                    if(finalValues.get(largestIndex)<finalValues.get(i))
                        largestIndex=i;
                }
//                for(int i=0;i<finalValues.size();i++){
//                    System.out.println(at.className[i]+" Value:"+finalValues.get(i));
//                }
    //            System.out.println("///////////////////////RESULT////////////////////////////////////");
//                System.out.println("Your Message belongs in "+at.className[largestIndex]+" category");
                
                if(finalValues.get(largestIndex)!=0.0){
                    if(str.compareToIgnoreCase("HAM")==0){
                        confusionMatrix[0][largestIndex]++;
                    }
                    else if(str.compareToIgnoreCase("SPAM")==0){
                        confusionMatrix[1][largestIndex]++;
                    }
                }
            }
            }
            evaluationResult = "";
            evaluationResult = evaluationResult + "\t\tHAM\tSPAM\n";
            System.out.println("\tHAM\tSPAM");
            for(int i=0;i<confusionMatrix.length;i++){
                if(i==0){
                    System.out.print("HAM"+"\t");
                    evaluationResult = evaluationResult + "HAM"+"\t";
                }
                else if(i==1){
                    System.out.print("SPAM"+"\t");
                    evaluationResult = evaluationResult + "SPAM"+"\t";
                }
                for(int j=0;j<confusionMatrix.length;j++){
                    System.out.print(confusionMatrix[i][j]+"\t");
                    evaluationResult = evaluationResult + confusionMatrix[i][j]+"\t\t";
                }
                evaluationResult = evaluationResult + "\n";
                System.out.println();
            }
            double sum= (double)confusionMatrix[0][0] + (double)confusionMatrix[1][1] ;
            evaluationResult = evaluationResult + "Accuracy: "+sum+" / 1672"+" = "+sum/1672.0;
            System.out.println("Accuracy: "+sum+" / 1672"+" = "+sum/1672.0);        
        }
        else
            JOptionPane.showMessageDialog(null, "Now System is ready!");
        System.out.println("Reached at end of constructor");
    }
    public void readFile(String temp){
        indexByDatasetVocab = new HashMap<String, ArrayList<Integer>>();        
        indexByCategory = new HashMap<String, ArrayList<String>>();
        indexByVocabulary = new HashMap<String, ArrayList<String>>();
        indexByVocabValues = new ArrayList<HashMap<String, ArrayList<Double>>>();
        try {
            Scanner scanner = new Scanner(new File(temp));
            int i=0;
            
            String myMessage="",myLevel="";
            int c=0;
            String fullLine="";
            while(scanner.hasNext()){
                String s=scanner.nextLine();                            //reading file
                c++;                                                    //documentId and documentCount handle by this

                    String arr[]=s.split("\t");                        
                    myLevel=arr[0];                                     //getting category
                    myMessage=arr[1];                                   //getting message
                    myMessage=myMessage.replaceAll("[^a-zA-Z0-9 ]", "");//including only alpha-numeric 
                    myMessage = myMessage.toLowerCase();
                    String tmpMsg[] = myMessage.split(" ");
                    for(int j=0;j<tmpMsg.length;j++){                   //removing extra spacing
                        if(tmpMsg[j].contains(" "))
                            tmpMsg[j] = tmpMsg[j].replace(" ", "");
                        if(this.indexByDatasetVocab.containsKey(tmpMsg[j])){
                            ArrayList<Integer> postingExist= indexByDatasetVocab.get(tmpMsg[j]);    //if key exisits in hashmap then add in list
                            postingExist.add(c);
                            indexByDatasetVocab.put(tmpMsg[j],postingExist);
                        }
                        else{
                            ArrayList<Integer> postingNew= new ArrayList<Integer>();                //if key not exists then create one list there
                            postingNew.add(c);
                            indexByDatasetVocab.put(tmpMsg[j], postingNew);
                        }
                    }
                    if(this.indexByCategory.containsKey(myLevel)){                                  //if key exisits in hashmap then add in list
                        ArrayList<String> postingExist= indexByCategory.get(myLevel);
                        postingExist.add(myMessage);
                        indexByCategory.put(myLevel,postingExist);
                    }
                    else{
                        ArrayList<String> postingNew= new ArrayList<String>();                      //if key not exists then create one list there
                        postingNew.add(myMessage);
                        indexByCategory.put(myLevel, postingNew);
                    }
                    String arr2[]=myMessage.split(" ");
                    for(int j=0;j<arr2.length;j++){
                        if(arr2[j].compareToIgnoreCase(" ")!=0 && arr2[j].compareToIgnoreCase("")!=0){
                            if(this.indexByVocabulary.containsKey(myLevel)){                        //if key exisits in hashmap then add in list
                                ArrayList<String> postingExist= indexByVocabulary.get(myLevel);
                                postingExist.add(arr2[j]);
                                indexByVocabulary.put(myLevel,postingExist);
                            }
                            else{
                                ArrayList<String> postingNew= new ArrayList<String>();              //if key not exists then create one list there
                                postingNew.add(arr2[j]);
                                indexByVocabulary.put(myLevel, postingNew);
                            }                        
                        }
                    }
            }
            ArrayList<String> vv = new ArrayList<String>(this.indexByDatasetVocab.keySet());        //getting only keys (means all distinct vocab of data)
            this.totalVocabSize= vv.size();                                                         //storing vocab size
            this.totalDocuments= c;                                                                 //storting document count.
            
            System.out.println("Count: "+this.totalDocuments);   
            ArrayList<String> v = new ArrayList<String>(this.indexByCategory.keySet());             //getting names of all present categories
            className = new String [v.size()];
            classDocCount = new double [v.size()];                                                  //for number of documents in each category
            this.priorValues =  new double [v.size()];                                              //for prior values of each category
            int z=0;
            for (String str : v) {
                ArrayList<String> tmp= this.indexByCategory.get(str);
                className[z]=str;                                                                   //storing all classes names
                classDocCount[z]=(double)tmp.size();                                                //storing number of documents of each class
                z++;
            }
            System.out.println("Prior Values : "); 
            for(int j=0;j<className.length;j++){
                this.priorValues[j]= (classDocCount[j]/(double)c);                                  //storing prior values for each class
                System.out.println("Prior value of "+className[j]+" = "+(this.priorValues[j]));                
            }
            int k=0;    
            for (String str : v) {                                                                      //this loop calculates conditional probability for each vocab with respect to its class
                                                                                                        //and stores them in a hashmap array
                    HashMap<String, ArrayList<Double>> indexByValues = new HashMap<String, ArrayList<Double>>();
                    ArrayList<String> tmp= indexByVocabulary.get(str);
                    int totalWordsInClass= tmp.size();

                    for (String str2 : tmp) {
                        int countt = Collections.frequency(tmp, str2);
//                        int countt = getFrequency(tmp, str2);                        
                        if(str2.compareToIgnoreCase(" ")!=0 && str2.compareToIgnoreCase("")!=0){
                                if(!indexByValues.containsKey(str2)){
                                    countt=countt+1;
                                    double valueee = ((double)countt)/(((double)totalWordsInClass)+((double)totalVocabSize));
                                    ArrayList<Double> postingNew= new ArrayList<Double>();
                                    postingNew.add(valueee);                                           //storing each vocab conditional probability on 0th index of its hashmap 
                                    indexByValues.put(str2, postingNew);
                                }
                        }
                    }
                    indexByVocabValues.add(k, indexByValues);
                    k++;
            }
            scanner.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(spamorham.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private ArrayList<String> duplicateHandler(ArrayList<String> postingList) {
        ArrayList<String> mPosting = new ArrayList<String>();
        for(int i=0;i<postingList.size();i++){
            if(!mPosting.contains(postingList.get(i))) 
                mPosting.add(postingList.get(i));
        }
        return mPosting;
    }
    public void writeOuputVocab() throws IOException{                   //extraction of vocabulary in sorted order from messages and categorized them with
                                                                        //respect to level of message(OAG,NAG,CAG)
        File file = new File("outputVocab.txt");
        
        if ( !file.exists() ) 
            file.createNewFile();
        
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
       
        for (Map.Entry<String, ArrayList<String>> entry : indexByVocabulary.entrySet()) {
            bw.write(entry.getKey()+"-->>"+entry.getValue().size()+" ->> ");
            for(int i=0;i<entry.getValue().size();i++)
                bw.write(entry.getValue().get(i)+ " -> ");
            bw.newLine();  
        }
        
        
        
        bw.flush();
        bw.close();

    }    
    public void writeOuputLevel() throws IOException{                   //.csv message text categorized by there level(OAG,NAG,CAG)

        File file = new File("outputLevel.txt");
        
        if ( !file.exists() ) 
            file.createNewFile();
        
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
       
        for (Map.Entry<String, ArrayList<String>> entry : indexByCategory.entrySet()) {
            bw.write(entry.getKey()+"-->>"+entry.getValue().size()+" ->> ");
            for(int i=0;i<entry.getValue().size();i++)
                bw.write(entry.getValue().get(i)+ " -> ");
            bw.newLine();  
        }
        
        
        
        bw.flush();
        bw.close();

    }
    public int getFrequency(ArrayList<String> listt, String target ){
        int countt=0;
        if(listt!=null){
            for(int i=0;i<listt.size();i++){
                if(listt.get(i).equalsIgnoreCase(target))
                    countt++;
            }
            return countt;
        }
        else 
            return -1;
    }
    public void writeOuputValues() throws IOException{                      //.csv simply reading file and mapping messages to their message id

        for(int j=0;j<this.className.length;j++){
            String fileName="ouputClass_"+(j+1)+".txt";
            File file = new File(fileName);

            if ( !file.exists() ) 
                file.createNewFile();

            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);

            for (Map.Entry<String, ArrayList<Double>> entry : this.indexByVocabValues.get(j).entrySet()) {
                bw.write(entry.getKey()+"-->>"+entry.getValue().size()+" ->> ");
                for(int i=0;i<entry.getValue().size();i++)
                    bw.write(entry.getValue().get(i)+"");
                bw.newLine();  
            }



            bw.flush();
            bw.close();
        }
    }
    public Map<String, ArrayList<String>> getTestData(String st){                   //this function performs same steps as train data to calculate condition probabiliy
        Map<String, ArrayList<String>> tempHash;                                    //but it does for the testing dataset for calculating accuracy from confusion matrix.
        tempHash = new HashMap<String, ArrayList<String>>();
        try {
            Scanner scanner = new Scanner(new File(st));
            int i=0;
            
            String myMessage="",myLevel="";
            int c=0;
            String fullLine="";
            while(scanner.hasNext()){
                String s=scanner.nextLine();
                c++;
                        String arr[]=s.split("\t");
                        myMessage=arr[1];
                        myLevel=arr[0];

                    myMessage=myMessage.replaceAll("[^a-zA-Z0-9 ]", "");  //[^a-zA-Z#-)!(.@:_0-9 ]
                    myMessage = myMessage.toLowerCase();
                    String tmpMsg[] = myMessage.split(" ");             
                    myMessage = "";
                    for(int j=0;j<tmpMsg.length;j++){                              
                        if(tmpMsg[j].contains(" "))
                            tmpMsg[j] = tmpMsg[j].replace(" ", "");
                        myMessage= myMessage + tmpMsg[j] + " ";
                    }
                    
                    
                    if(tempHash.containsKey(myLevel)){
                        ArrayList<String> postingExist= tempHash.get(myLevel);
                        postingExist.add(myMessage);
                        tempHash.put(myLevel,postingExist);
                    }
                    else{
                        ArrayList<String> postingNew= new ArrayList<String>();
                        postingNew.add(myMessage);
                        tempHash.put(myLevel, postingNew);
                    }
                
            }
            scanner.close();
        File file = new File("outputTestData.txt");
        
        if ( !file.exists() ) 
            file.createNewFile();
        
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
       
        for (Map.Entry<String, ArrayList<String>> entry : tempHash.entrySet()) {
            bw.write(entry.getKey()+"-->>"+entry.getValue().size()+" ->> ");
            for(int m=0;m<entry.getValue().size();m++)
                bw.write(entry.getValue().get(m)+ " -> ");
            bw.newLine();  
        }
        
        
        
        bw.flush();
        bw.close();
            
            return tempHash;
        }catch(Exception e){
            return null;
        }
    }
    
    public void writeOuput() throws IOException{                      //.csv simply reading file and mapping messages to their message id

        File file = new File("output.txt");
        
        if ( !file.exists() ) 
            file.createNewFile();
        
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
       
        for (Map.Entry<String, ArrayList<Integer>> entry : indexByDatasetVocab.entrySet()) {
            bw.write(entry.getKey()+"-->>"+entry.getValue().size()+" ->> ");
            for(int i=0;i<entry.getValue().size();i++)
                bw.write(entry.getValue().get(i)+ " -> ");
            bw.newLine();  
        }
        bw.flush();
        bw.close();

    }
    public String getStringResult(String message){
        ArrayList<Double> finalValues = new ArrayList<Double>(); 
        message=message.replaceAll("[^a-zA-Z0-9 ]", "");  //[^a-zA-Z#-)!(.@:_0-9 ]
        message=message.toLowerCase();
        String arr[]= message.split(" ");
        for(int i=0;i<className.length;i++){
            double priorTmp= priorValues[i];
            for(int j=0;j<arr.length;j++){
                if(arr[j].contains(" "))
                    arr[j] = arr[j].replace(" ", "");
                if(indexByDatasetVocab.containsKey(arr[j])){                 //using multinomial NB to calculate but in case of not found using vocabsize for improved accuracy
                    if(indexByVocabValues.get(i).containsKey(arr[j])){
                        double myScore = indexByVocabValues.get(i).get(arr[j]).get(0);
                        priorTmp = priorTmp * myScore;
                    }
                    else{
                        double t= 1.0/(double)totalVocabSize;
                        priorTmp= priorTmp * (t);
//                                priorTmp= priorTmp * 1.0;
                    }
                }
                else{
                    double t= 1.0/(double)totalVocabSize;
                    priorTmp= priorTmp * (t);
//                            priorTmp= priorTmp * 1.0;
                }
            }
            finalValues.add(priorTmp);
        }

        int largestIndex = 0;
        for(int i=1;i<finalValues.size();i++){
            if(finalValues.get(largestIndex)<finalValues.get(i))
                largestIndex=i;
        }
//                for(int i=0;i<finalValues.size();i++){
//                    System.out.println(at.className[i]+" Value:"+finalValues.get(i));
//                }
//            System.out.println("///////////////////////RESULT////////////////////////////////////");
//                System.out.println("Your Message belongs in "+at.className[largestIndex]+" category");
        return className[largestIndex];
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
//        spamorham at=new spamorham("SMSSpamCollection.txt","SMSSpamCollection_test.txt");
        spamorham classifierObjectSearch = new spamorham("SMSSpamCollectionFull.txt","NOTESTFILE");
//        at.readFile("SMSSpamCollection.txt");
//        at.writeOuput();
//        at.writeOuputLevel();
//        at.writeOuputVocab();
//        at.writeOuputValues();
//        Map<String, ArrayList<String>> tempHash;
//        tempHash = new HashMap<String, ArrayList<String>>();
//        tempHash = at.getTestData("SMSSpamCollection_test.txt");
//        int confusionMatrix[][]= new int [at.className.length][at.className.length];
//        for(int i=0;i<confusionMatrix.length;i++){
//            for(int j=0;j<confusionMatrix.length;j++){
//                confusionMatrix[i][j]=0;
//            }
//        }
//        ArrayList<String> v = new ArrayList<String>(tempHash.keySet());
//            for (String str : v) {
//            ArrayList<String> tmp= tempHash.get(str);
//            for(int k=0;k<tmp.size();k++){
//                ArrayList<Double> finalValues = new ArrayList<Double>(); 
//                String message= tmp.get(k);
//                message=message.replaceAll("[^a-zA-Z0-9 ]", "");  //[^a-zA-Z#-)!(.@:_0-9 ]
//                message=message.toLowerCase();
//                String arr[]= message.split(" ");
//                for(int i=0;i<at.className.length;i++){
//                    double priorTmp= at.priorValues[i];
//                    for(int j=0;j<arr.length;j++){
//                        if(arr[j].contains(" "))
//                            arr[j] = arr[j].replace(" ", "");
//                        if(at.indexByDatasetVocab.containsKey(arr[j])){                 //using multinomial NB to calculate but in case of not found using vocabsize for improved accuracy
//                            if(at.indexByVocabValues.get(i).containsKey(arr[j])){
//                                double myScore = at.indexByVocabValues.get(i).get(arr[j]).get(0);
//                                priorTmp = priorTmp * myScore;
//                            }
//                            else{
//                                double t= 1.0/(double)at.totalVocabSize;
//                                priorTmp= priorTmp * (t);
////                                priorTmp= priorTmp * 1.0;
//                            }
//                        }
//                        else{
//                            double t= 1.0/(double)at.totalVocabSize;
//                            priorTmp= priorTmp * (t);
////                            priorTmp= priorTmp * 1.0;
//                        }
//                    }
//                    finalValues.add(priorTmp);
//                }
//
//                int largestIndex = 0;
//                for(int i=1;i<finalValues.size();i++){
//                    if(finalValues.get(largestIndex)<finalValues.get(i))
//                        largestIndex=i;
//                }
////                for(int i=0;i<finalValues.size();i++){
////                    System.out.println(at.className[i]+" Value:"+finalValues.get(i));
////                }
//    //            System.out.println("///////////////////////RESULT////////////////////////////////////");
////                System.out.println("Your Message belongs in "+at.className[largestIndex]+" category");
//                
//                if(finalValues.get(largestIndex)!=0.0){
//                    if(str.compareToIgnoreCase("HAM")==0){
//                        confusionMatrix[0][largestIndex]++;
//                    }
//                    else if(str.compareToIgnoreCase("SPAM")==0){
//                        confusionMatrix[1][largestIndex]++;
//                    }
//                }
//            }
//            }
//            System.out.println("\tHAM\tSPAM");
//            for(int i=0;i<confusionMatrix.length;i++){
//                if(i==0){
//                    System.out.print("HAM"+"\t");
//                }
//                else if(i==1){
//                    System.out.print("SPAM"+"\t");
//                }
//                for(int j=0;j<confusionMatrix.length;j++){
//                    System.out.print(confusionMatrix[i][j]+"\t");
//                }
//                System.out.println();
//            }
//            double sum= (double)confusionMatrix[0][0] + (double)confusionMatrix[1][1] ;
//            System.out.println(sum+" / 1672"+" = "+sum/1672.0);
//        }
    }
    
}
