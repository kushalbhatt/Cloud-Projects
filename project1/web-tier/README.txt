The web tier is running a cloudpi.php script which is hosted on the Apache web server.

Please run start.sh shell script to launch and start the server which is hosted on the EC2 instance.
We have already configured the server with desired php sccript files and configuration to run apache server. 

And then We took a snapshot of that EBS  volume and created a custom AMI for launching similar servers. 

At the launch time you need to supply server start command to start the web server. start.sh uses the userdata_web_tier.txt files which contains the required launch time userdata for the web tier.

And then assigns the public elastic IP to the launched instance.

In short just run the start.sh and wait for ~~ 1-2 mins the server will be up and running. Then you can submit requests for PI calculation by  http://PUBLIC-IP/cloudpi.php?input=x

NOTE: The script will timeout after 10 mins of waiting. For huge input values the computation can take several minutes. So if page expires after 10 mins just refresh it after waiting for a while.

  