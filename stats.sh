#!/bin/bash
# stats
mkdir -p stats
blank=
save=stats/stats_$1.csv
> $save
timeout=3
K=154  ## 4th character starting from index 0
L=350 ## 6th character starting from index 0
nb=10
FILES=./pddl/$1/p[0-1][0-9].pddl

echo ---- Stats for problem $1 ----
for file in $FILES
do
	f="$(basename -- $file)"
	sat=$sat,$f
done
echo $sat >> $save

sat=sat
for file in $FILES
do
	f="$(basename -- $file)"
	echo " "doing $f of $1 with sat...
	res=`bash start.sh -p sat -d $1 $f -t $timeout -l timings $2`
	sat=$sat,$res
done
echo $sat >> $save

sat=astar
for file in $FILES
do
	f="$(basename -- $file)"
	echo " "doing $f of $1 with astar...
	res=`bash start.sh -p astar -d $1 $f -t $timeout -l timings $2 | tr -d "\n\r"`
	res="${res:K:L - K + 1}"
	sat=$sat,$res
done
echo $sat >> $save

# | cut -d' ' -f 10