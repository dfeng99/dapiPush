<?php
class Payload
{
	public $obj_apns;
	public $json_apns;
	public $obj_fcm;
	public $json_fcm;

	function __construct(){
        }
	
	public function create_apns_payload($dataArray){
		if( empty($dataArray['title']) && empty($dataArray['body']) ){
			return false;
		}
                $alert = new stdClass();
                $alert->title = $dataArray['title'];
                $alert->body  = $dataArray['body'];
                if( isset($dataArray['action-loc-key']) && $dataArray['action-loc-key'] != null ){
                	$alert->{'action-loc-key'} = $dataArray['action-loc-key'];;
		}
                if( isset($dataArray['loc-key']) && $dataArray['loc-key'] != null ){
                	$alert->{'loc-key'} = $dataArray['loc-key'];;
		}
                if( isset($dataArray['loc-arg']) && $dataArray['loc-arg'] != null ){
                	$alert->{'loc-arg'} = $dataArray['loc-arg'];;
		}
        
                $aps = new stdClass();
                $aps->sound = "default";
                $aps->alert = $alert;
                if( isset($dataArray['badge']) && $dataArray['badge'] != null ){
                        $aps->badge = $dataArray['badge'];
                }       
                if( isset($dataArray['sound']) && $dataArray['sound'] != null ){
                        $aps->sound = $dataArray['sound'];
                }       

                $this->obj_apns = new stdClass();
                $this->obj_apns->aps = $aps;
		foreach($dataArray as $k => $v){
                	if( strpos($key,'acme') == 0){
                       	 $this->obj_apns->$key = $v;
                	}       
		}
                $this->json_apns = json_encode($this->obj_apns);

                return true;
        }       
        
        public function create_fcm_payload($dataArray){
                return false;
        }

        public function send($user_id,$bzToken, $persons){
		$callees = base64_encode(json_encode($persons));
                $notification = array(
                        'dapiToken' => $bzToken, 
                        'user_id' => $user_id,
                        'invitations' => $callees
                );
                $postdata = http_build_query($notification);
                $opts = array('http' =>
                                array(
                                'method'  => 'POST',
                                'header'  => 'Content-type: application/x-www-form-urlencoded',
                                'content' => $postdata
                                )
                        );      
                $context  = stream_context_create($opts);
                $query = "https://push.bzcentre.com/pushTo/";
		$resp = file_get_contents($query,false,$context);
		return empty($resp) ? $postdata : "Failed";
        }
}       
?>

