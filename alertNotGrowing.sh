#!/bin/bash
while [ 1 ] ; do
	last_size=$size
	size=$(du /media/noob/aimDebDrive.hdd)
	echo $size
	if [ "$last_size" = "$size" ] ; then
		mplayer -novideo /home/adam/Videos/omd.mp4
		break
	fi
	sleep 12
done
