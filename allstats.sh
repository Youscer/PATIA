#!/bin/bash
DOMAINES=./pddl/*/

for d in $DOMAINES
do
dirname="$(basename -- $d)"
bash stats.sh $dirname
done
