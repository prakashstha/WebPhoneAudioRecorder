/**
* sends 'msg' to registered devices
*/
function sendMessageToDevice(msg){
	dataToSend = {
			message	: msg
	};
	
	$.ajax({
		type: "POST",
		url: "gcm/gcm_engine.php",
		data: dataToSend,
		success: function() {
			console.log("Sent: " + msg + " to registered device..");
		}
	});//end of .ajax({})
	
}
			