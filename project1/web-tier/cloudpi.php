<?php
require 'vendor/autoload.php';
use Aws\S3\S3Client;
use Aws\S3\Exception\S3Exception;
use Aws\Sqs\SqsClient;
set_time_limit (600);
$inp = $_GET["input"];
echo "The input given is";
echo $inp;
$bucket = 'kushal-aws-sample';
$keyname = $inp;
$s3_var = S3Client::factory(array(
'key' => 'AKIAJ425TLEITI6R7ZCA',
'secret'=>'O6VoBNroY+/Vt4SHeyztZ29Z/HJenUypXXr+EAQd'
));
$info = $s3_var->doesObjectExist($bucket,$keyname);
if ($info)
{
$result = $s3_var->getObject(array(
        'Bucket' => $bucket,
        'Key' => $keyname
));
echo "<br> Pi = ";
echo $result["Body"];
}
else
{
$sqs = SqsClient::factory(array(
'region' => 'us-west-2',
'version' => 'latest',
'credentials' => array(
'key' => 'AKIAJ425TLEITI6R7ZCA',
'secret'=>'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',
)));
$result = $sqs->getQueueUrl(array('QueueName' =>'pi-input-queue'));
$queueUrl = $result->get('QueueUrl');
$sqs->sendMessage(array(
'QueueUrl' =>$queueUrl,
'MessageBody' => $inp,
));
while(true)
{
	$info = $s3_var->doesObjectExist($bucket,$keyname);
	if ($info)
	{
		$result = $s3_var->getObject(array(
        'Bucket' => $bucket,
        'Key' => $keyname
	));

	echo "<br> Pi = ";
	echo $result["Body"];
	break;
	}
}
}
?>