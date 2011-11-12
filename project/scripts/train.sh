#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $DIR/classpath_setup.sh
source $DIR/global_settings.sh

dbname=$1
if [ -z "$dbname" ]; then
	echo "No dbname specified. Defaulting to signatures"
	dbname=$DIR/../signatures
fi

rm -rf "$dbname*"
java -Xmx${MAX_HEAP_SIZE} -cp ${project_cp} org.sidoh.song_recognition.database.BulkDbBuilder $dbname $DIR/../songs/wavs/*
