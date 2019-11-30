# @Author: Nithin Sivakumar <Nithin>
# @Date:   2019-11-26T18:19:54-05:00
# @Last modified by:   Nithin
# @Last modified time: 2019-11-29T20:18:29-05:00

#!/bin/sh

##### Constants
# jelly
folderPath=/home/ns1077/work/outfiles
indexPath=/home/ns1077/work/paragraphIndex
outlinesPath=/home/ns1077/work/benchmarkY1/benchmarkY1-test-public/test.pages.cbor-outlines.cbor
targetDir=../Wiki-Section-Rank/target
jarName=Wiki-Section-Rank-0.0.1-SNAPSHOT-jar-with-dependencies.jar
class=edu.unh.cs.nithin.main.Main

# local
# folderPath=/Users/Nithin/Desktop/outfiles
# indexPath=/Users/Nithin/Desktop/ParagraphIndex/
# outlinesPath=/Users/Nithin/Desktop/benchmarkY1/benchmarkY1-test-public/test.pages.cbor-outlines.cbor
# targetDir=../Wiki-Section-Rank/target
# jarName=Wiki-Section-Rank-0.0.1-SNAPSHOT-jar-with-dependencies.jar
# class=edu.unh.cs.nithin.main.Main

#### Constants
type=

##### Functions
install_dependencies() {
    echo "Running mvn package"
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
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath $folderPath
}

usage() {
    echo " reached help "
}

##### install maven dependencies
install_dependencies

##### execute wiki-section-rank
while [ "$1" != "" ]; do
    case $1 in
        -r | --retrieval )      exec_retrieval
                                exit 0 ;;
        -c | --clasify )        exec_classify
                                exit 0 ;;
        -b | --build )          exec_build
                                exit 0 ;;
        -t | --train )          exec_train
                                exit 0 ;;
        -h | --help )           usage
                                exit 0
                                ;;
        * )                     usage
                                exit 1
    esac
done

# if no paramaters are passed
exec_retrieval
exec_classify
echo "retrieval done"
