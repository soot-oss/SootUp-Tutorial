package taintanalysis;/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;
import heros.flowfunc.Transfer;
import sootup.analysis.interprocedural.ifds.DefaultJimpleIFDSTabulationProblem;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.AbstractDefinitionStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.types.NullType;

import java.util.*;

public class TaintAnalysisProblem
        extends DefaultJimpleIFDSTabulationProblem<Value, InterproceduralCFG<Stmt, SootMethod>> {

    private SootMethod entryMethod;

    protected InterproceduralCFG<Stmt, SootMethod> icfg;

    public TaintAnalysisProblem(
            InterproceduralCFG<Stmt, SootMethod> icfg, SootMethod entryMethod) {
        super(icfg);
        this.icfg = icfg;
        this.entryMethod = entryMethod;
    }

    @Override
    public Map<Stmt, Set<Value>> initialSeeds() {
        return DefaultSeeds.make(
                Collections.singleton(entryMethod.getBody().getStmtGraph().getStartingStmt()), zeroValue());
    }

    @Override
    protected FlowFunctions<Stmt, Value, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Stmt, Value, SootMethod>() {

            @Override
            public FlowFunction<Value> getNormalFlowFunction(Stmt curr, Stmt succ) {
                return getNormalFlow(curr, succ);
            }

            @Override
            public FlowFunction<Value> getCallFlowFunction(Stmt callStmt, SootMethod destinationMethod) {
                return getCallFlow(callStmt, destinationMethod);
            }

            @Override
            public FlowFunction<Value> getReturnFlowFunction(
                    Stmt callSite, SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
                return getReturnFlow(callSite, calleeMethod, exitStmt, returnSite);
            }

            @Override
            public FlowFunction<Value> getCallToReturnFlowFunction(Stmt callSite, Stmt returnSite) {
                return getCallToReturnFlow(callSite, returnSite);
            }
        };
    }

    @Override
    protected Value createZeroValue() {
        return new Local("<<zero>>", NullType.getInstance());
    }


    /**
     * TODO: Start here
     * In this exercise you will implement a full-fledged whole-program interprocedural taint analysis.
     * A taint denotes critical information that we want to track throughout the application.
     *
     * In the following we will generate a taint, if and only if currentStmt is in the following form:
     * x = "SECRET"
     *
     * taints can be propagated:
     * (1) intraprocedurally, through assignments:
     * y = x
     *
     * (2) interprocedurally, through method calls:
     * foo(x)
     *
     * (3) and through method returns:
     * y = bar()
     *
     *
     * In the following, you will find instructions to fill the corresponding TODO blocks and uncomment boilerplate code blocks.
     *
     */

    /**
     * NormalFlow corresponds to all intraprocedural statements, all the statements except method calls (invokes)
     *
     * @param currentStmt
     * @param successorStmt
     * @return
     */
    FlowFunction<Value> getNormalFlow(Stmt currentStmt, Stmt successorStmt) {
        /**
         * TODO: 1. What should be the type of the currentStmt?
         * Remember we generate taints when a certain "assignment" happens
         * Hint: check the implementers of the Stmt interface
         */

        /* Uncomment and fill [...]
        if (currentStmt instanceof [...]) {
            [...] assign = ([...]) currentStmt;
            Value leftOp = assign.getLeftOp();
            Value rightOp = assign.getRightOp();
         */

            /**
             * TODO: 2. Generating a taint
             * If the rightOp is instanceof StringConstant and if it has the value "SECRET",
             * we will generate a taint on the leftOp using an existing FlowFunction implementation, Gen (Generate).
             */

            /* Uncomment and fill [...]
            if (rightOp instanceof [...]) {
                [...] str = ([...]) rightOp;
                if (str.getValue().equals("[...]")) {
                    return new Gen<>(leftOp, zeroValue());
                }
            }
            */

            /***
             * TODO: 3. Transferring an existing taint
             * A generated taint can be transferred between different variable through assignments.
             * What is the appropriate FlowFunction implementation?
             * Hint: check the implementers of the FlowFunction interface
             */

            /* Uncomment and fill [...]
            return new [...]<>([...], [...]);
        }
        */

        return Identity.v(); // Anything else continues as it is
    }

    /**
     * CallFlow corresponds to a method call.
     * During a method call actual arguments are mapped into the local parameters in the callee method's context.
     *
     * @param callStmt
     * @param destinationMethod
     * @return
     */
    FlowFunction<Value> getCallFlow(Stmt callStmt, final SootMethod destinationMethod) {
        AbstractInvokeExpr ie = callStmt.getInvokeExpr();
        final List<Immediate> callArgs = ie.getArgs();
        Map<Value, Value> callArgsToLocalParamsMapping = new HashMap<>(); // a map of call arguments to local parameters
        for (int i = 0; i < destinationMethod.getParameterCount(); i++) {
            callArgsToLocalParamsMapping.put(callArgs.get(i), destinationMethod.getBody().getParameterLocal(i));
        }

        /**
         * TODO: 4. Mapping (Transferring) from call args to local params
         * implement the code block that transfers each callArg to its corresponding LocalParam
         */


        /*
        Implement Here
        */





        return KillAll.v(); // Anything else should not be mapped into this context
    }

    /**
     * ReturnFlow corresponds to returning from a method call.
     * During a method return, the returned value is mapped to the caller method's context.
     *
     * @param callSite: statement in the caller method that calls the calleeMethod
     * @param calleeMethod
     * @param exitStmt: the final statement in a method
     * @param returnSite
     * @return
     */
    FlowFunction<Value> getReturnFlow(
            final Stmt callSite, final SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {

        /**
         * TODO: 5. What type should be the exitStmt?
         */
       // Uncomment and fill [...]
       // if (exitStmt instanceof [...]) {
       //     [...] returnStmt = ([...]) exitStmt;

            /**
             * TODO: 6. What type should be the callSite stmt?
             * Remember we want to model assignments of method calls, e.g. y = foo()
             */

            // Uncomment and fill [...]
            // if (callSite instanceof [...]) {
            //    [...] assignStmt = ([...]) callSite;
            //    final Value retOp = returnStmt.getOp();

            //    if(!(retOp instanceof StringConstant)){ // normal parameter return
                    /**
                     * TODO: 7. Mapping (Transferring) the returned parameter to the variable at the call site
                     *
                     */
                    // Uncomment and fill [...]
                    //return new Transfer<>([...], retOp);
             //   }else { // retOp is StringConstant
                    /**
                     * TODO: Bonus Task
                     * A method might return the StringConstant "SECRET"
                     * What kind of a FlowFunction should handle this? transfer or gen?
                     * Implement the code block that handles this case.
                     *
                     */


                    /*
                    Implement here
                     */

             //   }
        //    }
    //    }
        return KillAll.v(); // Anything else should not be returned from this context
    }

    /**
     * Not part of the exercise
     *
     * @param callSite
     * @param returnSite
     * @return
     */
    FlowFunction<Value> getCallToReturnFlow(final Stmt callSite, Stmt returnSite) {
        return Identity.v(); // Anything else continues as it is
    }
}