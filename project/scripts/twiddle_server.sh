#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $DIR/classpath_setup.sh
source $DIR/global_settings.sh

clips_dir=$1
if [ -z "$clips_dir" ]; then
	clips_dir=$DIR/../songs/samples
	echo "No clips dir specified. Using: $clips_dir"
fi

echo "Starting twiddle server on port 8000..."
java -Xmx${MAX_HEAP_SIZE} -cp ${project_cp} org.sidoh.song_recognition.benchmark.SettingsTwiddler -sd "$clips_dir"
