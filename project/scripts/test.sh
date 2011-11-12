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

rm -rf $report
java -Xmx${MAX_HEAP_SIZE} -cp ${project_cp} org.sidoh.song_recognition.benchmark.BulkTest $dbname $report $DIR/../songs/samples/*
