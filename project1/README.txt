Project Members:

Kushal Bhatt       1211206721
Sarvesh Kulkarni   1210322864

Public Elastic IP of web tier:  
52.41.116.118

NOTE::  Once the instance is launched. We will assign the Elastic IP from the start.sh script after some delay as for this command instance must be in the running state(not pending). Sometimes it takes around 2 minutes for IP assignment to take effect.

start script requires two userdata files that are present in userdata folder. Please make sure scripts have access to them. For ease of use I have put the two files in userdata folder under the scripts folder. Please keep them as it is.  If you change the location then you need to change the path of the files in the script as well.

If you receive "permission denied" error while running the scripts. Please change the permission by executing following command.

sudo chmod 777 [filename]

Also for ease of testing we have put the export creddentials statements in all the scripts. 


The project uses AWS SQS, S3, EC2, AWS JAVA and PHP SDK.

The overall structure of the project is as follows.

PHP WEB Server ::
				get the input
				check 
				if computed result is already available in S3?   (Bucket serves as a ~~ cache as well.)
					yes then return the response.(No need to compute)
				else
					Submit the request to SQS input queue. 
					And wait for the result to be available in S3.
					Once it is available return the response.
					
JAVA App-Tier Driver:
				Performs auto-scaling and launches instances to compute pi value.
				Waits on SQS message queue.
				Message will be deleted from the input queue once served .(So no duplicated computation).
				
				
For any other queries or questions feel free to contact us:
Kushal Bhatt  		email: Kushal.Bhatt@asu.edu   contact# 602-702-4309
Sarvesh Kulkarni	email: skulka19@asu.edu


			


