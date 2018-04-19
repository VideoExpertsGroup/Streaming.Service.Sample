<?php

/**
 * Class OneFileLoginApplication
 *
 * An entire php application with user registration, login and logout in one file.
 * Uses very modern password hashing via the PHP 5.5 password hashing functions.
 * This project includes a compatibility file to make these functions available in PHP 5.3.7+ and PHP 5.4+.
 *
 * @author Panique
 * @link https://github.com/panique/php-login-one-file/
 * @license http://opensource.org/licenses/MIT MIT License
 */
 
include_once("VXGStreamLandKey.php");
 
class OneFileLoginApplication
{
    /**
     * @var string Type of used database (currently only SQLite, but feel free to expand this with mysql etc)
     */
    private $db_type = "sqlite"; //

    /**
     * @var string Path of the database file (create this with _install.php)
     */
    private $db_sqlite_path = "./db/users.db";

    /**
     * @var object Database connection
     */
    private $db_connection = null;

    /**
     * @var bool Login status of user
     */
    private $user_is_logged_in = false;

	private $cloud_key = 'OHqaG9hsXxKtg4HL';
	private $cloud_key_password = 'OHqaG9hsXxKtg4HL';

	private $base_accp_url = 'https://cnvrclient2.videoexpertsgroup.com/api/';
	private $base_svcp_url = 'https://web.skyvr.videoexpertsgroup.com/api/';
	private $cloud_api_token = null;


    /**
     * @var string System messages, likes errors, notices, etc.
     */
    public $feedback = "";

	public function db_conn()
	{
		if($this->db_connection == null){
			$this->createDatabaseConnection();
		}
		return $this->db_connection;
	}

    /**
     * Does necessary checks for PHP version and PHP password compatibility library and runs the application
     */
    public function __construct()
    {
        if ($this->performMinimumRequirementsCheck()) {
            $this->runApplication();
        }
    }

    /**
     * Performs a check for minimum requirements to run this application.
     * Does not run the further application when PHP version is lower than 5.3.7
     * Does include the PHP password compatibility library when PHP version lower than 5.5.0
     * (this library adds the PHP 5.5 password hashing functions to older versions of PHP)
     * @return bool Success status of minimum requirements check, default is false
     */
    private function performMinimumRequirementsCheck()
    {
        if (version_compare(PHP_VERSION, '5.3.7', '<')) {
            echo "Sorry, Simple PHP Login does not run on a PHP version older than 5.3.7 !";
        } elseif (version_compare(PHP_VERSION, '5.5.0', '<')) {
            require_once("libraries/password_compatibility_library.php");
            return true;
        } elseif (version_compare(PHP_VERSION, '5.5.0', '>=')) {
            return true;
        }
        // default return
        return false;
    }

	private function read_json_input(){
		return json_decode(file_get_contents('php://input'), true);
	}

	private function response_json($arr)
	{
		header('Access-Control-Allow-Origin: *');
		header('Content-Type: application/json');
		echo json_encode($arr);
		exit;
	}
	
	private function response_json_error($code, $msg)
	{	
		http_response_code($code);
		$this->response_json(array(
			'code' => $code,
			'errorDetail' => $msg,
		));
		exit;
	}

