package sqlTsinjo.query.main.common;

import java.util.Vector;

import sqlTsinjo.query.err.eval.AmbigousNameErr;
import sqlTsinjo.query.err.eval.FieldNotFoundErr;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public class QualifiedIdentifier {
   String origin;
   String name;

   public QualifiedIdentifier() {
   }

   public QualifiedIdentifier(String origin, String name) {
      this.origin = origin;
      this.name = name;
   }

   public static QualifiedIdentifier buildFromName(String name) {
      return new QualifiedIdentifier(null, name);
   }

   public static Vector<QualifiedIdentifier> into(Vector<String> arg){
      Vector<QualifiedIdentifier> result=new Vector<>();
      for (String name : arg) {
            result.add(buildFromName(name));
      }
      return result;
   }

   public Object getValueFromARow(Vector<QualifiedIdentifier> fieldName, Vector<Object> row, SelectCtx ctx)
         throws AmbigousNameErr, FieldNotFoundErr {
      int index = getIndex(fieldName, ctx);
      return getValueAtIndex(row, index);
   }

   public Object getValueFromARow(Vector<QualifiedIdentifier> fieldName, Vector<Object> row, int index, SelectCtx ctx)
         throws AmbigousNameErr, FieldNotFoundErr {
      return getValueAtIndex(row, index);
   }

   public Object getValueAtIndex(Vector<Object> row, int index) {
      if (index < 0 || index >= row.size()) {
         throw new IndexOutOfBoundsException("L'index " + index + " invalide pour la ligne de taille: " + row.size());
      }
      return row.get(index);
   }

   public int getIndex(Vector<QualifiedIdentifier> fieldName, SelectCtx ctx)
         throws AmbigousNameErr, FieldNotFoundErr {
      String resolvedOrigin = resolveOrigin(ctx);

      if (resolvedOrigin != null && !resolvedOrigin.isEmpty()) {
         return findIndexWithOrigin(fieldName, resolvedOrigin, ctx);
      } else {
         return findIndexWithoutOrigin(fieldName, ctx);
      }
   }

   private String resolveOrigin(SelectCtx ctx) {
      if (this.getOrigin() == null || this.getOrigin().isEmpty()) {
         return null;
      }

      if (ctx != null && ctx.getAliasmap() != null && ctx.getAliasmap().containsKey(this.getOrigin())) {
         return ctx.getAliasmap().get(this.getOrigin());
      }
      

      return this.getOrigin();
   }

   private int findIndexWithOrigin(Vector<QualifiedIdentifier> fieldName, String resolvedOrigin, SelectCtx ctx)
         throws FieldNotFoundErr {

      // Recherche avec origine résolue
      int index = findExactMatch(fieldName, resolvedOrigin, this.name);
      if (index != -1)
         return index;

      // Recherche avec alias original (si différent)
      if (this.getOrigin() != null && !this.getOrigin().equals(resolvedOrigin)) {
         index = findExactMatch(fieldName, this.getOrigin(), this.name);
         if (index != -1)
            return index;
      }

      // Dernier recours: chercher par nom uniquement si unique
      SearchResult result = countFieldOccurrences(fieldName, this.name);
      if (result.count == 1) {
         return result.foundIndex;
      } else if (result.count == 0) {
         throw new FieldNotFoundErr("Le champ '" + this.getOrigin() + "." + name + "' n'existe pas dans la relation.");
      } else {
         throw new FieldNotFoundErr("Le champ '" + name + "' est ambigu avec l'origine '" + resolvedOrigin + "'.");
      }
   }

   private int findIndexWithoutOrigin(Vector<QualifiedIdentifier> fieldName, SelectCtx ctx)
         throws AmbigousNameErr, FieldNotFoundErr {

      SearchResult result = countFieldOccurrences(fieldName, this.name);
      if (result.count == 1) {
         return result.foundIndex;
      } else if (result.count == 0) {
         throw new FieldNotFoundErr("Le champ '" + name + "' n'existe pas dans la relation.");
      } else {
         throw new AmbigousNameErr(
               "Le champ '" + name + "' est ambigu. Il apparaît " + result.count + " fois dans la relation.");
      }
   }

   // ========== MÉTHODES HELPER PRIVÉES ==========

   private int findExactMatch(Vector<QualifiedIdentifier> fieldName, String originToMatch, String nameToMatch) {
      for (int i = 0; i < fieldName.size(); i++) {
         QualifiedIdentifier currentQid = fieldName.get(i);
         // System.out.println(""+currentQid);
         if (nameToMatch.equals(currentQid.getName()) && originToMatch.equals(currentQid.getOrigin())) {
            return i;
         }
      }
      return -1;
   }


   private SearchResult countFieldOccurrences(Vector<QualifiedIdentifier> fieldName, String fieldNameToSearch) {
      int count = 0;
      int foundIndex = -1;

      for (int i = 0; i < fieldName.size(); i++) {
         QualifiedIdentifier currentQid = fieldName.get(i);
         if (fieldNameToSearch.equals(currentQid.getName())) {
            count++;
            foundIndex = i;
         }
      }
      // System.out.println("count : "+count);

      return new SearchResult(count, foundIndex);
   }

   // ========== MÉTHODES PUBLIQUES UTILITAIRES ==========

   public String getResolvedOrigin(SelectCtx ctx) {
      return resolveOrigin(ctx);
   }

   public boolean referencesTable(String tableName, SelectCtx ctx) {
      String resolved = getResolvedOrigin(ctx);
      return resolved != null && resolved.equals(tableName);
   }

   // ========== CLASSES HELPER INTERNES ==========

   private static class SearchResult {
      final int count;
      final int foundIndex;

      SearchResult(int count, int foundIndex) {
         this.count = count;
         this.foundIndex = foundIndex;
      }
   }

   public String getOrigin() {
      return origin;
   }

   public void setOrigin(String origin) {
      this.origin = origin;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public String toString() {
      return "QualifiedIdentifier [origin=" + origin + ", name=" + name + "]";
   }

}