#!/bin/bash

PS_TO_INSERT=$(printf '%q', "
systemdict /setdistillerparams known {
<< /AutoFilterColorImages false /ColorImageFilter /FlateEncode >>  
setdistillerparams
} if" | sed "s/['$,]//g")

cd plots

for f in $(ls *.png); do
	toname=$(basename $f .png).eps
	convert $f $toname

	# Force distiller to use high quality images to avoid obvious JPEG
	# artifacts in PDFs.
	tmp_name=$(mktemp -t $toname.XXXXXXXXXXXX)
	insert_position=$(($(grep -n "% " $toname | head -n 1 | cut -d':' -f1) - 1))
	awk -v "n=${insert_position}" -v "s=${PS_TO_INSERT}" '
		(NR == n) { n = -1; print s; }
		          { print $0 }' $toname > $tmp_name
	mv $tmp_name $toname
done
