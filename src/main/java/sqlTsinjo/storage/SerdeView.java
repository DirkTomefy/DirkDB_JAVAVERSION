package sqlTsinjo.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.err.eval.ViewNotFound;
import sqlTsinjo.query.main.select.SelectExpr;

/**
 * Classe pour sérialiser et désérialiser les vues.
 * Une vue stocke uniquement le nom et la requête SelectExpr sérialisée.
 */
public class SerdeView {
    private AppContext appContext;
    private String viewName;

    public SerdeView(AppContext appContext, String viewName) {
        this.appContext = appContext;
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    /**
     * Récupère le fichier de la vue
     */
    public File getViewFile() throws ViewNotFound, NoDatabaseSelect {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File file = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "views", viewName + ".json").toFile();
        if (!file.exists() || TombstoneManager.isDeleted(file, appContext.getTombstoneConfig())) {
            throw new ViewNotFound(appContext.getDatabaseName(), viewName);
        }
        return file;
    }

    /**
     * Sérialise une vue (nom + requête SelectExpr)
     */
    public void serializeView(SelectExpr selectExpr) 
            throws IOException, NoDatabaseSelect {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        
        // Créer le dossier views s'il n'existe pas
        File viewsDir = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "views").toFile();
        if (!viewsDir.exists()) {
            viewsDir.mkdirs();
        }

        File file = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "views", viewName + ".json").toFile();
        TombstoneManager.clearDeletedMarker(file, appContext.getTombstoneConfig());
        
        ViewData viewData = new ViewData(viewName, selectExpr);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, viewData);
    }

    /**
     * Désérialise une vue et retourne ses métadonnées
     */
    public ViewData deserializeViewData() throws IOException, ViewNotFound, NoDatabaseSelect {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getViewFile(), ViewData.class);
    }

    /**
     * Évalue la vue et retourne la relation résultante avec les données actuelles
     */
    public Relation evalView() throws IOException, ViewNotFound, NoDatabaseSelect, ParseNomException, EvalErr {
        ViewData viewData = deserializeViewData();
        
        // Évaluer directement la requête SelectExpr stockée
        SelectExpr selectExpr = viewData.getSelectExpr();
        Relation result = selectExpr.eval(appContext);
        
        // Définir le nom de la vue comme nom de la relation
        result.setName(viewName);
        
        return result;
    }

    /**
     * Vérifie si une vue existe
     */
    public static boolean viewExists(AppContext appContext, String viewName) throws NoDatabaseSelect {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File file = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "views", viewName + ".json").toFile();
        return file.exists() && !TombstoneManager.isDeleted(file, appContext.getTombstoneConfig());
    }

    /**
     * Supprime une vue
     */
    public void dropView() throws ViewNotFound, NoDatabaseSelect, IOException {
        File viewFile = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "views", viewName + ".json").toFile();
        if (!viewFile.exists()) {
            throw new ViewNotFound(appContext.getDatabaseName(), viewName);
        }
        TombstoneManager.markDeleted(viewFile, appContext.getTombstoneConfig(), appContext.getInstanceId());
    }

    /**
     * Classe interne pour représenter les données d'une vue stockée
     */
    public static class ViewData {
        private String name;
        private SelectExpr selectExpr;

        public ViewData() {
            // Constructeur par défaut pour Jackson
        }

        public ViewData(String name, SelectExpr selectExpr) {
            this.name = name;
            this.selectExpr = selectExpr;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public SelectExpr getSelectExpr() {
            return selectExpr;
        }

        public void setSelectExpr(SelectExpr selectExpr) {
            this.selectExpr = selectExpr;
        }
    }
}
