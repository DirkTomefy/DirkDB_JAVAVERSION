package query.main.common;

import java.util.Vector;
import query.err.eval.AmbigousNameErr;
import query.err.eval.FieldNotFoundErr;

public record QualifiedIdentifier(String origin, String name) {

   public Object getValueFromARow(Vector<QualifiedIdentifier> fieldName, Vector<Object> row)
         throws AmbigousNameErr, FieldNotFoundErr {

      int index = getIndex(fieldName);
      return getValueFromARow(fieldName, row, index);
   }

   public Object getValueFromARow(Vector<QualifiedIdentifier> fieldName, Vector<Object> row, int index) {
      if (index < 0 || index >= row.size()) {
         throw new IndexOutOfBoundsException("L'index " + index + " invalide pour la ligne de taille: " + row.size());
      }

      return row.get(index);
   }

   public int getIndex(Vector<QualifiedIdentifier> fieldName)
         throws AmbigousNameErr, FieldNotFoundErr {

      // Vérifier si le QualifiedIdentifier a une origine spécifiée
      if (this.origin() != null && !this.origin().isEmpty()) {
         // Chercher uniquement dans le champ correspondant à l'origine
         return getIndexByOriginAndName(fieldName);
      } else {
         // Pas d'origine spécifiée, vérifier s'il y a ambiguïté
         return getIndexByNameOnly(fieldName);
      }
   }

   private int getIndexByOriginAndName(Vector<QualifiedIdentifier> fieldName)
         throws FieldNotFoundErr {

      for (int i = 0; i < fieldName.size(); i++) {
         QualifiedIdentifier currentQid = fieldName.get(i);
         if (origin.equals(currentQid.origin()) && name.equals(currentQid.name())) {
            return i;
         }
      }

      throw new FieldNotFoundErr(this);
   }

   private int getIndexByNameOnly(Vector<QualifiedIdentifier> fieldName)
         throws AmbigousNameErr, FieldNotFoundErr {

      int foundIndex = -1;
      int count = 0;

      // Compter les occurrences du nom dans les fieldName
      for (int i = 0; i < fieldName.size(); i++) {
         QualifiedIdentifier currentQid = fieldName.get(i);
         if (name.equals(currentQid.name())) {
            count++;
            foundIndex = i;
         }
      }

      // Vérifier l'ambiguïté
      if (count > 1) {
         throw new AmbigousNameErr(
               "Le champ '" + name + "' est ambigu. Il apparaît " + count + " fois dans la relation.");
      }

      if (count == 1 && foundIndex >= 0) {
         return foundIndex;
      }

      throw new FieldNotFoundErr(this);
   }

   // Version alternative de getValueFromARow qui utilise getIndex
   public Object getValueFromARow2(Vector<QualifiedIdentifier> fieldName, Vector<Object> row)
         throws AmbigousNameErr, FieldNotFoundErr {
      int index = getIndex(fieldName);
      return row.get(index);
   }
}