    /**
     * This is basically the controller that handles the entire flow of the application.
     */
    public function runApplication()
    {
        // check is user wants to see register page (etc.)
        if (isset($_GET["action"]) && $_GET["action"] == "register") {
            $this->doRegistration();
            $this->showPageRegistration();
		} else if (isset($_GET["action"]) && $_GET["action"] == "login") {
			$this->doStartSession();
			$this->performUserLoginAction();
		} else if (isset($_GET["action"]) && $_GET["action"] == "channels") {
			$this->doStartSession();
			$this->performUserLoginAction();
			if ($this->getUserLoginStatus()) {
                $this->showPageChannels();
            } else {
                $this->showPageLoginForm();
            }
		} else if (isset($_GET["action"]) && $_GET["action"] == "new_channel") {
			$this->doStartSession();
			$this->performUserLoginAction();
			if ($this->getUserLoginStatus()) {
                $this->showPageNewChannel();
            } else {
                $this->showPageLoginForm();
            }
        } else if (isset($_GET["action"]) && $_GET["action"] == "new_channel_mobile") {
			$this->doStartSession();
			$this->performUserLoginAction();
			if ($this->getUserLoginStatus()) {
                $this->showPageNewChannelMobile();
            } else {
                $this->showPageLoginForm();
            }
		} else if (isset($_GET["action"]) && $_GET["action"] == "delete_channel") {
			$this->doStartSession();
			$this->performUserLoginAction();
			if ($this->getUserLoginStatus()) {
                $this->deleteChannel();
            } else {
                $this->showPageLoginForm();
            }
        } else if (isset($_GET["action"]) && $_GET["action"] == "public_channels") {
			$this->doStartSession();
            $this->publicChannels();
        } else if (isset($_GET["action"]) && $_GET["action"] == "play_channel") {
			$this->doStartSession();
            $this->playChannel();
        } else if (isset($_GET["action"]) && $_GET["action"] == "create_channel") {
			$this->doStartSession();
			$this->performUserLoginAction();
			if ($this->getUserLoginStatus()) {
                $this->createChannel();
            } else {
                $this->showPageLoginForm();
            }
        } else if (isset($_GET["action"]) && $_GET["action"] == "create_channel_mobile") {
			$this->doStartSession();
			$this->performUserLoginAction();
			if ($this->getUserLoginStatus()) {
                $this->createChannelMobile();
            } else {
                $this->showPageLoginForm();
            }
        } else {
            // start the session, always needed!
            $this->doStartSession();
            // check for possible user interactions (login with session/post data or logout)
            $this->performUserLoginAction();
            // show "page", according to user's login status
            if ($this->getUserLoginStatus()) {
                $this->showPageLoggedIn();
            } else {
                $this->showPageLoginForm();
            }
        }
    }

    /**
     * Creates a PDO database connection (in this case to a SQLite flat-file database)
     * @return bool Database creation success status, false by default
     */
    private function createDatabaseConnection()
    {
        try {
            $this->db_connection = new PDO($this->db_type . ':' . $this->db_sqlite_path);
            return true;
        } catch (PDOException $e) {
			if(!isset($_GET['json'])){
				$this->feedback = "PDO database connection problem: " . $e->getMessage();
			} else {
				$this->response_json_error(500, "PDO database connection problem: " . $e->getMessage());
			}
        } catch (Exception $e) {
			if(!isset($_GET['json'])){
				$this->feedback = "General problem: " . $e->getMessage();
			}else{
				$this->response_json_error(500, "General problem: " . $e->getMessage());
			}
        }
        return false;
    }

	private $json = null;

    /**
     * Handles the flow of the login/logout process. According to the circumstances, a logout, a login with session
     * data or a login with post data will be performed
     */
    private function performUserLoginAction()
    {
        if (isset($_GET["action"]) && $_GET["action"] == "logout") {
            $this->doLogout();
        } elseif (isset($_GET["action"]) && $_GET["action"] == "login" && isset($_GET["json"])) {
			$this->json = $this->read_json_input();
			if($this->json == null){
				$this->response_json_error(403, "Expected json on input");
			}
			$this->doLoginWithPostData();
        } elseif (!empty($_SESSION['user_name']) && ($_SESSION['user_is_logged_in'])) {
            $this->doLoginWithSessionData();
        } elseif (isset($_POST["login"])) {
            $this->doLoginWithPostData();
		}
    }

    /**
     * Simply starts the session.
     * It's cleaner to put this into a method than writing it directly into runApplication()
     */
    private function doStartSession()
    {
        if(session_status() == PHP_SESSION_NONE) session_start();
    }

