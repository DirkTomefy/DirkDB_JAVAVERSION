package query.main.common;

import java.util.List;


public record QualifiedIdentifier(String origin,String name)  {
    public int getIndexFromList(List<QualifiedIdentifier> list){
       int index=-1;
       for(int i=0;i<list.size();i++){
          if(list.get(i).name==this.name) return i;
       }   
       return index;
    }
    public int getQteFromList(List<QualifiedIdentifier> list){
        int retour=0;
        for(int i=0;i<list.size();i++){
          if(list.get(i).name==this.name) retour++;
       }   
       return retour;
    }
    public boolean isAmbigousFromList(List<QualifiedIdentifier> list){
        return getIndexFromList(list)==1;
    }
}
