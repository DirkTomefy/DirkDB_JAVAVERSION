package sqlTsinjo.query.main.sqlobject.create;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Vector;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.DomainAtom;
import sqlTsinjo.base.DomainEnum;
import sqlTsinjo.base.DomainRef;
import sqlTsinjo.base.Relation;
import sqlTsinjo.base.domains.DATE;
import sqlTsinjo.base.domains.NUMBER;
import sqlTsinjo.base.domains.VARCHAR;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.err.eval.TableAlreadyExistErr;
import sqlTsinjo.query.main.sqlobject.create.token.CreateObjectTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.storage.SerdeRelation;

public class CreateTableRqst extends CreateObjectRqst {
    Vector<String> fieldName;
    Vector<Domain> domains;
    Vector<HashSet<String>> domainNonPrimitive;

    public CreateTableRqst(String name, Vector<String> fieldName, Vector<Domain> domains,
            Vector<HashSet<String>> domainNonPrimitive) {
        this.name = name;
        this.fieldName = fieldName;
        this.domains = domains;
        this.domainNonPrimitive = domainNonPrimitive;
    }

    @Override
    public void eval(AppContext ctx) throws EvalErr, IOException {
        if (ctx.getDatabaseName() == null)
            throw new NoDatabaseSelect();
        File path = new File("databases/" + ctx.getDatabaseName() + "/tables/" + this.name + ".json");
        if (path.exists()) {
            throw new TableAlreadyExistErr(name);
        } else {
            DomainRef.resolveAllNonPrimitiveDomains(domains, ctx);
            path.createNewFile();
            Relation rel = new Relation(name, fieldName, domains);
            SerdeRelation seralizer = new SerdeRelation(ctx, name);
            seralizer.serializeRelation(rel);
        }

    }

    public static ParseSuccess<CreateTableRqst> parseCreateTable(String input) throws ParseNomException {
        ParseSuccess<Token> requestindicator = CreateObjectTokenizer.scanCreateTableToken(input);
        String trimmed = requestindicator.remaining().trim();
        // Parser le nom de la table
        ParseSuccess<String> tableNameParse = ParserNomUtil.tagName(trimmed);
        String tableName = tableNameParse.matched();
        String remaining = tableNameParse.remaining().trim();

        // Initialiser les vecteurs
        Vector<String> fieldNames = new Vector<>();
        Vector<Domain> fieldDomains = new Vector<>();

        Vector<HashSet<String>> nonPrimitive = new Vector<>();
        // Vérifier s'il y a des champs (commence par '(')
        if (!remaining.startsWith("(")) {
            throw new ParseNomException(remaining, "Liste de champs attendue après le nom de la table");
        }

        // Consommer '('
        remaining = remaining.substring(1).trim();

        // Parser les champs tant qu'on n'arrive pas à ')'
        while (!remaining.startsWith(")")) {
            // Parser le nom du champ
            ParseSuccess<String> fieldNameParse = ParserNomUtil.tagName(remaining);
            String fieldName = fieldNameParse.matched();
            remaining = fieldNameParse.remaining().trim();

            // Parser le domaine du champ

            ParseSuccess<Domain> domainParse = parseSingleDomain(remaining);
            Domain domain = domainParse.matched();
            remaining = domainParse.remaining().trim();

            // Ajouter aux vecteurs
            fieldNames.add(fieldName);
            fieldDomains.add(domain);

            // Vérifier s'il y a un autre champ (séparé par ',') ou la fin ')'
            if (remaining.startsWith(",")) {
                // Consommer la virgule
                remaining = remaining.substring(1).trim();
                if (remaining.startsWith(")")) {
                    throw new ParseNomException(remaining, "Champ attendu après la virgule");
                }
            } else if (!remaining.startsWith(")")) {
                throw new ParseNomException(remaining, "Virgule ou parenthèse fermante attendue");
            }
        }

        remaining = remaining.substring(1).trim();

        return new ParseSuccess<>(remaining,
                new CreateTableRqst(tableName, fieldNames, fieldDomains, nonPrimitive));
    }

