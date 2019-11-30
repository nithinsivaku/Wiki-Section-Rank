# Wiki-Section-Rank
This project attempts to build a retrieval system that takes advantage of the text classifier model built using appropriate topical sections from the Wikipedia articles.

## Requirement
Java 1.8 or higher\
Maven 3.3.8 or higher

## Installation Instructions
1. git clone this repository\
``` git clone https://github.com/nithinsivakumar/Wiki-Section-Rank ```\
2. ``` ./run.sh ``` 
    1. This will execute retrieval and classify.
    2. creates a folder named outfiles in project dir and stores all the generated runfiles files for evaluation.
3. ``` ./run.sh -h ``` will print the below usage.
  
   ```
   usage: ./run.sh [One of the below options]
   -r || --retrieval      execute bm25 for certain categories from outlines.cbor
   -c || --classify       execute bm25 and rerank the passages using pre-trained classifier
   -t || --train          Genrate qrels and create trainsets for given categories
   -b || --build          Train multiple classifiers for categories present in trainset folder
   -h || --help           Print usage
   no arguments           will execute retrieval and classify
   ```
