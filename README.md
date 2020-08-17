# lc4j: Language Categorization for Java

`lc4j` Java library (and program) implement the technique described in this paper:

> Cavnar, W. B. and J. M. Trenkle, ''N-Gram-Based Text Categorization''
> In Proceedings of Third Annual Symposium on Document Analysis and
> Information Retrieval, Las Vegas, NV, UNLV Publications/Reprographics,
> pp. 161-175, 11-13 April 1994.

and can be used to find the language in which any given input text is written by using a technique based on n-grams.

## Origins

The idea behind this program and most of the sample texts have their roots in [TextCat](https://www.let.rug.nl/vannoord/TextCat/), a free Perl library which implements the text categorization algorithm.

This project was made in 2005 and updated in 2009, and finally published on Github around mid-2020.

No warranty is provided with the code; however, as far as I know, many people and international projects have found it useful and implemented it in their code over the years.

As of today (2020), this library offers a functionality which is offered as a PaaS service by many cloud providers; however, if one wants to incorporate the functionality in his own code and not depend on the cloud, this library could still be beneficial, even nowawadays.

## Short note on mem/CPU performance

Despite this library being written in Java, the code is engineered using memory-efficient and computational-efficient libraries such as fastutil, mg4j and colt (links below); also, eccessive usage of Java garbage collection is trying to be avoided by trying to re-use objects and avoid their creation in the first place, where possible.

## Installation of the library

Installation of `lc4j` library is very simple, since there are no special needs for an installation at all.

If you want to use the plain library in your project, just build the JAR file with [Maven](https://maven.org) and then copy the generated `.jar` file from the target Maven folder to your desired destination directory (command to run to build: `mvn package`)

Please note that to compile/run `lc4j` you will also need the following libraries:

* [fastutil](http://fastutil.dsi.unimi.it/)
* [mg4j](http://mg4j.dsi.unimi.it/)
* [Java GNU Getopt](http://www.urbanophile.com/arenn/hacking/download.html)
* [Colt](http://acs.lbl.gov/software/colt/)

and they must be placed in your `$CLASSPATH` env variable. Please note that you should also add to your classpath the `lc4j` library.

The `$CLASSPATH` can be set by sourcing [this file](/classpath.env) at shell command prompt: it should work, since the paths provided are all relative to the home and to base repo folder.

## Directory contents and usage

In the [models](/models) directory you will find some default (and valid) language models. These models have been extracted from the texts contained in the [sample-texts](/sample-texts) directory.

To regenerate language-models starting from the `*.txt` files contained in the [sample-texts](/sample-texts) directory you can launch this command at your prompt:

```bash
for i in sample-texts/*.txt ; do $(cat $i | java net.olivo.lc4j.LanguageCategorization \
-c > models/`basename $i | sed s/.txt//g`.lm) ; done ;
```

(it will take a few minutes, depending also on your CPU processing speed)

To run lc4j just run:

```bash
java net.olivo.lc4j.LanguageCategorization
```

after properly setting your `$CLASSPATH` (see installation instructions above) at your command prompt.

For more help and options, run:

```bash
java net.olivo.lc4j.LanguageCategorization -h
```