    public static ParseSuccess<Domain> parseSingleDomain(String input) throws ParseNomException {
        Vector<DomainAtom> domainAtoms = new Vector<>();
        String remaining = input.trim();

        // Parser le premier domain atom
        ParseSuccess<DomainAtom> firstAtomParse = parseDomainAtom(remaining);
        domainAtoms.add(firstAtomParse.matched());
        remaining = firstAtomParse.remaining().trim();

        // Continuer tant qu'on trouve des ':' suivis d'autres domain atoms
        while (remaining.startsWith(":")) {
            // Consommer le séparateur ':'
            remaining = remaining.substring(1).trim();

            if (remaining.isEmpty()) {
                throw new ParseNomException(input, "DomainAtom attendu après ':'");
            }

            // Parser le domain atom suivant
            ParseSuccess<DomainAtom> nextAtomParse = parseDomainAtom(remaining);

            domainAtoms.add(nextAtomParse.matched());
            remaining = nextAtomParse.remaining().trim();
        }

        // Créer le domaine avec tous les atoms
        Domain domain = new Domain(domainAtoms);
        return new ParseSuccess<>(remaining, domain);
    }

    public static ParseSuccess<DomainAtom> parseDomainAtom(String input) throws ParseNomException {
        try {

            return ParserNomUtil
                    .alt(
                            CreateTableRqst::parseNumber,
                            CreateTableRqst::parseVarChar,
                            CreateTableRqst::parseDate,
                            CreateTableRqst::parseDomainEnum)
                    .apply(input);
        } catch (ParseNomException e) {

            ParseSuccess<String> nameParse = ParserNomUtil.tagName(input);
            return nameParse.map(domainName -> {
                return new DomainRef(domainName);
            });
        }
    }

    public static ParseSuccess<DomainAtom> parseNumber(String input) throws ParseNomException {
        ParseSuccess<String> numberIndicator = ParserNomUtil.tagNoCase("ISA").apply(input.trim());
        ParseSuccess<String> parens = ParserNomUtil.tag("(").apply(numberIndicator.remaining().trim());
        ParseSuccess<String> paramin = ParserNomUtil.takeWhile1(c -> {
            return !c.equals(',');
        }).apply(parens.remaining());
        ParseSuccess<String> comma = ParserNomUtil.tag(",").apply(paramin.remaining());
        ParseSuccess<String> paramax = ParserNomUtil.takeWhile1(c -> {
            return !c.equals(')');
        }).apply(comma.remaining());

        ParseSuccess<String> lastparens = ParserNomUtil.tag(")").apply(paramax.remaining());
        Double paraminDouble = null;
        Double paramaxDouble = null;
        try {
            if (!paramin.matched().toString().trim().equalsIgnoreCase("null")) {
                paraminDouble = Double.parseDouble(paramin.matched().trim());
            }
        } catch (Exception ignore) {
            throw new ParseNomException(input, "impossible de parser en nombre :" + paramin.matched().toString());
        }

        try {
            if (!paramax.matched().toString().trim().equalsIgnoreCase("null")) {
                paramaxDouble = Double.parseDouble(paramax.matched().trim());
            }
        } catch (Exception ignore) {
            throw new ParseNomException(input, "impossible de parser en nimbre :" + paramax.matched().toString());
        }

        return new ParseSuccess<DomainAtom>(lastparens.remaining(), new NUMBER(paraminDouble, paramaxDouble));
    }

    public static ParseSuccess<DomainAtom> parseVarChar(String input) throws ParseNomException {
        ParseSuccess<String> numberIndicator = ParserNomUtil.tagNoCase("LAHATSORATRA").apply(input.trim());
        ParseSuccess<String> parens = ParserNomUtil.tag("(").apply(numberIndicator.remaining().trim());
        ParseSuccess<String> paramin = ParserNomUtil.takeWhile1(c -> {
            return !c.equals(',');
        }).apply(parens.remaining());
        ParseSuccess<String> comma = ParserNomUtil.tag(",").apply(paramin.remaining());
        ParseSuccess<String> paramax = ParserNomUtil.takeWhile1(c -> {
            return !c.equals(')');
        }).apply(comma.remaining());

        ParseSuccess<String> lastparens = ParserNomUtil.tag(")").apply(paramax.remaining());

        Integer paraminDouble = null;
        Integer paramaxDouble = null;

        try {
            if (!paramin.matched().toString().trim().equalsIgnoreCase("null")) {
                paraminDouble = (int) Double.parseDouble(paramin.matched().trim());
            }
        } catch (Exception ignore) {
            throw new ParseNomException(input, "impossible de parser en integer :" + paramin.matched().toString());
        }

        try {
            if (!paramax.matched().toString().trim().equalsIgnoreCase("null")) {
                paramaxDouble = (int) Double.parseDouble(paramax.matched().trim());
            }
        } catch (Exception ignore) {
            throw new ParseNomException(input, "impossible de parser en integer :" + paramax.matched().toString());
        }

        return new ParseSuccess<DomainAtom>(lastparens.remaining(), new VARCHAR(paraminDouble, paramaxDouble));
    }

