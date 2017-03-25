export AWS_DEFAULT_REGION=us-west-2
export AWS_ACCESS_KEY_ID=AKIAJ425TLEITI6R7ZCA
export AWS_SECRET_ACCESS_KEY=O6VoBNroY+/Vt4SHeyztZ29Z/HJenUypXXr+EAQd

aws s3 rm --quiet s3://kushal-aws-sample --recursive
id=$(aws ec2 run-instances --image-id ami-48a62828 --count 1 --instance-type t2.micro --key-name aws_tutorial --user-data file://userdata/userdata_web_tier.txt --security-group-ids sg-1cb2c664 --query 'Instances[0].InstanceId')

id=$(echo $id | tr -d \")

aws ec2 run-instances --image-id ami-4ca7292c --count 1 --instance-type t2.micro --key-name aws_tutorial --user-data file://userdata/userdata_java.txt --security-group-ids sg-1cb2c664

sleep 30;aws ec2 associate-address --instance-id $id  --public-ip 52.41.116.118
