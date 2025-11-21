package base;

import java.util.Arrays;
import java.util.Vector;

public class Individual {
     Vector<Object> values;

     public Vector<Object> getValues() {
          return values;
     }

     public Individual(Object[] lo) {
          values = new Vector<>();
          values.addAll(Arrays.asList(lo));


    }

     public Individual() {
          this.values=new Vector<>();
     }

     public Individual(Vector<Object> values) {
          this.values = values;
     }

     @Override
     public boolean equals(Object other) {
          if (!(other instanceof Individual))
               return false;

          Individual o = (Individual) other;
          if (this.values == null && o.values == null)
               return true;
          if (this.values == null || o.values == null)
               return false;

          if (this.values.size() != o.values.size())
               return false;

          for (int i = 0; i < this.values.size(); i++) {
               Object v1 = this.values.get(i);
               Object v2 = o.values.get(i);
               if (v1 == null && v2 == null)
                    continue;
               if (v1 == null || v2 == null)
                    return false;
               if (!v1.equals(v2))
                    return false;
          }

          return true;
     }

     public Object get(int index) {
         return this.values.get(index);
     }

     public void setValues(Vector<Object> values) {
          this.values = values;
     }

    

}
