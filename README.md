# CATS: Modular ABox Abduction Solver

CATS (Comenius-Abduction-Team Solver) is an experimental tool for solving A-Box abduction problems in description logics. It allows the user to choose from multiple various algorithms.

## How to run CATS
You can run CATS through the command line (allocation of more memory for Java is recommended), for example:

**java -Xmx4096m -jar cats.jar in/testExtractingModels/pokus9.in**, where **in/testExtractingModels/pokus9.in** is a relative path to the input file

Another way to run the solver is directly through the **src/main/java/Main.java** class in a Java IDE.

## Input
CATS receives a structured input file as a parameter. The input file contains one switch per line. Mandatory switches are **-f** and **-o**, other switches are optional.

#### Problem definition:
* **-f: \<string\>**  a relative path to the ontology file, which represents the knowledge base $K$.
* **-o: \<ontology\>** observation $O$ in the form of an ontology (in any ontology syntax), which has to be written in one line.

#### Algorithm settings:
* *-alg: \[ mhs | mhs-mxp | hst | hst-mxp ]>*  which algorithm should be used. Not case-sensitive, the dash can be replaced by an underscore or fully ignored (*MHS-MXP*, *mhsmxp*, *mhs_mxp* are all valid values). Set to MHS-MXP by default.
* *-t: \<positive integer\>* the time after which the search for explanations terminates. By default, it is not set.
* *-d: \<positive integer\>* the depth of the HS-tree, when the search terminates. For example, for *-d: 2* search terminates after completing level 1 of HS-tree. By default, it is not set.

#### Explanation limitations:
* *-r: \<boolean\>* allowing role assertions in explanations. Set to *false*, by default.
* *-n: \<boolean\>*  allowing negated assertions in explanations. Set to *true*, by default.
* *-l: \<boolean\>* allows assertions of form $i, i: R$ in explanations, i.e. individual $i$ can be in role $R$ with itself (it is also called *looping*).  

#### Output:
* *-output: \<string\>* custom relative path to output log files.
* *-p: \<boolean\>* prints a simple progress bar into the console. Most useful when *-d* or *-t* is set. Set to *false* by default.

#### Relevance for multiple observation
In the case where observation consists of multiple assertions (also called multiple observations), there are two ways how to define relevant explanation.
1. *Strictly relevant explanation:* an explanation is relevant with respect to **each** assertion from the observation $O$. 
2. *Partially relevant explanation:* an explanation is relevant with respect to **at least one** assertion from the observation $O$.

* *-sR: \<boolean\>* whether strict relevance should be used. Set to *true* by default.  

### Abducibles
#### Defining abducibles using <ins>entities</ins> they can contain:

* *-aI: \<IRI of an individual\>* defines individual that is used in abducibles. We can define more abducible individuals by writing multiple *-aI* switches. By default, all individuals from $K$ and $O$ are used.
* *-aC: \<IRI of a class\>* defines class that is used in abducibles. We can define more classes by writing multiple *-aC* switches. By default, all classes from $K$ and $O$ are used.
* *-aR: \<IRI of an object property\>* defines object property that is used in abducibles. We can define more object properties by writing multiple *-aR* switches. By default, all object properties from $K$ and $O$ are used.

There is also another way by which it is possible to define a list of individuals in one *-aI* switch. The list is wrapped in curly braces and each individual is in one line.

*-aI: {*

*\<IRI of an individual 1\>*

*\<IRI of an individual 2\>*

*}*

Same can be done with *-aC* (defining a list of classes) and *-aR* (defining a list of object properties).

#### Defining abducibles directly by enumerating <ins>assertions</ins>:
* *-abd: \<ontology\>* a complete ontology (in any ontology syntax), which has to be written in one line.

Another way is to list only assertions. In this case, *Manchester Syntax* needs to be used. 

*-abd: {*

*\<assertion 1\>*

*\<assertion 2\>*

*}* 
    
* *-abdF: \<string\>* a relative path to the ontology file, assertions from the ontology will be used as abducibles.

Abducibles cannot be defined by combining different definition types. Either they are defined using entities (*-aI*, *-aC*, *-aR*), using an ontology or list of assertions (*-abd*), or from the ontology file (*-abdF*).
