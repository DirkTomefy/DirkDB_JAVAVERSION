package query.main.common;

import java.util.Vector;
import query.err.eval.AmbigousNameErr;
import query.err.eval.FieldNotFoundErr;
import query.main.select.element.classes.SelectCtx;

public record QualifiedIdentifier(String origin, String name) {

   public Object getValueFromARow(Vector<QualifiedIdentifier> fieldName, Vector<Object> row,SelectCtx ctx)
         throws AmbigousNameErr, FieldNotFoundErr {

      int index = getIndex(fieldName,ctx);
      return getValueFromARow(fieldName, row, index,ctx);
   }

   public Object getValueFromARow(Vector<QualifiedIdentifier> fieldName, Vector<Object> row, int index,SelectCtx ctx) {
      if (index < 0 || index >= row.size()) {
         throw new IndexOutOfBoundsException("L'index " + index + " invalide pour la ligne de taille: " + row.size());
      }

      return row.get(index);
   }

   public int getIndex(Vector<QualifiedIdentifier> fieldName,SelectCtx ctx)
         throws AmbigousNameErr, FieldNotFoundErr {

      // Vérifier si le QualifiedIdentifier a une origine spécifiée
      if (this.origin() != null && !this.origin().isEmpty()) {
         // Chercher uniquement dans le champ correspondant à l'origine
         return getIndexByOriginAndName(fieldName,ctx);
      } else {
         // Pas d'origine spécifiée, vérifier s'il y a ambiguïté
         return getIndexByNameOnly(fieldName,ctx);
      }
   }

   private int getIndexByOriginAndName(Vector<QualifiedIdentifier> fieldName,SelectCtx ctx)
         throws FieldNotFoundErr {

      for (int i = 0; i < fieldName.size(); i++) {
         QualifiedIdentifier currentQid = fieldName.get(i);
         if (origin.equals(currentQid.origin()) && name.equals(currentQid.name())) {
            return i;
         }
      }

      throw new FieldNotFoundErr(this);
   }

   private int getIndexByNameOnly(Vector<QualifiedIdentifier> fieldName,SelectCtx ctx)
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

}