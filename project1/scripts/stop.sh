instances=$(aws ec2 describe-instances --query 'Reservations[*].Instances[*].[Placement.AvailabilityZone, State.Name, InstanceId]' --output text | grep us-west-2 | grep running | awk {'print$3}')

for i in $instances
do
	aws ec2 terminate-instances --instance-ids $i
done
