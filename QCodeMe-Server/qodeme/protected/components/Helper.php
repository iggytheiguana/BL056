<?php

class Helper {

    public static function random_string($length = 32)
    {
        $chars = array_merge(range(0,9), range('a','z'), range('A','Z'));
        shuffle($chars);
        $string = implode(array_slice($chars, 0, $length));
        return $string;
    }

    public static function generate_qrcode_string($password)
    {
        $qrcode = ( rand(1,1000) . $password. microtime());
        return $qrcode;
    }

    public static function generate_token($data)
    {
        $token = sha1($data . microtime());

        return $token;
    }

    public static function sent_individual_push( $type, $user_id, $qr = null,$contact_id=null)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
        $contact = Contact::getContactObj($user_id,$contact_id);
        $registrationIDs[] = User::getToken($user_id);
        $fields = array(
              'registration_ids'  => $registrationIDs,
              'data' => array(
                  "type" =>$type ,
                  "contact_id" => $contact['id'], /* поменял местами */
            ),
        );

        Helper::curlUnit($url,$apiKey,$fields);

    }

    public static function sent_message_push( $type, $user_id, $message_id)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';

        $registrationIDs[]=User::getToken($user_id);
        $fields = array(
            'registration_ids'  => $registrationIDs,
            'data' => array(
                "type" =>$type ,
                'user_id' =>$user_id,
                'message_id' =>$message_id,
            ),
        );


     Helper::curlUnit($url,$apiKey,$fields);

    }

    public static function curlUnit($url,$apiKey,$fields){
        $headers = array(
            'Authorization: key=' . $apiKey,
            'Content-Type: application/json'
        );

        // Open connection
        $ch = curl_init();

        // Set the url, number of POST vars, POST data
        curl_setopt( $ch, CURLOPT_URL, $url );

        curl_setopt( $ch, CURLOPT_POST, true );
        curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );

        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);

        curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode( $fields ) );

        // Execute post
       $result = curl_exec($ch);
       $info = curl_getinfo ($ch) ;
        if($info["http_code"] == 200)
        {
            $fp = fopen('log.txt', 'a');
            fwrite($fp, implode(",", $fields['registration_ids']).' '.date("Y-m-d H:i:s").'\r\n' );
            fclose($fp);
        } else {
            $fp = fopen('errorlog.txt', 'a');
            fwrite($fp, implode(",", $fields['registration_ids']).' '.date("Y-m-d H:i:s").' '.$info["http_code"].'\r\n' );
            fclose($fp);
        }

        // Close connection
        curl_close($ch);
        return $result;
    }

    public static function sent_push( $type, $chat_id, $user_id, $message = null,  $qr = null, $contact_id=null)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        //take push_tokens
        /*
         SELECT u.push_token
            FROM `user` u JOIN `chat_member` cm ON cm.user_id=u.id
            WHERE cm.chat_id=1 AND u.push_token is NOT null

         *  */
        $criteria = new CDbCriteria();
        $criteria->alias='u';
        $criteria->select = 'u.push_token';
        $criteria->condition = 'cm.chat_id=:chat_id AND u.push_token is NOT null AND u.id <>'.$user_id;
        $criteria->join = 'JOIN `chat_member` cm ON cm.user_id=u.id';
        $criteria->params = array(':chat_id' => $chat_id);
        $tokens = User::model()->findAll($criteria);

        $registrationIDs = array();
        foreach ($tokens as $t)
        {
            $registrationIDs[] = $t->push_token;
        }

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';

        if($message)
        {
            $user = User::model()->findByPk($user_id);
            $qrcode = $user->qrcode;

            $fields = array(
                'registration_ids'  => $registrationIDs,
                'data'              => array(
                    "type" =>$type ,
                    "id" => (int)$message->getPrimaryKey(),
                    "message" => $message->text,
                    "created" => date( "Y-m-d H:i:s"),
                    "from_qrcode" => $qrcode,
                    "chat_id" =>(int)$chat_id
                ),
            );
        }
        else
        {


        $public_name = Contact::getContactObj($user_id,$contact_id);
        $fields = array(
        'registration_ids'  => $registrationIDs,
        'data'              => array(
            "type" =>$type,
            "chat_id" =>$chat_id,
            "created" => date( "Y-m-d H:i:s"),
            "from_qrcode" => $qr,
            "contactObj" =>  Contact::getContactObj($contact_id,$user_id),
            "public_name" => $public_name['public_name'],
        ),
    );
    }

        $headers = array(
            'Authorization: key=' . $apiKey,
            'Content-Type: application/json'
        );

        // Open connection
        $ch = curl_init();

        // Set the url, number of POST vars, POST data
        curl_setopt( $ch, CURLOPT_URL, $url );

        curl_setopt( $ch, CURLOPT_POST, true );
        curl_setopt( $ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );

        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);

        curl_setopt( $ch, CURLOPT_POSTFIELDS, json_encode( $fields ) );

        // Execute post
        $result = curl_exec($ch);
        $er = curl_errno($ch);
        $info = curl_getinfo ($ch) ;
        if($info["http_code"] == 200)
            {
            $fp = fopen('log.txt', 'a');
            fwrite($fp,implode(",", $registrationIDs) .' '.date("Y-m-d H:i:s").'\r\n' );
            fclose($fp);
            } else {
            $fp = fopen('errorlog.txt', 'a');
            fwrite($fp,implode(",", $registrationIDs).' '.date("Y-m-d H:i:s").' '.$info["http_code"].' curl_er'.$er .'\r\n' );
            fclose($fp);
        }

        // Close connection
        curl_close($ch);

       return $result;
    }



}



