waitTime=150
$changedRecently=./.recentChangeLock
if [ ! -f recentChangeLock  ] ; then
	touch $changedRecently
	sleep $waitTime
	rm $changedRecently
fi
