# SootUp Tutorial

Welcome to the PLDI'24 tutorial "Static Analysis with SootUp". In this tutorial you will learn all about how to implement static code analyses for Java using the brand-new SootUp framework.

## Exercise 2:

**Task:** Implement the Flow Functions in TaintAnalysisProblem.java:

1. getNormalFlow()
    1. Generating a Taint: only for this specific assignment, x = "SECRET"
    2. Propagating (Transferring) a Taint between different variables
2. getCallFlow() 
    1. Mapping call arguments to local parameters,  foo(x)
3. getReturnFlow() 
    1. Mapping returned variable to assigned variable, y = foo(x)