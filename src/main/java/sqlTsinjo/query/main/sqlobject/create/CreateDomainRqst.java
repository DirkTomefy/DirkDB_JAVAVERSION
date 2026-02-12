package sqlTsinjo.query.main.sqlobject.create;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.DomainRef;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.err.eval.DomainAlreadyExistErr;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.main.sqlobject.create.token.CreateObjectTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.storage.SerdeDomain;


public class CreateDomainRqst extends CreateObjectRqst {
    Domain def;
    


    

    public CreateDomainRqst(String name, Domain def) {
        this.name = name;
        this.def = def;
    }

    public static ParseSuccess<CreateDomainRqst> parseCreateDomain(String input) throws ParseNomException{
           ParseSuccess<Token> requestindicator = CreateObjectTokenizer.scanCreateDomainToken(input);
        String trimmed = requestindicator.remaining().trim();
        // Parser le nom de la table
        ParseSuccess<String> domainNameParse = ParserNomUtil.tagName(trimmed);
        String domainName = domainNameParse.matched();
        String remaining = domainNameParse.remaining().trim();

        if (!remaining.startsWith("(")) {
            throw new ParseNomException(remaining, "'(' no hamaritana ny efitra");
        }
       
        
        remaining = remaining.substring(1).trim();
        ParseSuccess<Domain> domain = CreateTableRqst.parseSingleDomain(remaining);
        remaining=domain.remaining();
        if (!remaining.startsWith(")")) {
            throw new ParseNomException(remaining, "')' no hamanarana ny famaritana ny efitra");
        }

        remaining = remaining.substring(1).trim();

        return new ParseSuccess<CreateDomainRqst>(remaining, new CreateDomainRqst(domainName, domain.matched()));
    }

  
    public Domain getDef() {
        return def;
    }

    public void setDef(Domain def) {
        this.def = def;
    }

    @Override
    public void eval(AppContext ctx) throws EvalErr, IOException {
          if (ctx.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File path = Path.of(ctx.getDataDirectory(), ctx.getDatabaseName(), "domains", this.name + ".json").toFile();
        if (path.exists()) {
            throw new DomainAlreadyExistErr(name);
        } else {
            SerdeDomain serde = new SerdeDomain(ctx, null);
            Domain d=DomainRef.resolveNonPrimitiveDomain(def, serde);
            path.getParentFile().mkdirs();
            path.createNewFile();
            serde.setDomainName(name);
            serde.serializeDomain(d);
        }
    } 
}
