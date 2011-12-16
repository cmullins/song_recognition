#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $DIR/classpath_setup.sh
source $DIR/global_settings.sh

dbname=$1
test_dir=$2

if [ -z "$dbname" ] || [ -z "$test_dir" ]; then
	echo "Syntax is: classify.sh <db_name> <test_dir>"
	exit 1
fi

java -Xmx${MAX_HEAP_SIZE} -cp ${project_cp} org.sidoh.song_recognition.benchmark.Classifier "$dbname" "$test_dir"/*
