<?php

function template_page($app){
	include("head.php");
	echo '<body>';
	echo '<div class="container">';
	echo '<h2>Manage my channels</h2>';
	echo '	
			<a class="btn btn-secondary" href="' . $_SERVER['SCRIPT_NAME'] . '">Homepage</a>
			<a class="btn btn-secondary" href="' . $_SERVER['SCRIPT_NAME'] . '?action=new_channel">Add new channel (IP Camera or NVR)</a>
			<a class="btn btn-secondary" href="' . $_SERVER['SCRIPT_NAME'] . '?action=new_channel_mobile">Add new channel (Mobile Camera)</a>
	';
	echo '<hr><div>';
	$username = $_SESSION['user_name'];

	$sql = 'SELECT * FROM users WHERE user_name = ?';
	$query = $app->db_conn()->prepare($sql);
	$query->execute(array($username));
	$result_row = $query->fetchObject();
	// print_r($result_row);
	$user_id = $result_row->user_id;

	$sql = 'SELECT * FROM cams WHERE owner = ?';
	$query = $app->db_conn()->prepare($sql);
	// echo 'query:['.$query.']';
	// $query->bindValue(':username', $username);
	// $query->bindValue(':user_email', $user_email);
	$query->execute(array($user_id));
	$i = 0;
	while($result_row = $query->fetchObject()){
		//var_dump($result_row);
		$type = '';
		if($result_row->type == 0){
			$type = 'Mobile camera';
		}
		if($result_row->type == 1){
			$type = 'IP camera or NVR';
		}
		
		echo '
		<div class="card">
  <div class="card-body">
    <h5 class="card-title">'.$result_row->cam_name.'   #'.$result_row->channel_id.' ('.$type.')</h5>
    '.($result_row->type == 0 ? '<p class="card-text"><strong>Streaming:</strong> '.$result_row->token_all.'</p>' : '').'
    <p class="card-text"><strong>Playback:</strong> '.$result_row->token_watch.'</p>
    <a href="?action=delete_channel&cam_id='.$result_row->cam_id.'" class="btn btn-danger">Delete channel</a>
  </div>
</div><br>
		';
		
		$i++;
	}
	if($i == 0){
		echo "Not found channels";
	}
	echo '</div>';
	echo '</div>';
	echo '</body>';
	
}
