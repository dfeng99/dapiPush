<?php
use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

require '../vendor/autoload.php';
require 'configurations.php';

function newBzToken($seed){
    return sha1($seed.HOICOI_TOKEN);
}

spl_autoload_register(function ($classname) {
    require ("../classes/" . $classname . ".php");
});

$config['displayErrorDetails'] = true;
$config['addContentLengthHeader'] = false;

$config['db']['host']   = "localhost";
$config['db']['user']   = MgrDbUser;
$config['db']['pass']   = MgrDbPwd;
$config['db']['dbname'] = "DapiPush";

$app = new \Slim\App(["settings" => $config]);

//Containers
$container = $app->getContainer();
	//Views
	$container['view'] = new \Slim\Views\PhpRenderer("../templates/");
	//javascripts
	$container['javascript'] = new \Slim\Views\PhpRenderer("../templates/js");
	//Logger
	$container['logger'] = function($c) {
    		$logger = new \Monolog\Logger('my_logger');
		$file_handler = new \Monolog\Handler\StreamHandler("../logs/app.log");
    		$logger->pushHandler($file_handler);
    		return $logger;
	};
	//DB
	$container['db'] = function ($c) {
    		$db = $c['settings']['db'];
    		$pdo = new PDO("mysql:host=" . $db['host'] . ";dbname=" . $db['dbname'],
        	$db['user'], $db['pass']);
    		$pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    		$pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    		return $pdo;
	};

	// *** IMPORTANT
	// *** get callees, Developers should provide your own way to collect data for $container['callees']
	// *** example below, we store receivers token in an independent server, and requests them from a http query string
	$container['callees'] = function($c) {
		// get all callees' token
		$userData=array(
		        'username' => GWUser, // tokens server login information
		        'password' => GWPWD,
		        'invitees' => '*'
		);
		$postdata = http_build_query($userData);
		$opts = array('http' =>
    				array(
			        'method'  => 'POST',
			        'header'  => 'Content-type: application/x-www-form-urlencoded',
			        'content' => $postdata
				)
			);
		$context = stream_context_create($opts);
		$callees = file_get_contents(MgrHost.Query4Callees."&bzToken=".newBzToken($userData['password']),false,$context);
		$toWhom = json_decode($callees);
		return $toWhom;
	};

//Routes
$app->get('/', function (Request $request, Response $response) {
	$this->logger->addInfo("Load login form");
	if($this->db == null){
		$dbStatus=0;
		$this->logger->addInfo("database connection failed!");
	} else {
		$dbStatus=1;
	}
	$response = $this->view->render($response, "login.phtml",["dbStatus"=>$dbStatus]);

    	return $response;
});

$app->get('/logout', function (Request $request, Response $response) {
	$auth = new loginAuth($this->db);
	$auth->logout();
});

$app->post('/loginAuth', function (Request $request, Response $response) {
	  $data = $request->getParsedBody();
	  $loginData = [];
    	  $loginData['username']=filter_var($data['uname'],FILTER_SANITIZE_STRING);
    	  $loginData['password']=filter_var($data['pwd'],FILTER_SANITIZE_STRING);

	  $this->logger->addInfo($loginData['username']." is tring to login");
	  $auth = new loginAuth($this->db);

	  if( ($user=$auth->login($loginData)) ){
	    $response = $this->view->render($response, "dapipush.phtml", ["callees"=>$this->callees]);
	  } else {
	    $this->logger->addInfo($loginData['username']." failed to login");
	    $response = $this->view->render($response, "loginFailed.phtml", ["username"=>$user]);
	  }
    	return $response;
});

$app->get('/js/{name}', function (Request $request, Response $response, $args) {
	$response = $this->javascript->render($response, $args['name']);
    	return $response;
});
// Form process here
$app->post('/dapiPush', function (Request $request, Response $response) {
 	session_start();
	$data = $request->getParsedBody();

	$msg="";
	$payload= new Payload();
	$callees= array();
	$callee = new stdClass();

	foreach( $data['toWhom'] as $toWhom ){
		$tokens = json_decode( urldecode($toWhom) );
		$data['apns_badge'] = $tokens->apns_badge;
		$data['fcm_badge'] = $tokens->fcm_badge;
		if( ($tokens->apns && ($result = $payload->create_apns_payload($data)) ===true) || ($tokens->fcm && ($result =$payload->create_fcm_payload($data) )=== true)){
			$callee->apns_token = $tokens->apns;
			$callee->fcm_token = $tokens->fcm;
			$callee->payload = $payload->obj_apns;
			$callees[] = $callee;
			$callee = new stdClass();
		} else {
	  		$this->logger->addInfo("Failed to create payload:".($result > 0) ? $result : $toWhom);
		}
	}

	if( sizeOf($callees) > 0 ){
	  $this->logger->addInfo("Sending message to:".$data['server'].json_encode($callees));
	  $msg = $payload->send('195', newBzToken('dapiPush'), $callees, $data['server']); // send message
	  $this->logger->addInfo("Result:".$msg);
	} else {
	  $msg = "noCallee";
	}

        $response = $this->view->render($response, "dapipush.phtml", ["callees"=>$this->callees]);

    	return $response;
});
$app->run();

?>
