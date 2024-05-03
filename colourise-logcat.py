#!/usr/bin/env python

########################################################################
#
# Colourise-Logcat.py
#
# Prints saved logcat output with colour highlighting 
# for different error levels
# (Verbose, Debug, Info, Warn, Error, Assert)
#
########################################################################

import sys, re

#the command to set the foreground colour to one of the 216 allowed colours
esc_code = "\x1b["
set_fg = "38;5;"

levels = (
    ('V', set_fg+"129m"),#verbose
    ('D', set_fg+"33m"),#debug
    ('I', "m"),         #reset back for info
    ('W', set_fg+"220m"),#warn
    ('E', set_fg+"160m"),#error
    ('A', set_fg+"203m")#assert
)

def colourise(text):
    for thing in levels:
        if re.search(" "+thing[0]+"(/| [^:]+:)", text) is not None:
            #print(re.search(" "+thing[0]+"/", text))
            return esc_code+thing[1]+text
    return text

try:
    if len(sys.argv) == 2: #print logcat file with colours
        for line in open(sys.argv[1]).readlines():
            sys.stdout.write(colourise(line))
        sys.stdout.write(esc_code+"m")#reset terminal back to normal before exiting
    elif len(sys.argv) == 1: #print continuous input from stdin
        while True:
            line = sys.stdin.readline()
            if len(line) > 0:        
                sys.stdout.write(colourise(line))
            else:
                break
        sys.stdout.write(esc_code+"m")#reset terminal back to normal before exiting
    else:
        print("no arguments! usage:")
        print (sys.argv[0]+" <file>")
        print ("where <file> is a plaintext file containing logcat output")
        quit()
finally:
    sys.stdout.write(esc_code+"m")#whatever happens, reset the terminal colours back to normal
