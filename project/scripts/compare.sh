#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $DIR/classpath_setup.sh
source $DIR/global_settings.sh

wavfile=$1
if [ -z "$wavfile" ]; then
	echo "No train file specified."
	echo "Syntax is: spectrogram.sh <train> <test1> [test2, test3, ..., testN]"
	exit 1
fi

if [ -z "$2" ]; then
	echo "Need to specify at least one test file."
	echo "Syntax is: spectrogram.sh <train> <test1> [test2, test3, ..., testN]"
	exit 1
fi

java -Xmx${MAX_HEAP_SIZE} -cp ${project_cp} org.sidoh.song_recognition.benchmark.Compare $wavfile ${@:2}
