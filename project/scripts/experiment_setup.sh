#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source $DIR/global_settings.sh

#####
##### Script to batch-process MP3s. Converts them to raw WAVs, takes samples of them,
##### adds noise, and downconverts them.

##### Locations of sound files
MP3S_DIR=$DIR/../songs/mp3s
WAVS_DIR=$DIR/../songs/wavs
SAMPLES_DIR=$DIR/../songs/samples
NOISE_DIR=$DIR/../songs/noise

if [ ! -e "$SAMPLES_DIR" ]; then
	echo "Samples directory doesn't exist: $SAMPLES_DIR! Creating it..."
	mkdir -p "$SAMPLES_DIR"
fi

##### Experiment settings

# Which script to use to downconvert samples (not training files)
#DOWNCONVERT_SCRIPT=echo
TRIM_SCRIPT=$DIR/random_interval_trim.sh
DOWNCONVERT_SCRIPT=$DIR/gsm_downconvert.sh

###### BEGIN PROCESSING

cd $MP3S_DIR
find . -name "*.mp3" -print0 | while read -d $'\0' mp3; do
	wavname=$WAVS_DIR/$(basename "$mp3" .mp3 | sed 's/^[^a-zA-Z]*//g' | tr '[A-Z]' '[a-z]' | sed 's/[^a-z0-9]/_/g' | sed 's/__*/_/g' | sed 's/^_*//g' | sed 's/_*$//g').wav

	if [ ! -f $wavname ]; then
		echo "Converting to WAV: $mp3..."
		mpg123 -q -r ${SAMPLE_RATE} -m -w "$wavname" "$mp3"
	fi
done

cd $WAVS_DIR
for wav in $(ls *.wav); do
	$TRIM_SCRIPT "$wav" "$SAMPLES_DIR"
done

cd $SAMPLES_DIR
for sample in $(ls *.wav | grep "[0-9][0-9]*.wav"); do
	sample_len=$(sox $sample -n stat 2>&1 | grep Length | awk '{printf("%d", $NF)}')

	for noise in $(ls $NOISE_DIR/*.wav); do
		noise_len=$(sox $noise -n stat 2>&1 | grep Length | awk '{printf("%d", $NF)}')
		noise_name=$(basename "$noise" .wav)
		new_sample_name=$SAMPLES_DIR/$(basename $sample .wav)-added-noise-$noise_name.wav

		# If the noise is longer than the sample, trim the noise.
		if [ $noise_len -gt $sample_len ]; then
			noise_tmp=$(mktemp -t $(basename "$noise_name"))
			mv $noise_tmp $noise_tmp.wav
			noise_tmp=${noise_tmp}.wav

			sox $noise $noise_tmp trim 0 $sample_len
			noise=$noise_tmp
		fi

		if [ ! -f "$new_sample_name" ]; then
			echo "Adding noise: $new_sample_name"
			sox -m -r ${SAMPLE_RATE} $sample -r ${SAMPLE_RATE} $noise $new_sample_name trim 0 $sample_len
		fi
	done
done

cd "$SAMPLES_DIR"
find . -name "*.wav" -print0 | xargs -0 -n 1 -P ${MAX_PARALLEL_PROCESSES} -I {} \
	bash -c "${DOWNCONVERT_SCRIPT} {}"
