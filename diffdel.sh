#!/bin/bash
diff -raq $1 $2 > /tmp/diffresults.txt
result=$(cat /tmp/diffresults.txt)
if [ -z "$result" ] ; then
	echo files are identical, removing $1
	rm -r $1
else
	echo "$1" and $2 differ:
	echo "$result"
fi
