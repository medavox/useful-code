cd /usr/bin
for f in $(ls)
do
	if [ $(file -b --mime-type ${f}) = "text/plain" ]
	then
		echo $f
	fi
done
