package debug.function;

import base.Relation;
import base.err.EvalErr;
import base.err.ParseNomException;
import base.err.RelationDomainSizeErr;

public class TestJointureExterne {

    // public static void testLeftJoin(int id, Relation r1, Relation r2, String condition)
    //         throws ParseNomException, EvalErr {

    //     System.out.println("TEST " + id + " : JOINTURE EXTERNE GAUCHE");
    //     System.out.println("Relation gauche :\n" + r1.toStringDebug());
    //     System.out.println("Relation droite :\n" + r2.toStringDebug());

    //     Relation res = r1.jointureExterneGauche(r2, condition);

    //     System.out.println("Condition : " + condition);
    //     System.out.println("Résultat :\n" + res.toStringDebug());
    //     System.out.println("--------------------------------------------------\n");
    // }

    // public static void testRightJoin(int id, Relation r1, Relation r2, String condition)
    //         throws ParseNomException, EvalErr {

    //     System.out.println("TEST " + id + " : JOINTURE EXTERNE DROITE");
    //     System.out.println("Relation gauche :\n" + r1.toStringDebug());
    //     System.out.println("Relation droite :\n" + r2.toStringDebug());

    //     Relation res = r1.jointureExterneDroite(r2, condition);

    //     System.out.println("Condition : " + condition);
    //     System.out.println("Résultat :\n" + res.toStringDebug());
    //     System.out.println("--------------------------------------------------\n");
    // }

    // public static void testFullJoin(int id, Relation r1, Relation r2, String condition)
    //         throws ParseNomException, EvalErr, RelationDomainSizeErr {

    //     System.out.println("TEST " + id + " : JOINTURE EXTERNE PLEINE");
    //     System.out.println("Relation gauche :\n" + r1.toStringDebug());
    //     System.out.println("Relation droite :\n" + r2.toStringDebug());

    //     Relation res = r1.jointureExternePleine(r2, condition);

    //     System.out.println("Condition : " + condition);
    //     System.out.println("Résultat :\n" + res.toStringDebug());
    //     System.out.println("--------------------------------------------------\n");
    // }

    // public static void main(String[] args)
    //         throws ParseNomException, EvalErr, RelationDomainSizeErr {

    //     Relation r1 = TestSelection.makeRelationOne();
    //     Relation r2 = TestSelection.makeRelationTwo();
    //     Relation r3 = TestSelection.makeRelationThree();

    //     int i = 1;

    //     // =============================
    //     // LEFT / RIGHT / FULL JOIN simples
    //     // =============================
    //     testLeftJoin(i++, r1, r2, "Nom = Nom AND Age = Age");
    //     testRightJoin(i++, r1, r2, "Nom = Nom AND Age = Age");
    //     testFullJoin(i++, r1, r2, "Nom = Nom AND Age = Age");

    //     // =============================
    //     // Tests avec NULL
    //     // =============================
    //     testLeftJoin(i++, r1, r2, "Age is not null AND Age = Age");
    //     testFullJoin(i++, r1, r2, "Age is not null AND Age = Age");

    //     // =============================
    //     // Tests char / varchar
    //     // =============================
    //     testLeftJoin(i++, r3, r3, "CodeChar = CodeVar");
    //     testFullJoin(i++, r3, r3, "CodeChar != CodeVar");

    //     // =============================
    //     // Aucune correspondance
    //     // =============================
    //     testLeftJoin(i++, r1, r2, "Nom = 'INEXISTANT'");
    //     testFullJoin(i++, r1, r2, "Nom = 'INEXISTANT'");
    // }
}
