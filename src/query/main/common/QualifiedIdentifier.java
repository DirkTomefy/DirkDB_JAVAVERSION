package query.main.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import query.err.eval.AmbigousNameErr;
import query.err.eval.FieldNotFoundErr;
import query.main.select.element.classes.SelectCtx;

public record QualifiedIdentifier(String origin, String name) {

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
      if (this.origin() == null || this.origin().isEmpty()) {
         return null;
      }

      if (ctx != null && ctx.getAliasmap() != null && ctx.getAliasmap().containsKey(this.origin())) {
         return ctx.getAliasmap().get(this.origin());
      }

      return this.origin();
   }

   private int findIndexWithOrigin(Vector<QualifiedIdentifier> fieldName, String resolvedOrigin, SelectCtx ctx)
         throws FieldNotFoundErr {

      // Recherche avec origine résolue
      int index = findExactMatch(fieldName, resolvedOrigin, this.name);
      if (index != -1)
         return index;

      // Recherche avec alias original (si différent)
      if (!this.origin().equals(resolvedOrigin)) {
         index = findExactMatch(fieldName, this.origin(), this.name);
         if (index != -1)
            return index;
      }

      // Dernier recours: chercher sans origine si unique
      return findUniqueFieldWithoutOrigin(fieldName);
   }

   private int findIndexWithoutOrigin(Vector<QualifiedIdentifier> fieldName, SelectCtx ctx)
         throws AmbigousNameErr, FieldNotFoundErr {

      SearchResult result = countFieldOccurrences(fieldName, this.name);

      if (result.count == 1) {
         return result.foundIndex;
      }

      if (result.count > 1) {
         return resolveAmbiguity(fieldName, ctx, result.count);
      }

      // Si non trouvé, essayer avec origine implicite
      return tryWithImplicitOrigin(fieldName, ctx);
   }

   // ========== MÉTHODES HELPER PRIVÉES ==========

   private int findExactMatch(Vector<QualifiedIdentifier> fieldName, String originToMatch, String nameToMatch) {
      for (int i = 0; i < fieldName.size(); i++) {
         QualifiedIdentifier currentQid = fieldName.get(i);
         if (nameToMatch.equals(currentQid.name()) && originToMatch.equals(currentQid.origin())) {
            return i;
         }
      }
      return -1;
   }

   private int findUniqueFieldWithoutOrigin(Vector<QualifiedIdentifier> fieldName) throws FieldNotFoundErr {
      int count = 0;
      int foundIndex = -1;

      for (int i = 0; i < fieldName.size(); i++) {
         QualifiedIdentifier currentQid = fieldName.get(i);
         if (this.name.equals(currentQid.name()) &&
               (currentQid.origin() == null || currentQid.origin().isEmpty())) {
            count++;
            foundIndex = i;
         }
      }

      if (count == 1) {
         return foundIndex;
      }

      throw new FieldNotFoundErr(this);
   }

   private SearchResult countFieldOccurrences(Vector<QualifiedIdentifier> fieldName, String fieldNameToSearch) {
      int count = 0;
      int foundIndex = -1;

      for (int i = 0; i < fieldName.size(); i++) {
         QualifiedIdentifier currentQid = fieldName.get(i);
         if (fieldNameToSearch.equals(currentQid.name())) {
            count++;
            foundIndex = i;
         }
      }

      return new SearchResult(count, foundIndex);
   }

   private int resolveAmbiguity(Vector<QualifiedIdentifier> fieldName, SelectCtx ctx, int totalCount)
         throws AmbigousNameErr {

      if (ctx != null && ctx.getAliasmap() != null) {
         Map<String, Integer> originCounts = countOccurrencesByResolvedOrigin(fieldName, ctx);

         if (originCounts.size() == 1) {
            // Toutes les occurrences viennent de la même table après résolution
            return findFirstMatchingIndex(fieldName, this.name);
         }
      }

      throw new AmbigousNameErr(
            "Le champ '" + name + "' est ambigu. Il apparaît " + totalCount + " fois dans la relation.");
   }

   private Map<String, Integer> countOccurrencesByResolvedOrigin(Vector<QualifiedIdentifier> fieldName, SelectCtx ctx) {
      Map<String, Integer> originCounts = new HashMap<>();

      for (QualifiedIdentifier currentQid : fieldName) {
         if (this.name.equals(currentQid.name())) {
            String resolvedOrigin = currentQid.origin();
            if (resolvedOrigin != null && ctx.getAliasmap().containsKey(resolvedOrigin)) {
               resolvedOrigin = ctx.getAliasmap().get(resolvedOrigin);
            }
            originCounts.put(resolvedOrigin, originCounts.getOrDefault(resolvedOrigin, 0) + 1);
         }
      }

      return originCounts;
   }

   private int findFirstMatchingIndex(Vector<QualifiedIdentifier> fieldName, String nameToFind) {
      for (int i = 0; i < fieldName.size(); i++) {
         if (nameToFind.equals(fieldName.get(i).name())) {
            return i;
         }
      }
      return -1;
   }

   private int tryWithImplicitOrigin(Vector<QualifiedIdentifier> fieldName, SelectCtx ctx)
         throws AmbigousNameErr, FieldNotFoundErr {

      if (ctx != null && ctx.getAliasmap() != null && ctx.getAliasmap().size() == 1) {
         String implicitOrigin = ctx.getAliasmap().keySet().iterator().next();
         QualifiedIdentifier implicitQid = new QualifiedIdentifier(implicitOrigin, this.name);
         return implicitQid.findIndexWithOrigin(fieldName, implicitOrigin, ctx);
      }

      throw new FieldNotFoundErr(this);
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
}