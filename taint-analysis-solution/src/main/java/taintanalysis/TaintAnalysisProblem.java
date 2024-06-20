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
import sootup.analysis.interprocedural.ifds.DefaultJimpleIFDSTabulationProblem;
import sootup.core.jimple.basic.Immediate;
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

    FlowFunction<Value> getNormalFlow(Stmt curr, Stmt succ) {
        if (curr instanceof JAssignStmt) {
            final JAssignStmt assign = (JAssignStmt) curr;
            final Value leftOp = assign.getLeftOp();
            final Value rightOp = assign.getRightOp();
            // generate taint only at x="SECRET"
            if (rightOp instanceof StringConstant) {
                StringConstant str = (StringConstant) rightOp;
                if (str.getValue().equals("SECRET")) {
                    return new Gen<>(leftOp, zeroValue());
                }
            }
            return new FlowFunction<Value>() {
                @Override
                public Set<Value> computeTargets(Value source) {
                    // Kill T = ...
                    if (source == leftOp) {
                        return Collections.emptySet();
                    }
                    Set<Value> res = new HashSet<>();
                    res.add(source);
                    // x = T
                    if (source == rightOp) {
                        res.add(leftOp);
                    }
                    return res;
                }
            };
        }
        return Identity.v();
    }

    FlowFunction<Value> getCallFlow(Stmt callStmt, final SootMethod destinationMethod) {
        AbstractInvokeExpr ie = callStmt.getInvokeExpr();
        final List<Immediate> callArgs = ie.getArgs();
        Map<Value, Value> callArgsToLocalParamsMapping = new HashMap<>();
        for (int i = 0; i < destinationMethod.getParameterCount(); i++) {
            callArgsToLocalParamsMapping.put(callArgs.get(i), destinationMethod.getBody().getParameterLocal(i));
        }

        return new FlowFunction<Value>() {
            @Override
            public Set<Value> computeTargets(Value source) {
                Set<Value> ret = new HashSet<>();
                for (Value callArg : callArgsToLocalParamsMapping.keySet()) {
                    if (callArg.equivTo(source)) {
                        ret.add(callArgsToLocalParamsMapping.get(callArg)); // corresponding local parameter
                    }
                }
                return ret;
            }
        };
    }

    FlowFunction<Value> getReturnFlow(
            final Stmt callSite, final SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {

        if (exitStmt instanceof JReturnStmt) {
            JReturnStmt returnStmt = (JReturnStmt) exitStmt;
            final Value retOp = returnStmt.getOp();
            if (retOp instanceof StringConstant) {
                StringConstant str = (StringConstant) retOp;
                if (str.getValue().equals("SECRET")) {
                    if (callSite instanceof JAssignStmt) {
                        JAssignStmt assign = (JAssignStmt) callSite;
                        final Value leftOp = assign.getLeftOp();
                        return new Gen<>(leftOp, zeroValue());
                    }
                }
            }
            return new FlowFunction<Value>() {
                @Override
                public Set<Value> computeTargets(Value source) {
                    Set<Value> ret = new HashSet<>();
                    if (callSite instanceof AbstractDefinitionStmt && source == retOp) {
                        AbstractDefinitionStmt defnStmt = (AbstractDefinitionStmt) callSite;
                        ret.add(defnStmt.getLeftOp());
                    }
                    return ret;
                }
            };
        }
        return KillAll.v(); // Anything else should not be returned
    }

    FlowFunction<Value> getCallToReturnFlow(final Stmt callSite, Stmt returnSite) {
        return Identity.v();
    }
}