    /**
     * Set a marker (NOTE: is this method necessary ?)
     */
    private function doLoginWithSessionData()
    {
        $this->user_is_logged_in = true; // ?
    }

    /**
     * Process flow of login with POST data
     */
    private function doLoginWithPostData()
    {
        if ($this->checkLoginFormDataNotEmpty()) {
            if ($this->createDatabaseConnection()) {
                $this->checkPasswordCorrectnessAndLogin();
            }
        }
    }

    /**
     * Logs the user out
     */
    private function doLogout()
    {
        $_SESSION = array();
        session_destroy();
        $this->user_is_logged_in = false;
        $this->feedback = "You were just logged out.";
    }

    /**
     * The registration flow
     * @return bool
     */
    private function doRegistration()
    {
        if ($this->checkRegistrationData()) {
            if ($this->createDatabaseConnection()) {
                $this->createNewUser();
            }
        }
        // default return
        return false;
    }

    /**
     * Validates the login form data, checks if username and password are provided
     * @return bool Login form data check success state
     */
    private function checkLoginFormDataNotEmpty()
    {
		if(isset($_GET["json"])){
			if($this->json == null){
				$this->response_json_error(403, "Expected json on input");
			}
			if(!empty($this->json['user_name']) && !empty($this->json['user_password'])){
				return true;
			}
			$this->response_json_error(403, "Not found parameter user_name and/or user_password");
			return false;
		}
		if (!empty($_POST['user_name']) && !empty($_POST['user_password'])) {
            return true;
        } elseif (empty($_POST['user_name'])) {
            $this->feedback = "Username field was empty.";
        } elseif (empty($_POST['user_password'])) {
            $this->feedback = "Password field was empty.";
        }
        // default return
        return false;
    }

    /**
     * Checks if user exits, if so: check if provided password matches the one in the database
     * @return bool User login success status
     */
    private function checkPasswordCorrectnessAndLogin()
    {
		$user_name = $this->json != null ? $this->json['user_name'] : $_POST['user_name'];
		$user_password = $this->json != null ? $this->json['user_password'] : $_POST['user_password'];
        // remember: the user can log in with username or email address
        $sql = 'SELECT user_name, user_email, user_password_hash
                FROM users
                WHERE user_name = :user_name OR user_email = :user_name
                LIMIT 1';
        $query = $this->db_connection->prepare($sql);
        $query->bindValue(':user_name', $user_name);
        $query->execute();

        // Btw that's the weird way to get num_rows in PDO with SQLite:
        // if (count($query->fetchAll(PDO::FETCH_NUM)) == 1) {
        // Holy! But that's how it is. $result->numRows() works with SQLite pure, but not with SQLite PDO.
        // This is so crappy, but that's how PDO works.
        // As there is no numRows() in SQLite/PDO (!!) we have to do it this way:
        // If you meet the inventor of PDO, punch him. Seriously.
        $result_row = $query->fetchObject();
        if ($result_row) {
            // using PHP 5.5's password_verify() function to check password
            if (password_verify($user_password, $result_row->user_password_hash)) {
                // write user data into PHP SESSION [a file on your server]
                $_SESSION['user_name'] = $result_row->user_name;
                $_SESSION['user_email'] = $result_row->user_email;
                $_SESSION['user_is_logged_in'] = true;
                $this->user_is_logged_in = true;
                if(isset($_GET['json'])){
					$this->response_json(array("result" => "success"));
					exit;
				}
                return true;
            } else {
				if(!isset($_GET['json'])){
					$this->feedback = "Wrong password.";
				}else{
					$this->response_json_error(401, "Wrong password.");
				}
            }
        } else {
			if(!isset($_GET['json'])){
				$this->feedback = "This user does not exist.";
			}else{
				$this->response_json_error(401, "This user does not exist.");
			}
        }
        if(!isset($_GET['json'])){
			$this->response_json_error(401, "Something wrong.");
		}
        // default return
        return false;
    }

