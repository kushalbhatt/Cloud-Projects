#!/bin/sh
input=$(</home/ec2-user/kushal/pifft/input1)
out1=$(</home/ec2-user/kushal/pifft/output1)
echo "{\"input\" : {\"DataType\": \"String\", \"StringValue\": \"$input\"}}" > o.json
aws sqs send-message --queue-url https://sqs.us-west-2.amazonaws.com/021757108105/pi-output-queue --message-body $out1 --message-attributes file://o.json
aws s3 cp /home/ec2-user/kushal/pifft/output1 s3://kushal-aws-sample/$input
aws ec2 terminate-instances --instance-ids $(curl -s http://169.254.169.254/latest/meta-data/instance-id)
echo "end"
