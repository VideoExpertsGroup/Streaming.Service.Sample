<?php

function template_page($app){
	include("head.php");
	echo '<body>';
	echo '<div class="container">';
	echo '<h2>Channels</h2>';
	echo '	
		<a class="btn btn-secondary" href="' . $_SERVER['SCRIPT_NAME'] . '">Homepage</a>
		<a class="btn btn-secondary" href="' . $_SERVER['SCRIPT_NAME'] . '?action=public_channels">Channels</a>
	';
			echo '<hr><div>';


	$cam_id = $_GET['cam_id'];
	$sql = 'SELECT * FROM cams WHERE cam_id = ? AND public = 1';
	$query = $app->db_conn()->prepare($sql);
	// echo 'query:['.$query.']';
	// $query->bindValue(':username', $username);
	// $query->bindValue(':user_email', $user_email);
	$query->execute(array($cam_id));
	if($result_row = $query->fetchObject()){
		
		echo '
		<script>
			$(document).ready(function () {
				window.player1 = new CloudPlayerSDK("player1");
				player1.setSource("'.$result_row->token_watch.'");
			});
		</script>
		<div style="width:100%; height: calc(100% - 200px)">
			<div id="player1" ></div>
		</div>
		';
		$i++;
	}else{
		echo "Not found channel";
	}
	echo '</div>';
	echo '</div>';
	echo '</body>';
	
}
