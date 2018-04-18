# TestServer

	- Based on php-login-one-file


## Debian/Ubuntu configure:

Install php sqlite module

	$ sudo apt install php-sqlite3
	
Change access:

	$ sudo chown www-data:www-data TestServer/db/
	$ sudo chown www-data:www-data TestServer/db/users.db


## Api for mobiles:


### Login

Send json structure:  { "user_name": "user1", "user_password": "your_secure_password" }

	Request POST: http://%yourserver%/TestServer/index.php?action=login&json

Read set-cookie header and remember in your application

Example request on jquery ajax

	$.ajax({
		type: "POST",
		url: "http://%yourserver%/TestServer/index.php?action=login&json",
		contentType: "application/json",
		dataType: "json",
		data: JSON.stringify({ "user_name": "user", "user_password": "123456" }),
	}).done(function(r){
		console.log("Success: ", r);
	}).fail(function(err){
		if(err.responseJSON){
			console.error("Fail " + err.responseJSON.code + " :" + err.responseJSON.errorDetail)
		}else{
			console.error("Fail: " + err.responseText)
		}
	});


### List of channels

	Request GET: http://%yourserver%/TestServer/index.php?action=channels&json

	Example response:
	
### VXGStreamLandKey

Please enter your key in VXGStreamLandKey.php 
