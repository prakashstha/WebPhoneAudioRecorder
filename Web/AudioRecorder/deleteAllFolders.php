
<!DOCTYPE HTML>
<html>
	<head>
		<title>Sign-In</title>
		<link rel="stylesheet" type="text/css" href="style.css">
		<!--Put the following in the <head>-->
		<script src="http://code.jquery.com/jquery-1.11.0.min.js"></script>
		<script type="text/javascript">
		
		
		$("document").ready(function(){
			$("body").on("keydown mousedown",function(event){ 
			var current_time = $.now();
			var server_time = $.now();
			var data = {
				"action": "syncTime",
				"client_time": current_time,
				"server_time": server_time
			};
			data = $(this).serialize() + "&" + $.param(data);
			$.ajax({
				type: "POST",
				dataType: "json",
				url: "calcTimeOffset.php", //Relative or absolute path to response.php file
				data: data,
				success: function(data) {
					var diffTime = data["server_time"] - data["client_time"];
					var RTT = $.now() - data["client_time"];
					$(".the-return").html(
					"Request Time : " + data["client_time"] + " ms<br />Server Time: " + data["server_time"] + " ms<br\>Response Time: "+ 
					$.now() +" ms<br/> Time difference : " + diffTime + " ms<br/>RTT: " + RTT + " ms"
					);
				}
			});
			return false;
			});
		});
		</script>
	</head>
	<body id="body-color">
	
	<?php
		//$dir = "files/15-09-13-17-22-56";
		//if ( !file_exists($dir) ) {
			//echo "Directory ".$dir." doesn't exist...";
		//	rmdir($dir);
		//	echo "Directory ".$dir." created...";
		//}
		
		
			$dir = "files";
		if ( file_exists($dir) ) {
			
			$dh  = opendir($dir);
			while (false !== ($filename = readdir($dh))) {
					$files[] = $filename;					
			}
		foreach($files as $f){
			if((strcasecmp($f, '.') == 0) || (strcasecmp($f, '..') == 0)){
				//do nothing
			}else{
				$dirName = $dir."/".$f;
				if(is_dir($dirName)){
					 foreach(glob("{$dirName}/*") as $file)
					{
						unlink($file);
					}
					rmdir($dirName);
					echo $dirName."-->Removed\n";
					
				}
				else{
					unlink($dirName);
				}
			}
		}

			//sort($files);

			//print_r($files);

			//rsort($files);

			//print_r($files);

			//echo "Directory ".$dir." doesn't exist...";
		//	rmdir($dir);
		//	echo "Directory ".$dir." created...";
		}


		?>
		<form action="return.php" class="js-ajax-php-json" method="post" accept-charset="utf-8">
		<input type="text" name="favorite_beverage" value="" placeholder="Favorite restaurant" />
		<input type="text" name="favorite_restaurant" value="" placeholder="Favorite beverage" />
		<select name="gender">
		<option value="male">Male</option>
		<option value="female">Female</option>
		</select>
		<input type="submit" name="submit" value="Submit form" />
		</form>
		
		<div class="the-return">
		[HTML is replaced when successful.]
		</div>
	</body>
</html> 
