<?php

function template_page($app){
	include("head.php");
	echo '<body>';
	echo '<div class="container">';
	echo '<h2>Channels</h2>';
	echo '	
		<a class="btn btn-secondary" href="' . $_SERVER['SCRIPT_NAME'] . '">Homepage</a>
	';
			echo '<hr><div>';

	$sql = 'SELECT * FROM cams WHERE public = ?';
	$query = $app->db_conn()->prepare($sql);
	// echo 'query:['.$query.']';
	// $query->bindValue(':username', $username);
	// $query->bindValue(':user_email', $user_email);
	$query->execute(array(1));
	$i = 0;
	while($result_row = $query->fetchObject()){
		//var_dump($result_row);
		echo '
		<div class="card">
  <div class="card-body">
    <h5 class="card-title">Channel "'.$result_row->cam_name.'"  #'.$result_row->channel_id.'</h5>
    <p class="card-text">'.$result_row->token_watch.'</p>
    <a href="?action=play_channel&cam_id='.$result_row->cam_id.'" class="btn btn-danger">Open</a>
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
