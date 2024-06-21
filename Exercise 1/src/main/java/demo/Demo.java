package demo;

import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * This example demonstrates how one can get the argument of a print statement in some method body.
 */
public class Demo {

    public static void main(String[] args) {
        // Create a AnalysisInputLocation, which points to a directory. All class files will be loaded
        // from the directory
        String pathToBinary = "src/test/resources/example/";
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(pathToBinary);

        // Create a view , which allows us to retrieve classes
        JavaView view = new JavaView(inputLocation);

        // Create a signature for the class we want to analyze
        JavaClassType classType = view.getIdentifierFactory().getClassType("HelloWorld");

        // Create a signature for the method we want to analyze
        MethodSignature methodSignature =
                view
                        .getIdentifierFactory()
                        .getMethodSignature(
                                classType, "main", "void",
                                Collections.singletonList("java.lang.String[]"));


        // Retrieve method
        Optional<JavaSootMethod> method = view.getMethod(methodSignature);
        if (method.isPresent()) {
            JavaSootMethod sootMethod = method.get();

            // Get all the statements in the method's body
            List<Stmt> stmts = sootMethod.getBody().getStmts();
            for (Stmt stmt : stmts) {
                // Check for invoking statements (e.g. method calls)
                if (stmt instanceof JInvokeStmt) {
                    JInvokeStmt invoke = (JInvokeStmt) stmt;
                    AbstractInvokeExpr invokeExpr = invoke.getInvokeExpr();
                    // Print the first argument passed to the method call
                    System.out.println(invokeExpr.getArg(0));
                }
            }
        } else {
            System.err.println("Method not found");
        }


    }


}
