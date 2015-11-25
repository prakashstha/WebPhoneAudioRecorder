<!DOCTYPE HTML>
<html>
	<head>
		<title>Sign-In</title>
		<link rel="stylesheet" type="text/css" href="css/style.css">
	</head>
	<?php
		$dir = "files";
		//create necessary directory to store recordings
		if (!file_exists($dir) ) {
			mkdir($dir);
			echo "Directory ".$dir." created...";
		}
	?>
	<body id="body-color" >
		<br/><br/>
		Place: <input type="text" name="place" id = "place" value=""/><br/>
		<br/>
			<br/>
		<button id="start">Start Recording</button>
		<button id="stop" >Stop Recording</button>
			<br/>
			<br/>
		<div id="container" style="padding:1em 2em;"></div>
		<div id = "audioRecorder">Audio Recorder Info</div>
		<div id = "timeSynchronizer" class = "demo">Time Synchronizer Info</div>
		<div id = "gcm" class = "demo">GCM Info</div>
		
		<script src="js/jquery.js"></script>
		<script src="js/AudioRecorder.js"></script>
		<script src="js/RecordRTC.js"></script>
		<script src="js/FileUtility.js"></script>
		<script src="js/TimeSynchronizer.js"></script>
		<script src="js/gcm.js"></script>

		<script>
			var bAudioFile, bAudioTimeFile, bTimeSyncFile;
					
			$("document").ready(function(){
					
					var GCMStart = "start";
					var GCMStop = "stop";
					/* get the fields from the login page based on their id */
					var stop = document.getElementById('stop');
					var start = document.getElementById('start');
					var place = document.getElementById('place');
					/* UI field to show the GCM messages*/
					var gcmField = document.getElementById('gcm');
		

					/* ends of fileName */
					var audioEnds = 'bAudio';
					var audioTimeEnds = 'bAudioTime';
					var timeSyncEnds = 'bTimeSync';
					
					stop.disabled = true;
				
					/* start button click event listener*/
				    start.onclick = function(){
				    	console.log('start clicked');
						
						var placeValue = place.value;
						var dir = "<?php echo $dir?>";
						var date = new Date();
						var timeVar = date.getTime();

						var msgToDevice = GCMStart + ',' + timeVar +'_'+ placeValue;
						sendMessageToDevice(msgToDevice);
						showGCMStatusMessage(msgToDevice);

						bAudioFile = getFileName(dir, timeVar, placeValue, audioEnds);
						bAudioTimeFile = getFileName(dir, timeVar, placeValue, audioTimeEnds);
						bTimeSyncFile = getFileName(dir, timeVar, placeValue, timeSyncEnds);

						start.disabled = true;
						stop.disabled = false;
						
						startAudioRecordings(bAudioFile, bAudioTimeFile);
						startTimeSync(bTimeSyncFile);
						
					};


					/* stop buttion click event listener */
					stop.onclick = function() {
						console.log('stop clicked');
						
						sendMessageToDevice(GCMStop);
						showGCMStatusMessage(GCMStop);

						stopAudioRecording(GCMStop);
						stopTimeSync();
						stop.disabled = true;
						start.disabled = false;
						console.log('stop clicked');
				    };
				    function showGCMStatusMessage(msg){
				    	gcmField.innerHTML = "GCM Message: " + msg + " message SENT";
				    }
			});  
		</script>

	</body>

</html> 
