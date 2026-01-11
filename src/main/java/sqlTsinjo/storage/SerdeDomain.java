package sqlTsinjo.storage;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.base.Domain;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.err.eval.TableNotFound;

public class SerdeDomain {
    AppContext appContext;
    String domainName;

    public SerdeDomain(AppContext appContext, String domainName) {
        this.appContext = appContext;
        this.domainName = domainName;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public File getDomainFile() throws TableNotFound, NoDatabaseSelect {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        String path = "databases/" + appContext.getDatabaseName() + "/domains/" + domainName + ".json";
        File file = new File(path);
        if (!file.exists()) {
            throw new TableNotFound(appContext.getDatabaseName(), domainName);
        } else {
            return file;
        }
    }

    public void serializeDomain(Domain rel) throws IOException, TableNotFound, NoDatabaseSelect {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(getDomainFile(), rel);
    }

    public Domain deserializeDomain() throws IOException, TableNotFound, NoDatabaseSelect {
        ObjectMapper mapper = new ObjectMapper();
        Domain retour = mapper.readValue(getDomainFile(), Domain.class);
        return retour;
    }
}
