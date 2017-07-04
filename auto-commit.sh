#!/bin/bash
#every 2 hours, check a file on a site I control
#if we have uncommitted changes, AND the file has a recent enough timestamp,
#automatically commit and push those changes to master
REMOTE_FILE='https://medavox.github.io/autocommit.txt'
CHECK_PHRASE="do auto-commit"

cd ~/src/medi/
git diff --quiet
result=$?
if [ $result -eq 1 ] ; then
	checkline=$( curl $REMOTE_FILE )
	if [[ $checkline = $CHECK_PHRASE ]] ; then
        echo remote file matches string, committing and pushing...
		git commit -am "auto-commit performed by remote request" && git push origin modules
    else
        echo remote file contains incorrect string, not committing
	fi
else
    echo no local changes, no need to commit-push
fi
