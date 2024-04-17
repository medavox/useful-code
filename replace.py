# replaces every instance of the first argument in the input (read from stdin) with the second argument.
# supports perl-like python regex
# use as a simpler replacement for sed or awk:
# alias replace='python3 /path/to/replace.py'
import sys, re

input = sys.stdin.read().strip(" \n")
if len(sys.argv) < 3:
    print("ERROR: you must supply at least 2 arguments.", file=sys.stderr)
    exit(1)
print(re.sub(sys.argv[1], sys.argv[2], input))