package exercise1;

import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        String pathToJar = "jar/exercise1.jar";
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(pathToJar);

        JavaView view = new JavaView(inputLocation);

        // get all classes
        Collection<JavaSootClass> classes = view.getClasses();

        // Task 1
        int numberOfClasses = classes.size();
        System.out.println("Number of classes: " + numberOfClasses);

        // Task 2
        long numberOfPrivateMethods = classes.stream()
                .flatMap(clazz -> clazz.getMethods().stream())
                .filter(method -> method.isPrivate())
                .count();
        System.out.println("Number of private methods: " + numberOfPrivateMethods);

        // Task 3
        long numberOfStaticFields = classes.stream()
                .flatMap(clazz -> clazz.getFields().stream())
                .filter(field -> field.isStatic())
                .count();
        System.out.println("Number of static fields: " + numberOfStaticFields);

        // Task 4
        long numberOfSameTypes = classes.stream()
                .flatMap(clazz -> clazz.getMethods().stream())
                .filter(method -> method.getParameterCount() > 0)
                .filter(method -> method.getReturnType().equals(method.getParameterType(0)))
                .count();
        System.out.println("Number of methods where the return type is the same as its first parameter type (if exists) "
                + numberOfSameTypes);

        // Task 5
        ClassType ct = view.getIdentifierFactory().getClassType("org.reflections.util.FilterBuilder");
        MethodSignature ms = view.getIdentifierFactory()
                .getMethodSignature(ct, "test", "boolean", Collections.singletonList("java.lang.String"));
        Optional<JavaSootMethod> methodOpt = view.getMethod(ms);
        long assignStmtsInMethod = 0;
        if (methodOpt.isPresent()) {
            JavaSootMethod method = methodOpt.get();
            assignStmtsInMethod = method.getBody().getStmts().stream()
                    .filter(stmt -> stmt instanceof JAssignStmt)
                    .count();
        }
        System.out.println("Number of assign statements in method: " + assignStmtsInMethod);
    }

}
