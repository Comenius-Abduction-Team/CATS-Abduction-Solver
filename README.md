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

The '**:**' in the arguments is optional. Boolean arguments can be used without an explicit value, being equivalent to *true*. This means that the following lines have the exact same meaning:

*-n: true*  
*-n true*  
*-n*

A line in the input file can be commented out with a '**//**' or '**#**' at the start of the line (there must be a space after the comment symbol). 

#### Problem definition:
* **-f: \<string\>**  a relative path to the ontology file, which represents the knowledge base $K$.
* **-o: \<ontology\>** observation $O$ in the form of an ontology (in any ontology syntax), which has to be written in one line.

#### Algorithm settings:
* *-alg: \[ mhs | hst | rct | mhs-mxp | hst-mxp | rct-mxp | qxp | mxp ]*  which algorithm should be used. Not case-sensitive, the dash can be replaced by an underscore or fully ignored (*MHS-MXP*, *mhsmxp*, *mhs_mxp* are all valid values). Set to MHS-MXP by default.
* *-t: \<positive integer\>* the time after which the search for explanations terminates. By default, it is not set.
* *-d: \<positive integer\>* the depth of the HS-tree, when the search terminates. For example, for *-d: 2* search terminates after completing level 1 of HS-tree. By default, it is not set.

#### Explanation limitations:
* *-r: \<boolean\>* allowing role assertions in explanations. Set to *false*, by default.
* *-n: \<boolean\>*  allowing negated assertions in explanations. Set to *true*, by default.
* *-l: \<boolean\>* allows assertions of form $i, i: R$ in explanations, i.e. individual $i$ can be in role $R$ with itself (it is also called *looping*).  

#### Output:
* *-log: \<boolean\>* whether any output log files should be created. *true* by default.
* *-partial: \<boolean\>* whether partial log files (see section Output) should be created. *true* by default. If set to *false*, the run may be faster, bu there are no level data in the case of a crash.
* *-stats: \<boolean\>* whether stats for the level log should be tracked. *true* by default. If set to *false*, the run may be a bit faster, but no data is stored in the level logs. 
* *-out: \<string\>* custom relative path to output log files.
* *-p: \<boolean\>* prints a simple progress bar into the console. Most useful when *-d* or *-t* is set. Set to *false* by default.
* *-debug: \<boolean\>* prints detailed messages that describe events happening during the algorithm's run into the console. Set to *false* by default.
* *-fast: \<boolean\>* is a shorthand for *-log: false*,*-stats: false*,*-debug: false*. Additionally, the internal event communication system is shut down, providing an extra speed boost.  Set to *false* by default. If set to *true*, the run should be faster, but no information is stored or tracked except for the final explanations.

#### Relevance for multiple observation
In the case where observation consists of multiple assertions (also called multiple observations), there are two ways of defining a relevant explanation.
1. *Strictly relevant explanation:* an explanation is relevant with respect to **each** assertion from the observation $O$. 
2. *Partially relevant explanation:* an explanation is relevant with respect to **at least one** assertion from the observation $O$.

* *-sR: \<boolean\>* whether strict relevance should be used. Set to *true* by default.  

### Abducibles

Abducibles are used to limit the search space of the abduction problem.
With a set of abducible axioms given, the explanations are constricted to contain only axioms from the set and no others.

#### Defining abducibles using <ins>entities</ins> they can contain:

* *-aI: \<IRI of an individual\>* defines individual that is used in abducibles. By default, all individuals from $K$ and $O$ are used.
* *-aC: \<IRI of a class\>* defines class that is used in abducibles. By default, all classes from $K$ and $O$ are used.
* *-aR: \<IRI of an object property\>* defines object property that is used in abducibles. By default, all object properties from $K$ and $O$ are used.

Multiple individuals/classes/object properties can be defined in two ways (the example uses *-aI*, but the same can be done with *-aC* and *-aR*):

##### Repeating the switch

*-aI: \<IRI of an individual 1\>*  
*-aI: \<IRI of an individual 2\>*

##### Multi-line value

*-aI: {*  
*\<IRI of an individual 1\>*  
*\<IRI of an individual 2\>*  
*}*

#### Defining abducibles directly by enumerating <ins>assertions</ins>:
* *-abd: \<ontology\>* a complete ontology (in any ontology syntax), which has to be written in one line.

Another way is to list only assertions. In this case, the *Manchester Syntax* needs to be used. 

*-abd: {*  
*\<assertion 1\>*  
*\<assertion 2\>*  
*}* 
    
