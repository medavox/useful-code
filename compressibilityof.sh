#!/bin/bash
## Compresses a 4096-byte chunk from the middle of a file
# and returns the percent size of the original.
# Acts as a rough guide of a file's compressibility
function filesize {
	du -b $1 | { read first rest ; echo $first ; }
}
size=$(filesize $1)
halfway=$(($size/2))
blox=$(($halfway/4096))
dd if=$1 bs=4096 skip=$blox count=1 2>/dev/null | gzip > /tmp/compresstest.gz
compsize=$(filesize /tmp/compresstest.gz)
marg=$((compsize * 100))
percent=$(($marg / 4096))
echo compressed size is $percent% of original
rm /tmp/compresstest.gz
