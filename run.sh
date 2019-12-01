# @Author: Nithin Sivakumar <Nithin>
# @Date:   2019-11-26T18:19:54-05:00
# @Last modified by:   Nithin
# @Last modified time: 2019-11-30T17:18:57-05:00

#!/bin/sh

##### Constants
# jelly
folderPath=/home/ns1077/work/outfiles
indexPath=/home/ns1077/work/paragraphIndex
outlinesPath=/home/ns1077/work/benchmarkY1/benchmarkY1-test-public/test.pages.cbor-outlines.cbor
trainFile=/home/ns1077/work/unprocessedAllButBenchmark.v2.1/unprocessedAllButBenchmark.Y2.cbor
targetDir=../Wiki-Section-Rank/target
jarName=Wiki-Section-Rank-0.0.1-SNAPSHOT-jar-with-dependencies.jar
class=edu.unh.cs.nithin.main.Main

# local
# folderPath=/Users/Nithin/Desktop/outfiles
# indexPath=/Users/Nithin/Desktop/ParagraphIndex/
# outlinesPath=/Users/Nithin/Desktop/benchmarkY1/benchmarkY1-test-public/test.pages.cbor-outlines.cbor
# trainFile=/Users/Nithin/Desktop/unprocessedAllButBenchmark.cbor/unprocessedAllButBenchmark.cbor
# targetDir=../Wiki-Section-Rank/target
# jarName=Wiki-Section-Rank-0.0.1-SNAPSHOT-jar-with-dependencies.jar
# class=edu.unh.cs.nithin.main.Main

#### Constants
type=

##### install maven dependencies
install_dependencies() {
    echo "Installing maven dependencies"
    mvn package
    echo " Maven dependencies installed. Now generating run files"
}

exec_retrieval() {
    type=retrieval
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath
}

exec_classify() {
    type=classify-runfile
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath $folderPath
}

exec_build() {
    type=build-classifer-model
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $folderPath
}

exec_index() {
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath $folderPath
}

exec_train() {
    type=wikikreator
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $trainFile $folderPath
}

usage() {
    echo "usage: ./run.sh [One of the below options]"
    echo "  -r || --retrieval      execute bm25 for certain categories from outlines.cbor"
    echo "  -c || --classify       execute bm25 and rerank the passages using pre-trained classifier"
    echo "  -t || --train          Genrate qrels and create trainsets for given categories"
    echo "  -b || --build          Train multiple classifiers for categories present in trainset folder"
    echo "  -h || --help           Print usage"
    echo "  no arguments           will execute retrieval and classify"
}

##### execute wiki-section-rank
if [ "$1" != "" ]
then
    case $1 in
        -r | --retrieval )      install_dependencies
                                exec_retrieval
                                exit 0 ;;
        -c | --clasify )        install_dependencies
                                exec_classify
                                exit 0 ;;
        -b | --build )          install_dependencies
                                exec_build
                                exit 0 ;;
        -t | --train )          install_dependencies
                                exec_train
                                exit 0 ;;
        -h | --help )           usage
                                exit 0 ;;
        * )                     usage
                                exit 1
    esac
else
    echo " No parameters passed: Running retrieval and classify"
    install_dependencies
    exec_retrieval
    exec_classify
    echo "retrieval done"
fi
