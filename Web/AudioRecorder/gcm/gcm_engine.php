<?php

define("GOOGLE_API_KEY", "AIzaSyCCJ_DjdTT1cSy6V8NDwoy_VwN8LdF4EjM ");
define("GOOGLE_GCM_URL", "https://android.googleapis.com/gcm/send");
$regist_id = "APA91bE3Vty6JWJEhsizHn_0paKFU36lGXdJupaIGGsIY5vToAxl7e5f1FJIsA8H5L2RCFSH9NXMXtg2ScZCr0-yKA0jCI7GOF11jtNtL-7rbKuLCHefy0HMZtJDTfRIo9AivZb2jK5qDzy9tHpnTUgL_aTnl4w1TQ";
	
//echo $reg_id;
//send_gcm_notify($reg_id, $msg);
echo "inside gcm_engine";

if(isset($_POST['message'])){
	$msg = $_POST['message'];
	send_gcm_notify($regist_id, $msg);
	//sendMessageToRegisteredDevices($msg);
}
else{
	echo "Message not set";
}

function send_gcm_notify($reg_id, $message) {
 
    $fields = array(
		'registration_ids'  => array( $reg_id ),
		'data'              => array( "message" => $message ),
	);
				
	$headers = array(
		'Authorization: key=' . GOOGLE_API_KEY,
		'Content-Type: application/json'
	);
	
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, GOOGLE_GCM_URL);
	curl_setopt($ch, CURLOPT_POST, true);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
	curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

	$result = curl_exec($ch);
	if ($result === FALSE) {
		die('Problem occurred: ' . curl_error($ch));
	}

	curl_close($ch);
	//echo $result;
 }






?>