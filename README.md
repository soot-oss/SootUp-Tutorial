# SootUp Tutorial

Welcome to the PLDI'24 tutorial "Static Analysis with SootUp". In this tutorial you will learn all about how to implement static code analyses for Java using the brand-new SootUp framework. Please checkout the different branches of this repository to access the exercises and solutions.

## Exercise 1: Get to know the API

**Task:** Extract the following properties from the given [Jar](Exercise%201/jar/exercise1.jar):

1. Number of **classes** 
2. Number of **private methods**
3. Number of **static fields**
4. Number of **methods** where the **return type** is the same as its first **parameter type** (if exists)
5. Number of **assignment statements** in method *„boolean test(java.lang.String)“* in class *„org.reflections.util.FilterBuilder“*

You can use the provided [boilerplate](Exercise%201) to implement your solution.


## Exercise 1: Get to know the API - Solution

1. Number of classes: 74
2. Number of private methods: 175
3. Number of static fields: 44
4. Number of methods where the return type is the same as its first parameter type (if exists) 26
5. Number of assign statements in method: 16