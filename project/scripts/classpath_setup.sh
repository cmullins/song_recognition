#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JARS_DIR=$DIR/../lib

project_cp=${CLASSPATH}:$DIR/../bin
for jar in $(ls $JARS_DIR/**.jar); do
	project_cp=${project_cp}:$jar
done

echo $project_cp
