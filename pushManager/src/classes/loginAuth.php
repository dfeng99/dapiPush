<?php
class loginAuth
{
	private  $logindb;
	private  $userName;

	function __construct($db) {
   		session_start();
		$this->logindb=$db;
	}

	public function login($loginData){
		$this->userName= $loginData['username'];
		$password= $loginData['password'];
		$sql = "SELECT username from users where username='".$this->userName."' AND password='".sha1($loginData['password'])."'";			
		$stmt = $this->logindb->query($sql);
		$user = $stmt->fetch();
		if($user){
		  $_SESSION['valid'] = true;
                  $_SESSION['starttime'] = time();
                  $_SESSION['username'] = $this->userName;
		  $_SESSION['expire'] = $_SESSION['starttime'] + (60 * 60);
			return $user;
		} else {
			return false;
		}
	}

	public function logout(){
		session_start();
   		unset($_SESSION["username"]);
   
		header("refresh:1;url=/");
   		echo 'You have cleaned session';
	}

}
