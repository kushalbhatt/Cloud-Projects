export AWS_DEFAULT_REGION=us-west-2
export AWS_ACCESS_KEY_ID=AKIAJ425TLEITI6R7ZCA
export AWS_SECRET_ACCESS_KEY=O6VoBNroY+/Vt4SHeyztZ29Z/HJenUypXXr+EAQd
list=(`aws s3 ls s3://kushal-aws-sample | awk '{print $4}'`)
for ((i=0;i<${#list[@]};i++));
do 
	echo ${list[i]} `aws s3 cp --quiet s3://kushal-aws-sample/${list[i]} /dev/stdout`;
done;