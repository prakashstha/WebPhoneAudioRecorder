<?php

$action = $_POST['action'];

if(strcasecmp($action, 'audio_time_info') == 0){
	$timeStamp = $_POST['eventTime'];
	$infoType = $_POST['infoType'];
	$fields = array($infoType, $timeStamp);
	
	$file = $_POST['fileName'];//getFileName('time_info');
	$fp = fopen($file, 'a');
	if($fp){
		fputcsv($fp, $fields);
		fclose($fp);
		$response["error"] = false;		
		$response["message"] = 'Success!!'; 
		echo json_encode($response);
	}
	else {
		$response["error"] = true;		
		//$response["message"] = $elements; 
		echo 'unable to open file...\n'.$file.'\n'.json_encode($response);
	}
}
else if(strcasecmp($action, 'time_sync_info') == 0){
	$request_time = $_POST['request_time'];
	$server_time = $_POST['server_time'];
	$response_time = $_POST['response_time'];
	$avgTripTime = $_POST['avgTripTime'];
	$avgTimeDiff = $_POST['avgTimeDiff'];
	$fields = array($request_time,$server_time, $response_time,$avgTripTime,$avgTimeDiff);
	
	$file = $_POST['fileName'];
	$fp = fopen($file, 'a');
	if($fp){
		fputcsv($fp, $fields);
		fclose($fp);
	}
	else {
		echo 'unable to open file...'.$file;
	}
	$response["error"] = false;		
    //$response["message"] = $elements; 
    echo json_encode($response);
}



?>

