
		/* UI field to show the time sync status messages*/
		var timeSyncField = document.getElementById('timeSynchronizer');
		/* Time sync status messages */
		var TimeSyncStartMessage = "Time Sync STARTED...";
		var TimeSyncStopMessage = "Time Sync STOPPED...";

		function displayMessage(message){
			timeSyncField.innerHTML = message;
		}
		var count = 0;
		var avgClient2ServerTripTime = 0;
		var avgClient2ServerTimeDiff = 0;
		var timeSynchMsgCounter = 0;
		var timeSyncThread;
		var bTimeSyncFile;
		/* server side file writer url*/
		var serverWriterFile = "FileWriter.php";
		/* indication for server that the received synchronization time data needs to be written in a file */
		var timeSyncAction = 'time_sync_info';
		/* indication for server that the received data are request for calculating the time diff between server and client
		and needs to response back with server local time*/
		var synchronizationAction = 'syncTime';

		/**
		* method to start time synchronization process 
		* for every startTimeSync() call variable are initialized to their default values.
		* this method will call doTimeSync() method periodically for every 1000ms i.e. 1second
		* @param fileName name of file to store all time sync information
		*/
		function startTimeSync(fileName){
			bTimeSyncFile = fileName;
			console.log(TimeSyncStartMessage);
			//$("#demo").html('Time Sync started');
			initializeVariables();
			timeSyncThread= setInterval(function(){doTimeSync();},1000);
			displayMessage(TimeSyncStartMessage);
		}


		/* method to stop time synchronization process */
		function stopTimeSync(){
			clearInterval(timeSyncThread);
			console.log(TimeSyncStopMessage);
			displayMessage(TimeSyncStopMessage);
		}

		/* method to initialize the variables required for synchronization process to zero */
		function initializeVariables(){
			count = 0;
			avgClient2ServerTripTime = 0;
			avgClient2ServerTimeDiff = 0;
			timeSynchMsgCounter = 0;
		}
		/* method gives the number of times doTimeSync() method has been called */
		function getCounter(){
			return timeSynchMsgCounter;
		}

		function getAvgTimeDiff(){
			return Math.round(avgClient2ServerTimeDiff);
		}

		function getAvgTripTime(){
			return Math.round(avgClient2ServerTripTime);
		}
		/**
		* 
		* @param reqTime client request time
		* @param serverTime server time when request arrives
		* @param respTime client time when response arrives
		* @param avgClient2ServerTripTime average round trip time from client-server-client
		* @param avgClient2ServerTimeDiff average time difference -> (client_time - server_time)
		*/
		function recordTimeSyncInfo(reqTime,serverTime,respTime,avgClient2ServerTripTime,avgClient2ServerTimeDiff){
			//console.log(bTimeSyncFile);
			dataToSend = {
						action	: timeSyncAction,
						request_time : reqTime,
						server_time : serverTime,
						response_time : respTime,
						avgTripTime : avgClient2ServerTripTime,
						avgTimeDiff : avgClient2ServerTimeDiff,
						fileName : bTimeSyncFile
				};
				
				$.ajax({
					type: "POST",
					url: serverWriterFile,
					data: dataToSend,
					success: function(resp_data) {
						console.log(resp_data);
						//$('#div2').html(resp_data);
					}
				});//end of .ajax({})
				
		}
		/**
		* method to compute the time offset of client with server
		* it sends its request_time to server
		* server responds back with its server_time
		* it records response_time
		* it does computation based on following basic concepts to compute the average time difference between server and client
		* oneWayTimeDelay = (response_time - request_time)/2
		* client_2_server_trip_time = response_time - request_time;
		* client_2_server_time_diff = (response_time - oneWayTimeDelay) - server_time)
		*/
	    function doTimeSync(){
			
			var request_time = $.now();
			var server_time = $.now();
			var data = {
				"action": synchronizationAction,
				"request_time": request_time,
				"server_time": server_time
			};
			data = $(this).serialize() + "&" + $.param(data);
			$.ajax({
				type: "POST",
				dataType: "json",
				url: "calcTimeOffset.php", //Relative or absolute path to response.php file
				async:false,
				data: data,
				success: function(response) {
					var reqTime = response["request_time"];
					var serverTime = response["server_time"];
					var respTime = $.now();
					var oneWayTimeDelay = (respTime - reqTime)/2;
					avgClient2ServerTripTime = ((avgClient2ServerTripTime * timeSynchMsgCounter) + oneWayTimeDelay)/(timeSynchMsgCounter + 1);
					avgClient2ServerTimeDiff = (((respTime - oneWayTimeDelay) - serverTime) + avgClient2ServerTimeDiff*timeSynchMsgCounter)/(timeSynchMsgCounter + 1);
					timeSynchMsgCounter += 1;
					recordTimeSyncInfo(reqTime,serverTime,respTime,avgClient2ServerTripTime,avgClient2ServerTimeDiff);
					
				}
			});
		}
