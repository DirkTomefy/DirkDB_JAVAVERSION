package sqlTsinjo.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Comparator;

import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.err.eval.DatabaseNotExistErr;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.err.eval.TableNotFound;
import sqlTsinjo.query.err.eval.ViewNotFound;
import sqlTsinjo.query.main.select.SelectExpr;

public class SerdeRelation {
    AppContext appContext;
    String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SerdeRelation(AppContext appContext, String tableName) {
        this.appContext = appContext;
        this.tableName = tableName;
    }

    public File getTableFile() throws TableNotFound, NoDatabaseSelect {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File file = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "tables", tableName + ".json").toFile();
        if (!file.exists()) {
            throw new TableNotFound(appContext.getDatabaseName(), tableName);
        } else {
            return file;
        }
    }

    /**
     * Vérifie si c'est une vue
     */
    public boolean isView() throws NoDatabaseSelect {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File file = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "views", tableName + ".json").toFile();
        return file.exists();
    }

    /**
     * Vérifie si c'est une table
     */
    public boolean isTable() throws NoDatabaseSelect {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File file = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "tables", tableName + ".json").toFile();
        return file.exists();
    }

    public void serializeRelation(Relation rel) throws IOException, TableNotFound, NoDatabaseSelect {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File tableFile = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "tables", tableName + ".json").toFile();
        tableFile.getParentFile().mkdirs();
        TombstoneManager.clearDeletedMarker(tableFile, appContext.getTombstoneConfig());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(tableFile, rel);
    }

    public Relation deserializeRelation() throws IOException, TableNotFound, NoDatabaseSelect, EvalErr, ParseNomException {
        // D'abord essayer de charger comme table
        if (isTable()) {
            ObjectMapper mapper = new ObjectMapper();
            Relation retour = mapper.readValue(getTableFile(), Relation.class);
            retour.setName(tableName);
            return retour;
        }
        
        // Si pas une table, essayer comme vue
        if (isView()) {
            SerdeView serdeView = new SerdeView(appContext, tableName);
            try {
                return serdeView.evalView();
            } catch (ViewNotFound e) {
                throw new TableNotFound(appContext.getDatabaseName(), tableName);
            }
        }
        
        // Ni table ni vue trouvée
        throw new TableNotFound(appContext.getDatabaseName(), tableName);
    }

    public static void dropDatabase(String databaseName,AppContext appContext) throws IOException, DatabaseNotExistErr {
        if (databaseName.equals(appContext.getDatabaseName()))
            appContext.setDatabaseName(null);
        File file = Path.of(appContext.getDataDirectory(), databaseName).toFile();
        if (!file.exists()) {
            throw new DatabaseNotExistErr(databaseName);
        } else {
            deleteDirectoryRecursively(file.toPath());
        }
    }

    public void dropTable() throws TableNotFound, NoDatabaseSelect, IOException {
        File tableFile = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "tables", tableName + ".json").toFile();
        if (!tableFile.exists()) {
            throw new TableNotFound(appContext.getDatabaseName(), tableName);
        }
        Files.deleteIfExists(tableFile.toPath());
        Files.deleteIfExists(TombstoneManager.tombstoneFor(tableFile, appContext.getTombstoneConfig()).toPath());
    }

    private static void deleteDirectoryRecursively(java.nio.file.Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignore) {
                        }
                    });
        }
    }

    public static void main(String[] args) throws ParseNomException, EvalErr, IOException {
        ParseSuccess<SelectExpr> select = SelectExpr
                .parseExpr("Alaivo * #ao@ code c1 \n #atifitra@ (Alaivo * #ao@ code) ");
        System.out.println("" + select);
        Relation r = select.matched().eval(new AppContext("test", null, true));
        System.out.println("" + r.toStringDebug());
    }
}
