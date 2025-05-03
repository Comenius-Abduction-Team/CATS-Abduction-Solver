# CATS: Modular ABox Abduction Solver

CATS (Comenius-Abduction-Team Solver) is an experimental tool for solving A-Box abduction problems in description logics. It allows the user to choose from multiple various algorithms.

## How to run CATS
You can run CATS through the command line using

**java -jar cats.jar \<relative path to the input file\>**

For example (allocation of more memory for Java is recommended):

**java -Xmx4096m -jar cats.jar in/toothache.in**

Another way to run the solver is directly through the **src/main/java/sk/uniba/fmph/dai/cats/Main.java** class in a Java IDE. The input can be entered either through a JVM argument or by setting the constants in this class as follows:

*TESTING = true;*  
*API = false;*  
*INPUT_FILE = \<relative path to the input file\>;*

The input can also be constructed programmatically using the classes and methods of the [DL Abduction API](https://github.com/Comenius-Abduction-Team/DL-Abduction-API). In this case, the constants need to be set to:

*TESTING = true;*  
*API = true;*

The abduction problem is then defined in the *runApiTestingMain()* method, which already contains an example.

## Input
CATS receives a structured input file as a parameter. The input file contains one switch per line. Mandatory switches are **-f** and **-o**, other switches are optional.

A line in the input file can be commented out with a // at the start of the line (there must be a space after the slashes). 

#### Problem definition:
* **-f: \<string\>**  a relative path to the ontology file, which represents the knowledge base $K$.
* **-o: \<ontology\>** observation $O$ in the form of an ontology (in any ontology syntax), which has to be written in one line.

#### Algorithm settings:
* *-alg: \[ mhs | hst | mxp | mhs-mxp | hst-mxp ]*  which algorithm should be used. Not case-sensitive, the dash can be replaced by an underscore or fully ignored (*MHS-MXP*, *mhsmxp*, *mhs_mxp* are all valid values). Set to MHS-MXP by default.
* *-t: \<positive integer\>* the time after which the search for explanations terminates. By default, it is not set.
* *-d: \<positive integer\>* the depth of the HS-tree, when the search terminates. For example, for *-d: 2* search terminates after completing level 1 of HS-tree. By default, it is not set.

#### Explanation limitations:
* *-r: \<boolean\>* allowing role assertions in explanations. Set to *false*, by default.
* *-n: \<boolean\>*  allowing negated assertions in explanations. Set to *true*, by default.
* *-l: \<boolean\>* allows assertions of form $i, i: R$ in explanations, i.e. individual $i$ can be in role $R$ with itself (it is also called *looping*).  

#### Output:
* *-log: \<boolean\>* whether output log files should be created. *true* by default.
* *-out: \<string\>* custom relative path to output log files.
* *-p: \<boolean\>* prints a simple progress bar into the console. Most useful when *-d* or *-t* is set. Set to *false* by default.
* *-debug: \<boolean\>* prints detailed messages that describe events happening during the algorithm's run into the console. Set to *false* by default.

#### Relevance for multiple observation
In the case where observation consists of multiple assertions (also called multiple observations), there are two ways of defining a relevant explanation.
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

The same can be done with *-aC* (defining a list of classes) and *-aR* (defining a list of object properties).

#### Defining abducibles directly by enumerating <ins>assertions</ins>:
* *-abd: \<ontology\>* a complete ontology (in any ontology syntax), which has to be written in one line.

Another way is to list only assertions. In this case, *Manchester Syntax* needs to be used. 

*-abd: {*  
*\<assertion 1\>*  
*\<assertion 2\>*  
*}* 
    
* *-abdF: \<string\>* a relative path to the ontology file, assertions from the ontology will be used as abducibles.

Abducibles cannot be defined by combining different definition types. Either they are defined using entities (*-aI*, *-aC*, *-aR*), using an ontology or list of assertions (*-abd*), or from the ontology file (*-abdF*).

## Output
As an output for a given input, the solver produces several log files. Time in the logs is given in seconds. The time in file names is in Unix time format.

Each log is created in a folder with path *logs/\<algorithm\>/\<background knowledge ontology file name\>/\<input file name\>*. If using the *-out* switch, the path is *logs/\<algorithm\>/\<value of -out\>* instead.

### Final logs 
Final logs are created only after the search for explanations is complete (it either ended normally and all explanations have been found or it was interrupted by the depth or time limit). The found explanations are filtered and only the desired ones are written in the logs.

**Final log**
*\<time\>__\<input file name\>__final.log*

* the main final log which contains desired explanations of a certain length in each line (except the last)
  * line form: *\<length n\>; \<number of explanations\>; \<level completion time\>; {\<found explanations of the length n\>}*
* last line contains the total running time

**Explanation times log**
*\<time\>__\<input file name\>__explanation_times.log*

* final log which contains desired explanations and time when they were found
  * line form: *\<time t\>; \<explanation found in the time t\>*

**Level log**
*\<time\>__\<input file name\>__level.log*

* final log which contains desired explanations found in a certain level in each line (except the last)
  * line form: *\<level l\>; \<number of explanations\>; \<level l completion time\>; {\<explanations found in the level l\>}*
* last line contains the total running time

**Info log**
*\<time\>__\<input file name\>__info.log*
* contains basic information about how the input was set in a given run, e.g. timeout, maximum depth or enabling negation in explanations (switches)

**Error log**
*\<time\>__\<input file name\>__error.log*
* created only if an error occurred during the algorithm's run
* records an error that occurred

### Partial logs
Partial logs are created while the solving of the abduction problem is running. They help us to have an overview of the progress along the run. They record, for example, possible explanations which were found after passing one level. However, these explanations are only possible explanations and therefore may not be desired.

**Partial explanations log**
*\<time\>__\<input file name\>__partial_explanations.log*

* partial log with the same structure as **final log**
* may contain undesired explanations 

**Partial level explanations log**
*\<time\>__\<input file name\>__partial_level_explanations.log*

* partial log with the same structure as **level log**
* may contain also undesired explonations 

When solving the abduction problem using MHS-MXP/HST-MXP, all the mentioned logs are produced.
When using the MHS/HST algorithms, however, only some are produced: **final log**, **explanation times log**, **info log**, **error log** and **partial explanations log**. The reason for this is that other logs would be redundant. For the MHS/HST algorithm, the grouping of explanations according to the length is identical to grouping them according to the levels.

# Acknowledgements

The development of this sofwtare was supported by the Slovak Republic under the grant no. APVV-19-0220 (ORBIS) and by the EU
under the H2020 grant no. 952215 (TAILOR)