* *-abdF: \<string\>* a relative path to an ontology file. Assertions from the ontology will be used as abducibles.

Abducibles cannot be defined by combining different definition types. Either they are defined using entities (*-aI*, *-aC*, *-aR*), using an ontology or list of assertions (*-abd*), or from the ontology file (*-abdF*).

### Optimisations configuration

The solver contains several modifications that could, in theory, optimise the effectivity of the algorithms (see later section Optimisations for more details).
Some of them are highly experimental and/or tied to the fine details of the algorithms, so we *do not recommend to configure them for basic usage*.
However, there is an option to do so using the following parameters:

* *-defOpt: \<boolean\>*  allows default optimisations. Set to *true*, by default.
* *-opt: \<string\>* turns on optimisations whose char IDs are listed in the given string. E.g., *-opt: 123* activates opts. 1, 2 and 3. The order of the chars or other symbols in the string that are not opt. IDs have no effect. If the default optimisations are active (*-defOpt* is not set to *false*), the listed opts. are added to the default ones. 

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

* final log which contains detailed statistics about the solver's run
* the stats are separate for each level of the tree built by the algorithm used
  * if the algorithm does not build a tree, the log contains only one "level"
  * the last "level" in the log represents the final filtering of explanations
* the stats are mostly numbers of occurences of specific actions/events in that level,e.g. how many nodes were created/pruned, how many consistency checks were performed, how many explanations were found and how many of those were filtered out... + average memory usage (in MB)

**Info log**
*\<time\>__\<input file name\>__info.log*
* contains basic information about how the input was set in a given run, e.g. timeout, maximum depth or enabling negation in explanations (switches)

**Error log**
*\<time\>__\<input file name\>__error.log*
* created only if an error occurred during the algorithm's run
* records an error that occurred

### Partial logs
Partial log is a temporary version of the level log, updated after each level of the tree. It is deleted if the run successfully finished. However, if the run crashed and could not create the final level log for some reason, at least some data is stored in the partial log. 

**Partial level explanations log**
*\<time\>__\<input file name\>__partial_level.log*

* partial log with the same structure as **level log**
* may contain undesired explonations
* if the run successfully finished, the log is deleted

## Optimisations

### List of optimisations

| ID | Name                           | Description                                                                                                                                                                                                                            |
|----|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1  | Reduced consistency checks     | Greatly reduces the number of redundant consistency checks.                                                                                                                                                                            |
| 2  | Model sorting                  | When reusing model labels, the smallest one is always chosen, decreasing the number of potential branches.                                                                                                                             |
| 3  | Removed negated path           | (Only with negations) When MXP is called, negations of the axioms on the current tree path are moved from MXP's input. This decreases the number of conflicting axioms in MXP, improving the chance to find some explanations.         |
| 4  | Triple MXP                     | (Only with negations) When MXP should be called, it is called thrice: with all axioms on its input, with only positive axioms and only negative axioms. This increases the chance of finding explanations with conflicting abducibles. |
| 5  | Fully random set division      | When QXP divides its input set into two, the division is fully random instead of deterministic.                                                                                                                                        |
| 6  | Equal size random set division | Same as the previous one, but the input set is divided in two halves of the same size if possible.                                                                                                                                     |

### Default optimisations

The default optimisations have been chosen based on an empirical evaluation.
Each algorithm uses a combination of optimisations that lead to the best average results on the testing dataset, with extra focus on problems with longer solving time. 

#### Without negations (*-n: false*)

| Optimisation               | MHS(-MXP) | RCT(-MXP) | HST(-MXP) |
|----------------------------|-----------|-----------|-----------|
| Reduced consistency checks | ✓         | ✓         | ✗         |
| Model sorting              | ✓         | ✗         | ✓         |

#### With negations (*-n: true*)

| Optimisation               | MHS(-MXP) | RCT(-MXP) | HST(-MXP) |
|----------------------------|-----------|-----------|-----------|
| Reduced consistency checks | ✓         | ✓         | ✓         |
| Model sorting              | ✓         | ✗         | ✗         |
| Triple MXP                 | ✓         | ✓         | ✓         |

# License

This software is licensed under the GNU Affero General Public License v3.0 (AGPLv3).
See the [LICENSE](./LICENSE) file for more information.

# Acknowledgements

The development of this sofwtare was supported by the Slovak Republic under the grant no. APVV-19-0220 (ORBIS) and by the EU
under the H2020 grant no. 952215 (TAILOR)

