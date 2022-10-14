#!/bin/bash

# turns cpu cores on and off.
# asks for sudo password with a gui prompt;
# and includes retries and validation.
# built to run fussy older games on the steam deck.
if [[ "$1"  == "on" ]] ; then toggle=1
elif [[ "$1" == "off" ]] ; then toggle=0
else
    echo "incorrect arguments. Specify 'on' or 'off'."
    exit
fi
for (( i=1; i<=5; i+=1)) ; do # try 3 times
    pw=$(zenity --password --title="toggle CPU Cores (Try $i/5)" 2>/dev/null ) # ask for sudo password in gui prompt
    if [[ -z "$pw" ]] ; then # if the password is empty, user cancelled
        zenity --text="Do you want to cancel?" --question 2>/dev/null
        cancel0continue1=$?
        if [[ $cancel0continue1 == 0 ]] ; then break ; fi
    fi
    echo $pw | sudo -kvS -p "" # check password is correct
    rc=$? # return code is 0 if it's the right password

    if [[ $rc == 0 ]] ; then # if the password correct, run the command
        for n in {1..5} ; do
            { echo "$pw" ; echo "$toggle" ; } | sudo -kS -p "" tee /sys/devices/system/cpu/cpu$n/online >/dev/null
        done
        echo "succeeded!"
        break
    fi #else, repeat the loop
done