    /**
     * Validates the user's registration input
     * @return bool Success status of user's registration data validation
     */
    private function checkRegistrationData()
    {
        // if no registration form submitted: exit the method
        if (!isset($_POST["register"])) {
            return false;
        }

        // validating the input
        if (!empty($_POST['user_name'])
            && strlen($_POST['user_name']) <= 64
            && strlen($_POST['user_name']) >= 2
            && preg_match('/^[a-z\d]{2,64}$/i', $_POST['user_name'])
            && !empty($_POST['user_email'])
            && strlen($_POST['user_email']) <= 64
            && filter_var($_POST['user_email'], FILTER_VALIDATE_EMAIL)
            && !empty($_POST['user_password_new'])
            && strlen($_POST['user_password_new']) >= 6
            && !empty($_POST['user_password_repeat'])
            && ($_POST['user_password_new'] === $_POST['user_password_repeat'])
        ) {
            // only this case return true, only this case is valid
            return true;
        } elseif (empty($_POST['user_name'])) {
            $this->feedback = "Empty Username";
        } elseif (empty($_POST['user_password_new']) || empty($_POST['user_password_repeat'])) {
            $this->feedback = "Empty Password";
        } elseif ($_POST['user_password_new'] !== $_POST['user_password_repeat']) {
            $this->feedback = "Password and password repeat are not the same";
        } elseif (strlen($_POST['user_password_new']) < 6) {
            $this->feedback = "Password has a minimum length of 6 characters";
        } elseif (strlen($_POST['user_name']) > 64 || strlen($_POST['user_name']) < 2) {
            $this->feedback = "Username cannot be shorter than 2 or longer than 64 characters";
        } elseif (!preg_match('/^[a-z\d]{2,64}$/i', $_POST['user_name'])) {
            $this->feedback = "Username does not fit the name scheme: only a-Z and numbers are allowed, 2 to 64 characters";
        } elseif (empty($_POST['user_email'])) {
            $this->feedback = "Email cannot be empty";
        } elseif (strlen($_POST['user_email']) > 64) {
            $this->feedback = "Email cannot be longer than 64 characters";
        } elseif (!filter_var($_POST['user_email'], FILTER_VALIDATE_EMAIL)) {
            $this->feedback = "Your email address is not in a valid email format";
        } else {
            $this->feedback = "An unknown error occurred.";
        }

        // default return
        return false;
    }

    /**
     * Creates a new user.
     * @return bool Success status of user registration
     */
    private function createNewUser()
    {
        // remove html code etc. from username and email
        $user_name = htmlentities($_POST['user_name'], ENT_QUOTES);
        $user_email = htmlentities($_POST['user_email'], ENT_QUOTES);
        $user_password = $_POST['user_password_new'];
        // crypt the user's password with the PHP 5.5's password_hash() function, results in a 60 char hash string.
        // the constant PASSWORD_DEFAULT comes from PHP 5.5 or the password_compatibility_library
        $user_password_hash = password_hash($user_password, PASSWORD_DEFAULT);

        $sql = 'SELECT * FROM users WHERE user_name = :user_name OR user_email = :user_email';
        $query = $this->db_connection->prepare($sql);
        $query->bindValue(':user_name', $user_name);
        $query->bindValue(':user_email', $user_email);
        $query->execute();

        // As there is no numRows() in SQLite/PDO (!!) we have to do it this way:
        // If you meet the inventor of PDO, punch him. Seriously.
        $result_row = $query->fetchObject();
        if ($result_row) {
            $this->feedback = "Sorry, that username / email is already taken. Please choose another one.";
        } else {
            $sql = 'INSERT INTO users (user_name, user_password_hash, user_email)
                    VALUES(:user_name, :user_password_hash, :user_email)';
            $query = $this->db_connection->prepare($sql);
            $query->bindValue(':user_name', $user_name);
            $query->bindValue(':user_password_hash', $user_password_hash);
            $query->bindValue(':user_email', $user_email);
            // PDO's execute() gives back TRUE when successful, FALSE when not
            // @link http://stackoverflow.com/q/1661863/1114320
            $registration_success_state = $query->execute();
			
			//$this->db_connection->errorInfo()
            if ($registration_success_state) {
                $this->feedback = "Your account has been created successfully. You can now log in.";
                return true;
            } else {
				var_dump(is_file($this->db_sqlite_path), $this->db_sqlite_path);
				
                $this->feedback = "Sorry, your registration failed. Please go back and try again. ".print_r($query->errorInfo(),true);
            }
        }
        // default return
        return false;
    }

