#!/bin/bash

if [ ! -e plots ]; then
	mkdir plots
fi

for plot in $(ls ../plots/**.eps); do
	new_name=plots/$(basename "$plot" .eps).eps

	if [ ! -f "$new_name" ] || [ $plot -nt $new_name ]; then
		echo $new_name
		cp $plot $new_name
	fi
done
