# Ready-to-use examples of dkpro-core components and pipelines

This package, *dkpro-core-examples*, demonstrates the use of DKPro components, such as readers, annotators, and writers.

## Content 

So far, it comprises the following examples:

* **NameAnnotationPipeline**: a dictionary-based name annotator that uses a custom annotation type `name`. 
* **LdaEstimationPipeline**: a pipeline to demonstrate the LDA component which estimates a model.
* **LdaInferencePipeline**: a pipeline that annotates documents with LDA topic proportions, using a previously computed model.
* **TokenizedWriterPipeline**: demonstrates the TokenizedTextWriter which writes all tokens from all documents separated
by whitespaces, one sentence per line.
* **StanfordCoreComponents**: demonstrates the usage of the Stanford Core NLP tools; mind that they are
 GPL-licensed!
 
