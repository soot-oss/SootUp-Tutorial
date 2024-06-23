package demo;

import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.Collections;
import java.util.List;


/**
 * This example demonstrates how one can get the argument of a print statement in some method body.
 */
public class CallGraphDemo {

    public static void main(String[] args) {
        // The folder that contains all class files
        String pathToBinary = "CallGraphDemo/src/test/resources/example/";
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(pathToBinary);

        // Create a view , which allows us to retrieve classes
        JavaView view = new JavaView(inputLocation);

        // Create a type for the class we want to analyze
        JavaClassType classType = view.getIdentifierFactory().getClassType("Start");

        // Create a signature for the method that is the entry point for the call graph analysis
        MethodSignature methodSignature =
                view
                        .getIdentifierFactory()
                        .getMethodSignature(
                                classType, "main", "void",
                                Collections.singletonList("java.lang.String[]"));
        MethodSignature startMethod =
            view
                .getIdentifierFactory()
                .getMethodSignature(
                    classType, "start", "void",
                    Collections.singletonList("A"));

        List<MethodSignature> entryPoints = Collections.singletonList(methodSignature);

        //create the algorithm
        ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view);
        RapidTypeAnalysisAlgorithm rta = new RapidTypeAnalysisAlgorithm(view);

        //create the call graphs for both algorithms
        CallGraph cgCHA = cha.initialize(entryPoints);
        CallGraph cgRTA = rta.initialize(entryPoints);

        //print the calls of the method start
        cgCHA.callsFrom(startMethod).forEach(methodSignature1 -> System.out.println("cha:" + methodSignature1));
        cgRTA.callsFrom(startMethod).forEach(methodSignature2 -> System.out.println("rta:" + methodSignature2));

        //export the call graph as dot
        System.out.println(cgCHA.exportAsDot());
        System.out.println(cgRTA.exportAsDot());


    }


}
