#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source $DIR/global_settings.sh

######
######
###### Script that creates clips from a WAV using a fixed set of intervals.

###### SETTINGS #######
START_LOCATIONS=$(seq 12 40 120)
SAMPLE_LENGTHS="15"

wav_file=$1
samples_dir=$2
if [ -z "$wav_file" ] || [ -z "$samples_dir" ]; then
	echo "Syntax is: fixed_intervals_trim.sh <wav_file> <samples_dir>"
	exit 1;
fi

if [ ! -e "$samples_dir" ]; then
	echo "Samples directory doesn't exist! Creating it."
	mkdir -p $samples_dir
fi

song_len=$(sox $wav_file -n stat 2>&1 | grep Length | awk '{printf("%d", $NF)}')
for start_location in $START_LOCATIONS; do
	for length in $SAMPLE_LENGTHS; do
		end_location=$(($start_location + $length))

		# If the end location goes past the length of the song, then forget about
		# this sample. It'll screw up stats.
		if [ $end_location -le $song_len ]; then
			sample_name=$samples_dir/$(basename "$wav_file" .wav)-$start_location-to-$end_location.wav

			if [ ! -f "$sample_name" ]; then
				echo "Trimming: $sample_name"
				sox $wav_file $sample_name trim $start_location $length
			fi
		fi
	done
done
