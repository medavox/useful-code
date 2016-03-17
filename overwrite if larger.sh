#!/bin/bash
#foreach file in source dir:
#	if there exists a file with the same name in dest dir:
#		as long as source file is not just zeroes,
#			and it has a valid magic number for its file extension
#				if source file is bigger than dest:
#					overwrite dest file with it
#					print this info to screen
#		else
#			do nothing
#print number of files overwritten, 

source=$1
dest=$2

