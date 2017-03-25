/*************************************
Kushal Bhatt
Sarvesh Kulkarni

CSE546 Cloud Computing  Project 1
**************************************/
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.elasticmapreduce.model.InstanceState;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;


public class ec2commands {
	
	static AmazonEC2 ec2Client; 
	static String imageid="ami-14a92574";
	
	static AmazonS3Client s3CLient;
	
	static String keyname = "aws_tutorial";
	static String sg= "default";
	
	static String instanceid="i-07ef07c6fe75a5301";
	static ArrayList<String> ids = new ArrayList<String>();
	
	static String accessKey="AKIAJ425TLEITI6R7ZCA";
	static String secretKey="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";
	static String queuename = "pi-input-queue";
	static String queueUrl = "https://sqs.us-west-2.amazonaws.com/021757108105/pi-input-queue";
	static int running=0;
	static BasicAWSCredentials awsCredentials;
	
	static AmazonSQS sqs;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ec2Client  = AmazonEC2ClientBuilder.standard().withRegion("us-west-2").build();
				
		awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		sqs = new AmazonSQSClient(awsCredentials);
		sqs.setEndpoint("https://sqs.us-west-2.amazonaws.com");
		
		Receiver r = new Receiver(queueUrl);
		r.start();		
	}
	
	
	static void createInstance(String input)
	{
		String userdata= "#!/bin/bash\n cd /home/ec2-user/kushal/pifft\n input="+input+"\n echo $input>>input1\n ./pifft input1 > output1\n > o.json\n /home/ec2-user/./upload.sh\n";
		String formattedstring = Base64.encodeBase64String(userdata.getBytes());
		
		RunInstancesRequest run = new RunInstancesRequest();
		run.withImageId(imageid).withInstanceType("t2.micro").withMinCount(1).withMaxCount(1).
			withKeyName(keyname).withSecurityGroups(sg).withUserData(formattedstring);
		
		
		RunInstancesResult result = ec2Client.runInstances(run);		
		System.out.println("Instance Launched: "+result);
		
		//get the instance-id of newly created instance 
		String j_data = result.toString();
		int start = j_data.indexOf("InstanceId: ");
		start+=12;
		int end = j_data.indexOf(",", start);
		String id = j_data.substring(start, end);
		System.out.println("instance id "+ id);
		
		ids.add(id); //add this to the arraylist
	}
			
	static void checkinstances()
	{
				
		DescribeInstancesResult describeInstancesRequest = ec2Client.describeInstances();
		    List<Reservation> reservations = describeInstancesRequest.getReservations();		
		    for (Reservation reservation : reservations) 
		    {
		    	 List<Instance> instances = reservation.getInstances();
		    	
		      for (Instance instance : instances) 
		      {
		    	  //instance do whatever you want to
		    	  if(ids.contains(instance.getInstanceId()))
		    	  {
		    		  int status = instance.getState().getCode();
		    		  if(status == 48 || status == 32) //shuttingdown or terminated?
		    		  {
		    			  running--;
		    			  ids.remove(instance.getInstanceId()); //remove it from the list we don't need it anymore
		    			  System.out.println("Updated Count:: "+running);
		    		  }
		    	  }		    	 
		      }
		    }
	}
	
	static class Receiver extends Thread
	{
		int count;
		String url;
		private int threshold=9; //for deployment make it 9
		Receiver(String url)
		{
			//count = n;
			this.url = url;
		}
		
		@Override
		public void run() 
		{
			//infinite loop   won't stop unless explicitly closed
			//hungry for messages   -> app tier
	
			while(true)
			{
				if(running<threshold)
				{
					ReceiveMessageResult receive = sqs.receiveMessage(new ReceiveMessageRequest(url).withMaxNumberOfMessages(threshold-running));//(threshold-running))//.withMessageAttributeNames("input"));
					//List<String> attr = new ArrayList<String>();
					//attr.add("ApproximateNumberOfMessages");
					//n = Integer.parseInt(sqs.getQueueAttributes(url,attr).getAttributes().get("ApproximateNumberOfMessages"));					
					List<Message> msgs = receive.getMessages();
					//System.out.println("Approx Message Size = "+n+"actually Received: "+msgs.size());

					for(int i=0;i<msgs.size();i++)
					{
						Message m = msgs.get(i);
						//String value = m.getMessageAttributes().get("input").getStringValue();
						System.out.println("\tReceived message: "+m.getBody());
						//delete message after received for avoiding duplicate reads

						//launch instance who will compute pifft ; suuply no of iterations
						//body of the message contains the required input
						
						ec2commands.createInstance(m.getBody());
						running+=1; //increment count
						sqs.deleteMessage(new DeleteMessageRequest(url,m.getReceiptHandle()));
					}
						
					System.out.println("\nAfter Launches: count = "+running);
					
					if(msgs.size()==0)
					{
						System.out.println("I/p Queue Empty..... sleep..zzzzzzz");
						try {
							ec2commands.checkinstances();//check here also for consistency
							Thread.sleep(5000);							
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//sleep for few seconds and wake up again
					}
				}//end of running if
				else
				{
					//if running 9 or more wait for instances to finish their task 
					//and update count accordingly					
					try {
						//wait for a sec saving network queries
						ec2commands.checkinstances();
						Thread.sleep(2000);						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			}
		}
	}
}

