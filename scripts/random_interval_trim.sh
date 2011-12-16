#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source $DIR/global_settings.sh

######
######
###### Script that creates clips from a WAV using a randomly generated interval

###### SETTINGS ########
SAMPLE_LENGTHS="20"

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

for length in $SAMPLE_LENGTHS; do
	song_len=$(sox $wav_file -n stat 2>&1 | grep Length | awk '{printf("%d", $NF)}')
	max_start_location=$(($song_len - $length))
	start_location=$RANDOM
	let "start_location %= $max_start_location"
	end_location=$(($start_location + $length))

	sample_name=$samples_dir/$(basename "$wav_file" .wav)-$start_location-to-$end_location.wav
	
	echo "Trimming: $sample_name"
	sox $wav_file $sample_name trim $start_location $length
done
