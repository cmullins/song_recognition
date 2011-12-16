#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source $DIR/global_settings.sh

## Script that takes as input a WAV file and distorts it by converting it to
## a low-quality format (GSM).

infile=$1
if [ -z "$infile" ]; then
	echo "Syntax is: gsm_downconvert.sh <infile>"
	exit 1
fi

echo "Downconverting: $infile"

tmpfile=$(mktemp -t $infile).gsm

sox -r ${SAMPLE_RATE} -G $infile -r 8000 -G $tmpfile
sox -G -r 8000 $tmpfile -G $tmpfile.mp3 
mpg123 -q -r ${SAMPLE_RATE} -m -w $infile $tmpfile.mp3 
