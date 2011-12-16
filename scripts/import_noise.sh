#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
NOISE_DIR=$DIR/../songs/noise

source $DIR/global_settings.sh

infile=$1
if [ -z "$infile" ]; then
	echo "Syntax is: import_sound.sh <infile> [outfile]"
	exit 1
fi

if [ ! -f "$infile" ]; then
	echo "$infile doesn't exist!"
	exit 1
fi

outfile=$2
if [ -z "$outfile" ]; then
	outfile=$NOISE_DIR/$(basename "$infile")

	if [ $(basename '$outfile' .wav) == $(basename '$outfile') ]; then
		outfile=${outfile}.wav
	fi

	echo "No outfile specified. Using default: $outfile"
else 
	outfile=$NOISE_DIR/$outfile
fi

sox $infile -G -c 1 -r ${SAMPLE_RATE} $outfile
