#!/bin/bash

for f in $(ls plots/*.png); do
	convert $f plots/$(basename $f .png).eps
done
