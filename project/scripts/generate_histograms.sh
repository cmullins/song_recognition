#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

dir=$1
if [ -z "$dir" ]; then
	dir=.
fi

cd $dir
for data in $(ls . | grep "[01]\.[0-9]*[0-9]$"); do
	file=${dir}/${data}
	png=${dir}/$(basename $data).png

	if [ ! -f "$png" ] || [ $data -nt $png ]; then
		echo "Generating histogram for $data"
		R --no-save --slave --args "$file" < $DIR/generate_histogram_.R &>/dev/null
	fi
done
