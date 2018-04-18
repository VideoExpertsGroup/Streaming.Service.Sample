<?php

function template_page($app){
	include("head.php");
	echo '<body>';
	echo '<div class="container">';
	echo '<h2>New channel</h2>';
	echo '<a class="btn btn-secondary" href="' . $_SERVER['SCRIPT_NAME'] . '?action=channels">My channels</a>';
			echo '<hr>';
	$username = $_SESSION['user_name'];
	if($app->feedback){
		echo '<font color="red">'.$app->feedback.'</font>';
	}
	$name = isset($_POST['name']) ? $_POST['name'] : '';
	$url = isset($_POST['url']) ? $_POST['url'] : '';
	$url_login = isset($_POST['url_login']) ? $_POST['url_login'] : '';
	$url_password = isset($_POST['url_password']) ? $_POST['url_password'] : '';
	echo '
	<form action="?action=create_channel_mobile" method="POST">
	<div class="form-group">
    <label for="name1">Name</label>
    <input name="name" type="text" class="form-control" value="'.$name.'" id="name1" placeholder="Enter name">
  </div>
  <button type="submit" name="create_channel_mobile" class="btn btn-primary">Create</button>
</form>
';
	echo '</div>';
	echo '</body>';
}
