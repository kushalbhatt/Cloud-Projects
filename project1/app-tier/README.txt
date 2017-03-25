The app tier consists of EC2 instances who will compute pi value based on the iteration provided by web tier ON-DEMAND.

Initially only one app-tier instance will be running. This instance is running a JAVA code . Source code is present in this folder filename is : ec2commands.java. (I aplogize for the bad naming convention but redeploying was a lengthy process. )

This java app is a driver for the whole app tier.  It is waiting for messages in SQS input queue. If there are any pending messages in the queue then it will read them and launch an app-tier EC2 instance that contains the pifft c code. 

It supplies the necessary input to run the pifft at the time of launch via userdata.

This is the userdata provided to pifft EC2.
 
#!/bin/bash
echo "new launch"
cd /home/ec2-user/kushal/pifft
input=value
echo $input > input1
./pifft input1 > output1
> o.json
/home/ec2-user/./upload.sh
 

Once the computation is done the output is stored in a file local to ec2 instance. Then the script upload.sh is run. 
This script uploads the output file to S3 bucket and also writes the output to SQS output-queue. S3 bucket object name would be  the input-value for that iteration. After uploading it the instance will terminate itself as it is no longer needed.

upload.sh is provided in MISC folder.

JAVA driver is allso performing the Autoscaling. At a time at most there will be 9 App-tier instances will be running and computing pi values.  Hence total app-tier count will always be <= 10.

If the count is 10 then java driver will wait for few instances to finish their task before serving new mesaage requests.
The logic is inside the ec2commands.java . It keeps a global counter for managing this. Code is self explanatory. 


The JAVA driver code is busy-waiting in an infinite loop waiting for the messages to be served. 
Please run the start.sh script it launches and starts the JAVA drive app-tier instance. Rest will be handled. 

This will be up and running untill forcefully closed by executing stop.sh .
 