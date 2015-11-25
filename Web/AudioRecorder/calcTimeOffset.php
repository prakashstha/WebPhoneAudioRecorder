<?php
	/*if (is_ajax()) {
		if (isset($_POST["action"]) && !empty($_POST["action"])) { //Checks if action value exists
			$action = $_POST["action"];
			switch($action) { 
				//Switch case for value of action
				case "syncTime": 
					syncTime(); 
					break;
			}
		}
	}
	
	//Function to check if the request is an AJAX request
	function is_ajax() {
		return isset($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) == 'xmlhttprequest';
	}*/
	//syncTime();
	function syncTime(){
		$return = $_POST;
		$return["server_time"] = round(microtime(true) * 1000);
		$return["error"] = false;
		$return["message"] = "from timesync server";
		//$return["json"] = json_encode($return);
		echo json_encode($return);
	}
	syncTime();
	?>