    /**
     * Simply returns the current status of the user's login
     * @return bool User's login status
     */
    public function getUserLoginStatus()
    {
        return $this->user_is_logged_in;
    }

    /**
     * Simple demo-"page" that will be shown when the user is logged in.
     * In a real application you would probably include an html-template here, but for this extremely simple
     * demo the "echo" statements are totally okay.
     */
    private function showPageLoggedIn()
    {
		include("template_main_page.php");
		template_page($this);
		exit();
    }

    /**
     * Simple demo-"page" with the login form.
     * In a real application you would probably include an html-template here, but for this extremely simple
     * demo the "echo" statements are totally okay.
     */
    private function showPageLoginForm()
    {
		if(isset($_GET['json'])){
			$this->response_json_error(401, "Unauthorized");
		}
		include("head.php");
        if ($this->feedback) {
            echo $this->feedback . "<br/><br/>";
        }
		
        echo '<h2>Login</h2>';

        echo '<form method="post" action="' . $_SERVER['SCRIPT_NAME'] . '" name="loginform">';
        echo '<label for="login_input_username">Username (or email)</label> ';
        echo '<input id="login_input_username" type="text" name="user_name" required /> ';
        echo '<label for="login_input_password">Password</label> ';
        echo '<input id="login_input_password" type="password" name="user_password" required /> ';
        echo '<input type="submit"  name="login" value="Log in" />';
        echo '</form>';

        echo '<a href="' . $_SERVER['SCRIPT_NAME'] . '?action=register">Register new account</a>';
    }

    /**
     * Simple demo-"page" with the registration form.
     * In a real application you would probably include an html-template here, but for this extremely simple
     * demo the "echo" statements are totally okay.
     */
    private function showPageRegistration()
    {
		include("template_registration_page.php");
		
        if ($this->feedback) {
            echo $this->feedback . "<br/><br/>";
        }
        
        template_page($this);
    }
    
    private function showPageChannels()
    {
		if(isset($_GET["json"])){
			
			// find the loggined user
			$username = $_SESSION['user_name'];
			$sql = 'SELECT * FROM users WHERE user_name = ?';
			$query = $this->db_conn()->prepare($sql);
			$query->execute(array($username));
			$result_row = $query->fetchObject();
			if(!$result_row){
				$this->response_json_error(401, "Unathorized request");
			}
			$user_id = $result_row->user_id;

			$sql = 'SELECT * FROM cams WHERE owner = ?';
			$query = $this->db_conn()->prepare($sql);
			$query->execute(array($user_id));
			$i = 0;
			$channels = array();
			while($result_row = $query->fetchObject()){
				$type = $result_row->type;
				$type_name = "";
				if($type == 0){
					$type_name = "Mobile";
				}else if($type == 1){
					$type_name = "IP camera or NVR";
				}
				$cam = array(
					"cam_name" => $result_row->cam_name,
					"type" => $result_row->type,
					"type_name" => $type_name,
					"channel_id" => $result_row->channel_id,
					"cam_id" => $result_row->cam_id,
					"access_token_watch" => $result_row->token_watch,
				);

				if($type == 0){
					$cam["access_token_all"] = $result_row->token_all;
				}
				$channels[] = $cam;
				$i++;
			}
			
			$this->response_json(array(
				"meta" => array(
					"count" => $i
				),
				"objects" => $channels,
			));
		}
		
		include("template_channels_page.php");
		
        if ($this->feedback) {
            echo $this->feedback . "<br/><br/>";
        }
        
        template_page($this);
    }

