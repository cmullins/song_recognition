#!/bin/bash

# If a png for a corresponding pgm doesn't exist or is older than the
# pgm, convert it to get a new one.

directory=$1

if [ -z "$directory" ]; then
	directory="."
fi

for i in $(ls $directory/*.pgm); do
	png=$(basename $i .pgm).png

	if [ ! -e $png ] || [ $i -nt $png ]; then
		echo "Converting $i --> $png..."
		convert $i $png
	fi
done
