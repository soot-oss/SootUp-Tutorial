package demo;

import java.util.Collections;
import java.util.List;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;


/**
 * This example demonstrates how one can get the argument of a print statement in some method body.
 */
public class CallGraphDemoIncomplete {

    public static void main(String[] args) {
        // The folder that contains all class files
        String pathToBinary = "CallGraphDemo/src/test/resources/example/";
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(pathToBinary);

        // Create a view , which allows us to retrieve classes
        JavaView view = new JavaView(inputLocation);

        // Create a type for the class we want to analyze
        JavaClassType classType = view.getIdentifierFactory().getClassType("Start");

        // Create a signature for the method that is the entry point for the call graph analysis
        MethodSignature mainMethod =
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

        List<MethodSignature> entryPoints = Collections.singletonList(mainMethod);

    }


}
