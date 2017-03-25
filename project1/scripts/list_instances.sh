export AWS_DEFAULT_REGION=us-west-2
export AWS_ACCESS_KEY_ID=AKIAJ425TLEITI6R7ZCA
export AWS_SECRET_ACCESS_KEY=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

instances=$(aws ec2 describe-instances --query 'Reservations[*].Instances[*].[Placement.AvailabilityZone, State.Name, InstanceId]' --output text | grep us-west-2 | grep running | awk {'print$3}')
#my_array=$(aws ec2 describe-instances --query 'Reservations[*].Instances[*].[InstanceId]' --output text)

now=$(date +"%Y-%m-%dT%H:%M:%S")
today=$(date -I)
t="T00:00:00"
start=$today$t
echo "instance_id         -    Usage %"
echo "================================"

for i in $instances
do
	#i=$i | head -c -1
	
	usage=$(aws cloudwatch get-metric-statistics --metric-name CPUUtilization --start-time $start --end-time $now --period 3600 --namespace AWS/EC2 --statistics Maximum --dimensions Name=InstanceId,Value=$i --query 'Datapoints[*].[Maximum]' --output text)
	 
	echo $i " -  " $usage				

done
#printf '%s\n' "${my_array[@]}"