    public static ParseSuccess<DomainAtom> parseDate(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.tagNoCase("DATY").apply(input.trim());
        return success.map(ignored -> {
            return new DATE();
        });
    }

    public static ParseSuccess<DomainAtom> parseDomainEnum(String input) throws ParseNomException {
        String trimmed = input.trim();

        // Parser l'indicateur ENUM
        ParseSuccess<String> enumIndicator = ParserNomUtil.tagNoCase("TANISA").apply(trimmed);
        String remaining = enumIndicator.remaining().trim();

        // Parser les valeurs entre parenthèses
        ParseSuccess<HashSet<Object>> valuesParse = parseEnumValues(remaining);

        // Créer le DomainEnum
        DomainEnum domainEnum = new DomainEnum(valuesParse.matched());
        return new ParseSuccess<>(valuesParse.remaining(), domainEnum);
    }

    private static ParseSuccess<HashSet<Object>> parseEnumValues(String input) throws ParseNomException {
        String remaining = input.trim();

        // Vérifier la parenthèse ouvrante
        if (!remaining.startsWith("(")) {
            throw new ParseNomException(remaining, "Parenthèse ouvrante attendue après TANISA");
        }
        remaining = remaining.substring(1).trim();

        HashSet<Object> allowedValues = new HashSet<>();

        // Parser la première valeur (obligatoire)
        ParseSuccess<Object> firstValue = parseEnumValue(remaining);
        allowedValues.add(firstValue.matched());
        remaining = firstValue.remaining().trim();

        // Parser les valeurs suivantes séparées par des virgules
        while (remaining.startsWith(",")) {
            remaining = remaining.substring(1).trim();

            if (remaining.startsWith(")")) {
                throw new ParseNomException(remaining, "Valeur attendue après la virgule");
            }

            ParseSuccess<Object> nextValue = parseEnumValue(remaining);
            allowedValues.add(nextValue.matched());
            remaining = nextValue.remaining().trim();
        }

        // Vérifier la parenthèse fermante
        if (!remaining.startsWith(")")) {
            throw new ParseNomException(remaining, "Parenthèse fermante attendue");
        }
        remaining = remaining.substring(1).trim();

        return new ParseSuccess<>(remaining, allowedValues);
    }

    private static ParseSuccess<Object> parseEnumValue(String input) throws ParseNomException {
        String remaining = input.trim();

        // Essayer de parser un nombre décimal
        try {
            ParseSuccess<Double> decimalParse = ParserNomUtil.decimal1(remaining);
            return new ParseSuccess<>(decimalParse.remaining(), decimalParse.matched());
        } catch (ParseNomException e) {
            // Continuer pour essayer les chaînes
        }

        // Essayer de parser une chaîne entre guillemets ou un mot simple
        return ParserNomUtil.alt(
                in -> parseQuotedString('\'', in),
                in -> parseQuotedString('"', in),
                in -> parseSimpleWord(in)).apply(remaining);
    }

    private static ParseSuccess<Object> parseQuotedString(char quoteChar, String input) throws ParseNomException {
        ParseSuccess<String> stringParse = ParserNomUtil.tagString(quoteChar).apply(input);
        return new ParseSuccess<>(stringParse.remaining(), stringParse.matched());
    }

    private static ParseSuccess<Object> parseSimpleWord(String input) throws ParseNomException {
        ParseSuccess<String> wordParse = ParserNomUtil.tagName(input);
        return new ParseSuccess<>(wordParse.remaining(), wordParse.matched());
    }

    public static void main(String[] args) throws ParseNomException {
        System.out.println(
                "" + parseCreateTable(
                        "MANAMBOARA TABILAO TOMEFY ( tay LAHATSORATRA(null,null) , tomefy DATy:TANIsa('tay',1) )"));

    }

    @Override
    public String toString() {
        return "CreateTableRqst [tableName=" + name + ", fieldName=" + fieldName + ", domains=" + domains + "]";
    }

}
