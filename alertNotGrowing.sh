#!/bin/bash
#alerts the user if the file in $1 hasn't changed for a while
while [ 1 ] ; do
	last_size=$size
	size=$(du $1)
	echo $size
	if [ "$last_size" = "$size" ] ; then
		#add an appropriate alert here
		mplayer -novideo /home/adam/Videos/omd.mp4
		break
	fi
	sleep 12
done
