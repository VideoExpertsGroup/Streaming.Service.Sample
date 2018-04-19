<?php

function template_page($app){
	include("head.php");

	echo '<body>';
	if ($app->feedback) {
		echo $app->feedback . "<br/><br/>";
	}
               
	echo '<div class="container">';
	echo '<h2>Hello ' . $_SESSION['user_name'] . ', you are logged in.</h2>';
	echo '<a class="btn btn-secondary" href="?action=channels">Manage my channels.</a><br/><br/>';
	echo '<a class="btn btn-secondary" href="?action=public_channels">View all channels.</a><br/><br/>';
	echo '<a class="btn btn-secondary" href="' . $_SERVER['SCRIPT_NAME'] . '?action=logout">Log out</a>';
	echo '</div>';
	echo '</div>';
	echo '</body>';
	
}
