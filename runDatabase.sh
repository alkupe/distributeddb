r=$(( $RANDOM % 4000 + 4000 ));


for (( c=$r; c<$r+10; c++ ))
do
	s+=$c;
	s+=" ";
	java -jar "DM/dist/DM.jar" $c &
done
#echo $s;
sleep 1
java -jar "TM/dist/TM.jar" $s
#echo $s
trap 'kill $(jobs -pr)' SIGINT SIGTERM EXIT
