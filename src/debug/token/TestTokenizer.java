package debug.token;

import RDP.base.ParseSuccess;
import RDP.base.function.expr.BinaryExpr;
import RDP.base.function.expr.Expression;
import RDP.base.function.expr.PrefixedExpr;
import RDP.err.ParseNomException;
import RDP.token.Token;
import RDP.token.Tokenizer;

public class TestTokenizer {
    public static void main(String[] args) throws ParseNomException {
        ParseSuccess<Token> t=Tokenizer.scanBinopToken("is not null");
        System.out.println("Ok : "+t.matched());
        try {
            ParseSuccess<Expression> expr= Expression.parseExpression.apply("!^");
            System.out.println(""+expr);
            System.out.println("Problème");       
        } catch (ParseNomException e) {
            System.out.println("Ok : "+e);
        }
        
        ParseSuccess<Expression> expr1=Expression.parseExpression.apply("name=age");
        System.out.println("Ok :"+expr1.matched());

        ParseSuccess<Expression> expr2=Expression.parseExpression.apply("!(age1>age2)");
        if(expr2.matched() instanceof PrefixedExpr) {
            System.out.println("Ok :"+expr2.matched());
        }else{
            throw new RuntimeException("Problème de test (expected : PrefixedExpr )"+expr2);
        };

        ParseSuccess<Expression> expr3=Expression.parseExpression.apply("!age1>age2");
         if(expr3.matched() instanceof BinaryExpr) {
            System.out.println("Ok :"+expr3.matched());
        }else{
            throw new RuntimeException("Problème de test (expected : BinaryExpr ) "+expr3);
        };

        ParseSuccess<Expression> expr4=Expression.parseExpression.apply("(((age)=age*1)/100)*10");
        System.out.println("Ok :"+expr4.matched());

        ParseSuccess<Expression> expr5=Expression.parseExpression.apply("--13");
        System.out.println("Ok :"+expr5.matched());

        ParseSuccess<Token> t1=Tokenizer.scanBinopToken("is");
        System.out.println("Ok :"+t1.matched()); 
        ParseSuccess<Expression> expr6=Expression.parseExpression.apply("ville is null");
        System.out.println("Ok :"+expr6.matched());

    }
}
