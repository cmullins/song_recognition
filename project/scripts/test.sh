#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $DIR/classpath_setup.sh
source $DIR/global_settings.sh

dbname=$1
if [ -z "$dbname" ]; then
	echo "No dbname specified. Defaulting to signatures"
	dbname=$DIR/../signatures
fi

report=$2
if [ -z "$report" ]; then
	echo "No report name specified. Defaulting to report"
	report=$DIR/../report
fi

test_dir=$3
if [ -z "$test_dir" ]; then
	test_dir=$DIR/../songs/samples
	echo "No test dir specified using default: $test_dir"
fi

rm -rf $report
java -Xmx${MAX_HEAP_SIZE} -cp ${project_cp} org.sidoh.song_recognition.benchmark.BulkTest $dbname $report $test_dir/*
