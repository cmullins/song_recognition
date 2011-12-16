#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

file=$1
if [ -z "$file" ]; then
	echo "Syntax is: gather_stats.sh <file>"
	exit 1
fi

R --no-save --slave --args "$file" < $DIR/gather_stats_.R