    private function showPageNewChannel()
    {
		include("template_new_channel_page.php");
		
        if ($this->feedback) {
            echo $this->feedback . "<br/><br/>";
        }
        
        template_page($this);
    }
    
    private function showPageNewChannelMobile()
    {
		include("template_new_channel_mobile_page.php");
		
        if ($this->feedback) {
            echo $this->feedback . "<br/><br/>";
        }
        
        template_page($this);
    }
    
    private function getUserID(){
		$username = $_SESSION['user_name'];

		$sql = 'SELECT * FROM users WHERE user_name = ?';
		$query = $this->db_conn()->prepare($sql);
		$query->execute(array($username));
		$result_row = $query->fetchObject();
		// print_r($result_row);
		return $result_row->user_id;	
	}
    
    
    private function getCloudApiToken(){
		
		if(VXGStreamLandKey::$KEY == ''){
			echo "Please replace key in VXGStreamLandKey.php to your key";
			exit;
		}
		
		if($this->cloud_api_token == null){
			$curl = curl_init();
			$data_string = json_encode(array('cloud_token' => true, 'username' => VXGStreamLandKey::$KEY, 'password' => VXGStreamLandKey::$KEY));
			curl_setopt_array($curl, array(
				CURLOPT_URL => $this->base_accp_url.'v1/account/login/',
				CURLOPT_CUSTOMREQUEST => 'POST',
				CURLOPT_POST => true,
				CURLOPT_POSTFIELDS => $data_string,
				CURLOPT_HTTPHEADER => array('Content-Type: application/json', 'Content-Length: ' . strlen($data_string)),
				CURLOPT_RETURNTRANSFER => '1',
				CURLOPT_SSL_VERIFYPEER => false,
				CURLOPT_SSL_VERIFYHOST => false,
			));
			$curl_response = curl_exec($curl);
			//echo $curl_response;
			
			$apitoken_response = json_decode($curl_response, true);
			if (!isset($apitoken_response['cloud_token'])){
				exit("\ncloud_token not found in response\n");
			}

			if (!isset($apitoken_response['cloud_token']['token'])){
				print_r($apitoken_response);
				exit("\ncloud_token.token not found in response please try again after several seconds\n");
			}
    
			// TODO while with sleep
			
			$this->cloud_api_token = $apitoken_response['cloud_token']['token'];
		}
		return $this->cloud_api_token;
	}
	
	private function cloudCreateChannel($name, $url, $url_login, $url_pasword){

		$data = array(
			"name" => $name,
			"rec_mode" => "off", // TODO "on"
		);
		
		if($url != null){
			$data["source"] = array(
				"url" => $url,
				"login" => $url_login,
				"password" => $url_password,
			);
		}
		
		$data = json_encode($data);

		$curl = curl_init();
		curl_setopt_array($curl, array(
			CURLOPT_URL => $this->base_svcp_url . 'v3/channels/' ,
			CURLOPT_RETURNTRANSFER => true,
			CURLOPT_POST => true,
			CURLOPT_CUSTOMREQUEST => 'POST',
			CURLOPT_POSTFIELDS => $data,
			CURLOPT_HTTPHEADER => array('Content-Type: application/json', 'Authorization: SkyVR ' . $this->cloud_api_token),
			CURLOPT_SSL_VERIFYPEER => false,
			CURLOPT_SSL_VERIFYHOST => false,
		));

		$channel = json_decode(curl_exec($curl), true);
		curl_close($curl);
		return $channel;
	}
	
