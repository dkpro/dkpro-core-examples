# Ready-to-use examples of dkpro-core components and pipelines

This package, *dkpro-core-examples*, demonstrates the use of DKPro components, such as readers, annotators, and writers.
Each module in this project refers to a DKPro core component, providing a simple pipeline that is
usable as is.

## Content 

So far, it comprises the following examples:

* **nameannotation-asl**: a dictionary-based name annotator that uses a custom annotation type. 
* **lda-asl**: pipelines to demonstrates how to estimate an LDA model and how to use it to infer topic proportions
in documents.
* **tokenizedwriter-asl**: demonstrates the `TokenizedTextWriter` which writes all tokens from all documents separated
by whitespaces, one sentence per line; can be used to prepare data for external tools such as Word2Vec. 
* **stanfordcorecomponents-gpl**: demonstrates the usage of the Stanford Core NLP tools; mind that they are
 GPL-licensed!
 
