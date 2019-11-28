# @Author: Nithin Sivakumar <Nithin>
# @Date:   2019-11-26T18:19:54-05:00
# @Last modified by:   Nithin
# @Last modified time: 2019-11-27T21:14:11-05:00

#!/bin/sh

##### Constants
# jelly
# folderPath=/home/ns1077/work/outfiles
# indexPath=/home/ns1077/work/paragraphIndex
# outlinesPath=/home/ns1077/work/benchmarkY1/benchmarkY1-test-public/test.pages.cbor-outlines.cbor
# targetDir=/home/ns1077/work/Wiki-Section-Rank/target
# jarName=Wiki-Section-Rank-0.0.1-SNAPSHOT-jar-with-dependencies.jar
# class=edu.unh.cs.nithin.main.Main

# local
folderPath=/Users/Nithin/Desktop/outfiles
indexPath=/Users/Nithin/Desktop/ParagraphIndex/
outlinesPath=/Users/Nithin/Desktop/benchmarkY1/benchmarkY1-test-public/test.pages.cbor-outlines.cbor
targetDir=/Users/Nithin/git/Wiki-Section-Rank/target
jarName=Wiki-Section-Rank-0.0.1-SNAPSHOT-jar-with-dependencies.jar
class=edu.unh.cs.nithin.main.Main


type=

echo "Generating runFiles"



# mvn package

##### Functions
exec_retrieval() {
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath $folderPath
}

exec_classify() {
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath $folderPath
}

exec_build() {
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath $folderPath
}

exec_index() {
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath $folderPath
}

exec_train() {
    java -Xms2048M -Xmx300g -cp $targetDir/$jarName $class $type $outlinesPath $indexPath $folderPath
}

usage() {
    echo " reached help"
}

while [ "$1" != "" ]; do
    case $1 in
        -r | --retrieval )      type=retrieval
                                exec_retrieval
                                exit 0 ;;
        -c | --clasify )        type=classify-runfile
                                exec_classify
                                ;;
        -b | --build )          type=build-classifer-model
                                exec_build
                                ;;
        -t | --train )          type=wikikreator
                                exec_train
                                ;;
        -h | --help )           usage
                                exit 0
                                ;;
        * )                     usage
                                exit 1
    esac
done

# if no paramaters are passed
type=retrieval
exec_retrieval
echo "retrieval over"
