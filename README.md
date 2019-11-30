# Wiki-Section-Rank
This project attempts to build a retrieval system that takes advantage of the text classifier model built using appropriate topical sections from the Wikipedia articles.

## Requirement
Java 1.8 or higher\
Maven 3.3.8 or higher

## Installation Instructions
1. git clone this repository\
``` git clone https://github.com/nithinsivakumar/Wiki-Section-Rank ```\
2. ``` ./run.sh ``` will create a folder named outfiles in project dir and stores all the runfiles files for evaluation\
3. ``` ./run.sh -r ``` will execute the bm25 retrieval model for certain categories\
4. ``` ./run.sh -c ``` will execute retrieval and classification \
5. ``` ./run.sh -t ``` will run the project in installation mode. ie. will create necessary training files for classifier\
6. ``` ./run.sh -b ``` will build classifier models using train files created using -t option.
