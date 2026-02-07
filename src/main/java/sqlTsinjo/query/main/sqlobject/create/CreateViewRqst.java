package sqlTsinjo.query.main.sqlobject.create;

import java.io.File;
import java.io.IOException;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.err.eval.ViewAlreadyExistErr;
import sqlTsinjo.query.main.select.SelectExpr;
import sqlTsinjo.query.main.sqlobject.create.token.CreateObjectTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.storage.SerdeView;

public class CreateViewRqst extends CreateObjectRqst {
    private SelectExpr expr;

    public CreateViewRqst(String name, SelectExpr expr) {
        this.name = name;
        this.expr = expr;
    }

    public SelectExpr getExpr() {
        return expr;
    }

    @Override
    public void eval(AppContext ctx) throws EvalErr, IOException {
        if (ctx.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        
        // Créer le dossier views s'il n'existe pas
        File viewsDir = new File("databases/" + ctx.getDatabaseName() + "/views");
        if (!viewsDir.exists()) {
            viewsDir.mkdirs();
        }
        
        File viewFile = new File("databases/" + ctx.getDatabaseName() + "/views/" + this.name + ".json");
        if (viewFile.exists()) {
            throw new ViewAlreadyExistErr(name);
        }
        
        // Évaluer d'abord la requête pour vérifier qu'elle fonctionne
        try {
            expr.eval(ctx);
        } catch (ParseNomException e) {
            throw new EvalErr("Erreur lors de l'évaluation de la requête de la vue: " + e.getMessage());
        }
        
        // Créer le fichier de vue
        viewFile.createNewFile();
        
        // Sérialiser la vue (nom + SelectExpr uniquement)
        SerdeView serdeView = new SerdeView(ctx, name);
        serdeView.serializeView(expr);
    }

    public static ParseSuccess<CreateViewRqst> parseCreateView(String input) throws ParseNomException {
        // Parse "manamboara jery"
        ParseSuccess<Token> requestIndicator = CreateObjectTokenizer.scanCreateViewToken(input);
        String trimmed = requestIndicator.remaining().trim();
        
        // Parse le nom de la vue
        ParseSuccess<String> viewNameParse = ParserNomUtil.tagName(trimmed);
        String viewName = viewNameParse.matched();
        String remaining = viewNameParse.remaining().trim();
        
        // Parse le séparateur "@"
        if (!remaining.startsWith("@")) {
            throw new ParseNomException(remaining, "Le caractère '@' est attendu après le nom de la vue");
        }
        remaining = remaining.substring(1).trim();
        
        // Parse la requête SELECT
        ParseSuccess<SelectExpr> selectExprParse = SelectExpr.parseExpr(remaining);
        SelectExpr selectExpr = selectExprParse.matched();
        
        return new ParseSuccess<>(selectExprParse.remaining(), 
                new CreateViewRqst(viewName, selectExpr));
    }
}
