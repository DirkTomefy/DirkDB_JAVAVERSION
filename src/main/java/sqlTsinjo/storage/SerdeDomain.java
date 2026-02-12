package sqlTsinjo.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.base.Domain;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.DomainNotFound;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;


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

    public File getDomainFile() throws  NoDatabaseSelect, DomainNotFound {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File file = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "domains", domainName + ".json").toFile();
        if (!file.exists()) {
            throw new DomainNotFound(appContext.getDatabaseName(), domainName);
        } else {
            return file;
        }
    }

    public void serializeDomain(Domain rel) throws IOException,  NoDatabaseSelect, DomainNotFound {
        if (appContext.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File domainFile = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "domains", domainName + ".json").toFile();
        domainFile.getParentFile().mkdirs();
        

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(domainFile, rel);
    }

    public Domain deserializeDomain() throws IOException,  NoDatabaseSelect, DomainNotFound {
        ObjectMapper mapper = new ObjectMapper();
        Domain retour = mapper.readValue(getDomainFile(), Domain.class);
        return retour;
    }

     public void dropDomain() throws  NoDatabaseSelect, IOException, DomainNotFound {
       File domainFile = Path.of(appContext.getDataDirectory(), appContext.getDatabaseName(), "domains", domainName + ".json").toFile();
       if (!domainFile.exists()) {
            throw new DomainNotFound(appContext.getDatabaseName(), domainName);
        }
        Files.deleteIfExists(domainFile.toPath());
    }
}
