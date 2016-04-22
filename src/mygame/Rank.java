/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.util.Comparator;
import java.util.StringTokenizer;

/**
 *
 * @author 2
 */
public class Rank {
    String name="";
    String date="";
    String duration="";
    
    
      public Rank(){
      }
    
    public Rank(String name,String date,String duration){
    this.name=name;
    this.date=date;
    this.duration=duration; 
    }
    
    public static Rank parseAsRank(String s){
        System.out.println(s);
     String[] array = s.split(",");
         System.out.println(array[2]);
    return new Rank(array[0],array[1],array[2]);
    }
               
     public static Comparator<Rank> durationComparator = new Comparator<Rank>() {

        public int compare(Rank r1, Rank r2) {
            	
	   int d1 =Integer.valueOf(r1.duration);
	    int d2 =Integer.valueOf(r2.duration);
            System.out.println(d1+"     "+d2);
	   //ascending order
	   return d1-d2;

	   //descending order
	   //return StudentName2.compareTo(StudentName1);change body of generated methods, choose Tools | Templates.
        }
    } ;

    
}


