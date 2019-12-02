# @Author: Nithin Sivakumar <Nithin>
# @Date:   2019-11-26T18:19:54-05:00
# @Last modified by:   Nithin
# @Last modified time: 2019-12-02T09:51:03-05:00

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

# qrels location
catHabitatQrel=/home/ns1077/work/outfiles/trec_qrels/cat_test_qrels/Category_Habitat
catDiseaseQrel=/home/ns1077/work/outfiles/trec_qrels/cat_test_qrels/Category_Diseases_and_disorders
catEnvironQrel=/home/ns1077/work/outfiles/trec_qrels/cat_test_qrels/Category_Environmental_terminology.qrels
catChristmQrel=/home/ns1077/work/outfiles/trec_qrels/cat_test_qrels/Category_Christmas_food

# result files location
catHabitatRes=outFiles/runFiles/classify/rerank/NaiveBayes/Category_Habitat
catDiseaseRes=outFiles/runFiles/classify/rerank/NaiveBayes/Category_Diseases_and_disorders
catEnvironRes=outFiles/runFiles/classify/rerank/NaiveBayes/Category_Environmental_terminology
catChristmRes=outFiles/runFiles/classify/rerank/NaiveBayes/Category_Christmas_food

# eval files
catHabitatEval=outFiles/eval_results/eval_Habitat.txt
catDiseaseEval=outFiles/eval_results/eval_Disease.txt
catEnvironEval=outFiles/eval_results/eval_Environment.txt
catChristmEval=outFiles/eval_results/eval_Christmasfood.txt

# local
# folderPath=/Users/Nithin/Desktop/outfiles
# indexPath=/Users/Nithin/Desktop/ParagraphIndex/
# outlinesPath=/Users/Nithin/Desktop/benchmarkY1/benchmarkY1-test-public/test.pages.cbor-outlines.cbor
# trainFile=/Users/Nithin/Desktop/unprocessedAllButBenchmark.cbor/unprocessedAllButBenchmark.cbor
# targetDir=../Wiki-Section-Rank/target
# jarName=Wiki-Section-Rank-0.0.1-SNAPSHOT-jar-with-dependencies.jar
# class=edu.unh.cs.nithin.main.Main
# catDiseaseQrel=/Users/Nithin/Desktop/outfiles/trec_qrels/cat_test_qrels/Category_Diseases_and_disorders


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
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $folderPath $indexPath $outlinesPath
}

exec_build() {
    type=build-classifer-model
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $folderPath
}

exec_index() {
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $folderPath $indexPath $outlinesPath
}

exec_train() {
    type=wikikreator
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $trainFile $folderPath
}

# download trec_eval, evaluate results, save results to file
evaluate_results() {
    git clone https://github.com/usnistgov/trec_eval
    (cd trec_eval && make)
    (cd outFiles && mkdir eval_results)
    touch $catHabitatEval $catDiseaseEval $catEnvironEval $catChristmEval
    (cd trec_eval && ./trec_eval -c $catHabitatQrel ../$catHabitatRes) | tee $catHabitatEval
    (cd trec_eval && ./trec_eval -c $catDiseaseQrel ../$catDiseaseRes) | tee $catDiseaseEval
    (cd trec_eval && ./trec_eval -c $catEnvironQrel ../$catEnvironRes) | tee $catEnvironEval
    (cd trec_eval && ./trec_eval -c $catChristmQrel ../$catChristmRes) | tee $catChristmEval
    echo "evaluation results are located at---- outFiles/eval_results"
    rm -rf trec_eval
}

usage() {
    echo "usage: ./run.sh [One of the below options]"
    echo "  -r || --retrieval      execute bm25 for certain categories from outlines.cbor"
    echo "  -c || --classify       execute bm25 and rerank the passages using pre-trained classifier"
    echo "  -t || --train          Genrate qrels and create trainsets for given categories"
    echo "  -b || --build          Train multiple classifiers for categories present in trainset folder"
    echo "  -h || --help           Print usage"
    echo "  no arguments           will execute retrieval and classify, write eval results at outFiles/eval_results/"
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
    echo "No parameters are passed:-- Running retrieval and classify"
    install_dependencies
    exec_retrieval
    exec_classify
    evaluate_results
fi
