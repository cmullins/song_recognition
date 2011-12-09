#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $DIR/classpath_setup.sh
source $DIR/global_settings.sh

wavfile=$1
if [ -z "$wavfile" ]; then
	echo "No wavfile specified. Syntax is: spectrogram.sh <wavfile> [outfile = wavfile.pnm]"
	exit 1
fi

outfile=$2
if [ -z "$outfile" ]; then
	outfile=$(basename "$wavfile" .wav).png
	echo "No outfile specified, defaulting to: $outfile"
fi

rm -rf $report
java -Xmx${MAX_HEAP_SIZE} -cp ${project_cp} org.sidoh.song_recognition.benchmark.CreateSpectrogram $wavfile $outfile