	private function cloudDeleteChannel($channel_id){
		$curl = curl_init();
		curl_setopt_array($curl, array(
			CURLOPT_URL => $this->base_svcp_url . 'v3/channels/'.$channel_id.'/',
			CURLOPT_RETURNTRANSFER => true,
			CURLOPT_CUSTOMREQUEST => 'DELETE',
			CURLOPT_HTTPHEADER => array('Authorization: SkyVR ' . $this->cloud_api_token),
			CURLOPT_SSL_VERIFYPEER => false,
			CURLOPT_SSL_VERIFYHOST => false,
		));

		$channel = json_decode(curl_exec($curl), true);
		curl_close($curl);
		return $channel;
	}
	
    private function createChannel(){
		$this->getCloudApiToken();
		$name = $_POST['name'];
		$url = $_POST['url'];
		$url_login = $_POST['url_login'];
		$url_password = $_POST['url_password'];
		$channel = $this->cloudCreateChannel($name, $url, $url_login, $url_password);
		if(isset($channel['errorDetail'])){
			include("template_new_channel_page.php");
			$this->feedback = $channel['errorDetail'];
			template_page($this);
			exit();
		}
		
		$user_id = $this->getUserID();

		$conn = $this->db_conn();
		$stmt = $conn->prepare('INSERT INTO cams(type, cam_name, owner, public, channel_id, token_all, token_watch) VALUES(?,?,?,?,?,?,?)');
		
		if(!$stmt->execute(array(1, $name ,$user_id, 1, $channel['id'], $channel['access_tokens']['all'], $channel['access_tokens']['watch']))){
			print_r($stmt->errorInfo());
		}else{
			header("Location: ?action=channels");
		}
		// print_r($channel);
	}
	
	private function createChannelMobile(){
		$this->getCloudApiToken();
		$name = $_POST['name'];
		$channel = $this->cloudCreateChannel($name, null, null, null);
		if(isset($channel['errorDetail'])){
			include("template_new_channel_mobile_page.php");
			$this->feedback = $channel['errorDetail'];
			template_page($this);
			exit();
		}
		
		$user_id = $this->getUserID();

		$conn = $this->db_conn();
		$stmt = $conn->prepare('INSERT INTO cams(type, owner, public, channel_id, token_all, token_watch) VALUES(?,?,?,?,?,?)');
		
		if(!$stmt->execute(array(0, $user_id, 1, $channel['id'], $channel['access_tokens']['all'], $channel['access_tokens']['watch']))){
			print_r($stmt->errorInfo());
		}else{
			header("Location: ?action=channels");
		}
		// print_r($channel);
	}
	
	private function deleteChannel(){
		
		$user_id = $this->getUserID();
		$cam_id = $_GET['cam_id'];
		

		$conn = $this->db_conn();
		$stmt = $conn->prepare('SELECT channel_id FROM cams WHERE cam_id = ? AND owner = ?');
		if(!$stmt->execute(array($cam_id, $user_id))){
			print_r($stmt->errorInfo());
			exit();
		}
		$channel_id = "";
		$result_row = $stmt->fetchObject();
		if($result_row){
			$channel_id = $result_row->channel_id;
		}
		
		if($channel_id != ""){
			$this->getCloudApiToken();
			$this->cloudDeleteChannel($channel_id);
		}
		$stmt = $conn->prepare('DELETE FROM cams WHERE cam_id = ? AND owner = ?');
		$stmt->execute(array($cam_id, $user_id));
		header("Location: ?action=channels");
	}
	
	private function publicChannels(){
		include("template_public_channels_page.php");
        
        template_page($this);		
	}
	
	private function playChannel(){
		include("template_play_channel_page.php");
        template_page($this);		
	}
	
	
	
}

// run the application
$application = new OneFileLoginApplication();
