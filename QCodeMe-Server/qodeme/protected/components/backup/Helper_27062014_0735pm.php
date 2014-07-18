<?php

class Helper
{

    public static function random_string($length = 32)
    {
        $chars = array_merge(range(0, 9), range('a', 'z'), range('A', 'Z'));
        shuffle($chars);
        $string = implode(array_slice($chars, 0, $length));
        return $string;
    }

    public static function generate_qrcode_string($password)
    {
        $qrcode = ( rand(1, 1000) . $password . microtime());
        return $qrcode;
    }

    public static function generate_token($data)
    {
        $token = sha1($data . microtime());

        return $token;
    }

    public static function sent_individual_push($type, $user_id, $qr = null, $contact_id = null)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
        $contact = Contact::getContactObj($user_id, $contact_id);
        $registrationIDs[] = User::getToken($user_id);
        $fields = array(
            'registration_ids' => $registrationIDs,
            'data' => array(
                "type" => $type,
                "contact_id" => $contact['id'], /* поменял местами */
            ),
        );

        Helper::curlUnit($url, $apiKey, $fields);
    }

    public static function sent_message_push($type, $user_id, $message_id)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';

        $registrationIDs[] = User::getToken($user_id);
        $fields = array(
            'registration_ids' => $registrationIDs,
            'data' => array(
                "type" => $type,
                'user_id' => $user_id,
                'message_id' => $message_id,
            ),
        );


        Helper::curlUnit($url, $apiKey, $fields);
    }

    public static function curlUnit($url, $apiKey, $fields)
    {
        $headers = array(
            'Authorization: key=' . $apiKey,
            'Content-Type: application/json'
        );

        // Open connection
        $ch = curl_init();

        // Set the url, number of POST vars, POST data
        curl_setopt($ch, CURLOPT_URL, $url);

        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);

        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

        // Execute post
        $result = curl_exec($ch);
        $info = curl_getinfo($ch);
        if ($info["http_code"] == 200)
        {
            $fp = fopen('log.txt', 'a');
            fwrite($fp, implode(",", $fields['registration_ids']) . ' ' . date("Y-m-d H:i:s") . '\r\n');
            fclose($fp);
        }
        else
        {
            $fp = fopen('errorlog.txt', 'a');
            fwrite($fp, implode(",", $fields['registration_ids']) . ' ' . date("Y-m-d H:i:s") . ' ' . $info["http_code"] . '\r\n');
            fclose($fp);
        }

        // Close connection
        curl_close($ch);
        return $result;
    }

    public static function sent_push($type, $chat_id, $user_id, $message = null, $qr = null, $contact_id = null)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        //take push_tokens
        /*
          SELECT u.push_token
          FROM `user` u JOIN `chat_member` cm ON cm.user_id=u.id
          WHERE cm.chat_id=1 AND u.push_token is NOT null

         *  */
        $criteria = new CDbCriteria();
        $criteria->alias = 'u';
        $criteria->select = 'u.push_token';
        $criteria->condition = 'cm.chat_id=:chat_id AND u.push_token is NOT null AND u.id <>' . $user_id;
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

        if ($message)
        {
            $searchData = Yii::app()->db->createCommand()
            ->selectDistinct('u.push_token')
            ->from('search s')
            ->leftJoin('user u', 's.user_id = u.id')
            ->where('chat_id=:chat_id', array(':chat_id'=>$chat_id))
            ->queryAll();

            $tokenArray = array();
            if(!empty($searchData))
            {
                foreach($searchData as $search)
                {
                    $tokenArray[] = $search['push_token'];
                }
            }

            $registrationIDs = array_unique(array_merge($registrationIDs,$tokenArray));
        
            $user = User::model()->findByPk($user_id);
            $qrcode = $user->qrcode;

            $fields = array(
                'registration_ids' => $registrationIDs,
                'data' => array(
                    "type" => $type,
                    "id" => (int) $message->getPrimaryKey(),
                    "message" => $message->text,
                    "user_id" => $message->user_id,
                    "photourl" => $message->photourl,
                    "has_photo" => $message->has_photo,
                    "replyto_id" => $message->replyto_id,
                    "is_flagged" => $message->is_flagged,
                    "latitude" => $message->latitude,
                    "longitude" => $message->longitude,
                    "sendername" => $message->sendername,
                    "created" => $message->created,
                    //"created" => date("Y-m-d H:i:s"),
                    "from_qrcode" => $qrcode,
                    "chat_id" => (int) $chat_id
                ),
            );
        }
        else
        {


            $public_name = Contact::getContactObj($user_id, $contact_id);
            $fields = array(
                'registration_ids' => $registrationIDs,
                'data' => array(
                    "type" => $type,
                    "chat_id" => $chat_id,
                    "created" => date("Y-m-d H:i:s"),
                    "from_qrcode" => $qr,
                    "contactObj" => Contact::getContactObj($contact_id, $user_id),
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
        curl_setopt($ch, CURLOPT_URL, $url);

        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);

        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

        // Execute post
        $result = curl_exec($ch);
        $er = curl_errno($ch);
        $info = curl_getinfo($ch);
        if ($info["http_code"] == 200)
        {
            $fp = fopen('log.txt', 'a');
            fwrite($fp, implode(",", $registrationIDs) . ' ' . date("Y-m-d H:i:s") . '\r\n');
            fclose($fp);
        }
        else
        {
            $fp = fopen('errorlog.txt', 'a');
            fwrite($fp, implode(",", $registrationIDs) . ' ' . date("Y-m-d H:i:s") . ' ' . $info["http_code"] . ' curl_er' . $er . '\r\n');
            fclose($fp);
        }

        // Close connection
        curl_close($ch);

        return $result;
    }

//    public static function set_chat_push($type, $userId, $chat_id, $tags)
//    {
//        $apiKey = Yii::app()->params['googleApiKey'];
//
//        // Set POST variables
//        $url = 'https://android.googleapis.com/gcm/send';
//        
//        $chatArray = Yii::app()->db->createCommand()
//        ->select('*')
//        ->from('chat')
//        ->where('id=:id', array(':id'=>$chat_id))
//        ->queryRow();
//        
//        $userIdString = explode(',',$userId);
//        
//        $fp = fopen('log.txt', 'a');
//        fwrite($fp, 'User IDs : '.$userIdString.'\r\n' );
//        fclose($fp);
//        
//        $dbCommand = Yii::app()->db->createCommand(
//                "SELECT push_token FROM `user` WHERE id IN (".$userIdString.");");
//        $resultArray = $dbCommand->queryAll();
//
//        $registrationIDs = array();
//        foreach($resultArray as $result)
//        {
//            $registrationIDs[] = $result['push_token'];
//        }
//        $fp = fopen('log.txt', 'a');
//        fwrite($fp, 'Registration IDs : '.print_r($registrationIDs,true).'\r\n' );
//        fclose($fp);
//        //$registrationIDs[] = User::getToken($userId);   
//
//        $fields = array(
//            'registration_ids' => $registrationIDs,
//            'data' => array(
//                "type" => $type,
//                'id' => $chat_id,
//                //'tags' => $chatArray['tags'],
//                'tags' => $tags,
//                'latitude' => $chatArray['latitude'],
//                'longitude' => $chatArray['longitude'],
//                'number_of_likes' => $chatArray['number_of_likes'],
//                'number_of_dislikes' => $chatArray['number_of_dislikes'],
//                'description' => $chatArray['description'],
//                'is_locked' => $chatArray['is_locked'],
//                'number_of_flagged' => $chatArray['number_of_flagged'],
//                'status' => $chatArray['status'],
//                'number_of_members' => $chatArray['number_of_members'],
//                'chat_title' => $chatArray['title'],
//            ),
//        );
//
//        Helper::curlUnit($url, $apiKey, $fields);
//    }
    
    public static function set_chat_push($type, $user_id, $chat_id, $tags, $status, $chat_color)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
        
        $chatArray = Yii::app()->db->createCommand()
        ->select('*')
        ->from('chat')
        ->where('id=:id', array(':id'=>$chat_id))
        ->queryRow();
        
        $criteria = new CDbCriteria();
        $criteria->alias = 'u';
        $criteria->select = 'u.push_token';
        $criteria->condition = 'cm.chat_id=:chat_id AND u.push_token is NOT null AND u.id <>' . $user_id;
        $criteria->join = 'JOIN `chat_member` cm ON cm.user_id=u.id';
        $criteria->params = array(':chat_id' => $chat_id);
        $tokens = User::model()->findAll($criteria);

        $registrationIDs = array();
        foreach ($tokens as $t)
        {
            $registrationIDs[] = $t->push_token;
        }
        
        $fields = array(
            'registration_ids' => $registrationIDs,
            'data' => array(
                "type" => $type,
                'id' => $chat_id,
                //'tags' => $chatArray['tags'],
                'tags' => $tags,
                'chat_color' => $chat_color,
                'latitude' => $chatArray['latitude'],
                'longitude' => $chatArray['longitude'],
                'number_of_likes' => $chatArray['number_of_likes'],
                'number_of_dislikes' => $chatArray['number_of_dislikes'],
                'description' => $chatArray['description'],
                'is_locked' => $chatArray['is_locked'],
                'number_of_flagged' => $chatArray['number_of_flagged'],
                'chat_status' => $chatArray['status'],
                'status' => $status,
                'number_of_members' => $chatArray['number_of_members'],
                'chat_title' => $chatArray['title'],
            ),
        );

        Helper::curlUnit($url, $apiKey, $fields);
    }

    public static function set_favorite_push($type, $user_id, $chat_id)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
        
        $criteria = new CDbCriteria();
        $criteria->alias = 'u';
        $criteria->select = 'u.push_token';
        $criteria->condition = 'cm.chat_id=:chat_id AND u.push_token is NOT null AND u.id <>' . $user_id;
        $criteria->join = 'JOIN `chat_member` cm ON cm.user_id=u.id';
        $criteria->params = array(':chat_id' => $chat_id);
        $tokens = User::model()->findAll($criteria);

        $registrationIDs = array();
        foreach ($tokens as $t)
        {
            $registrationIDs[] = $t->push_token;
        }
        
        $numberOfFavorites = Yii::app()->db->createCommand()
        ->select('COUNT(*) as count')
        ->from('favorite')
        ->where('chat_id=:chat_id', array(':chat_id'=>$chat_id))
        ->queryRow();
        
//        foreach($userIds as $userId)
//        {
//            $registrationIDs[] = User::getToken($userId['user_id']);   
            
            $fields = array(
                'registration_ids' => $registrationIDs,
                'data' => array(
                    "type" => $type,
                    'chat_id' => $chat_id,
                    'number_of_favorites' => $numberOfFavorites['count'],
                ),
            );
            
            Helper::curlUnit($url, $apiKey, $fields);
//        }
    }

    public static function set_flagged_push($type, $userIds, $messageID, $numberOfFlagged,$isFlagged)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
        
        foreach($userIds as $userId)
        {
            $registrationIDs[] = User::getToken($userId['user_id']);   
            
            $fields = array(
                'registration_ids' => $registrationIDs,
                'data' => array(
                    "type" => $type,
                    'message_id' => $messageID,
                    'number_of_flagged' => $numberOfFlagged,
                    'is_flagged' => $isFlagged,
                ),
            );
            
            Helper::curlUnit($url, $apiKey, $fields);
        }
    }

    public static function chat_add_member_push($type, $userIds, $chatID, $numberOfMembers, $ChatType, $tagString)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';

        $registrationIDs[] = User::getToken($userIds);  
        
        $chatArray = Yii::app()->db->createCommand()
        ->select('*')
        ->from('chat')
        ->where('id=:id', array(':id'=>$chatID))
        ->queryRow();
        
        $chatMemberData = Yii::app()->db->createCommand()
        ->select('status')
        ->from('chat_member')
        ->where('chat_id=:chat_id and user_id=:user_id', array(':chat_id'=>$chatID, ':user_id'=>$userIds))
        ->queryRow();

        $fields = array(
            'registration_ids' => $registrationIDs,
            'data' => array(
                "type" => $type,
                'chat_id' => $chatID,
                'number_of_members' => $numberOfMembers,
                'chat_type' => $ChatType,
                'latitude' => $chatArray['latitude'],
                'longitude' => $chatArray['longitude'],
                'number_of_likes' => $chatArray['number_of_likes'],
                'number_of_dislikes' => $chatArray['number_of_dislikes'],
                'description' => $chatArray['description'],
                'is_locked' => $chatArray['is_locked'],
                'number_of_flagged' => $chatArray['number_of_flagged'],
                'chat_status' => $chatArray['status'],
                'chat_title' => $chatArray['title'],
                'created' => $chatArray['created'],
                //'number_of_members' => $chatArray['number_of_members'],
                'status' => $chatMemberData['status'],
                'tags' => $tagString,
            ),
        );

        Helper::curlUnit($url, $apiKey, $fields);
    }

    public static function delete_message_push($type, $user_id, $chat_id, $message_id)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
        
        $criteria = new CDbCriteria();
        $criteria->alias = 'u';
        $criteria->select = 'u.push_token';
        $criteria->condition = 'cm.chat_id=:chat_id AND u.push_token is NOT null';
        $criteria->join = 'JOIN `chat_member` cm ON cm.user_id=u.id';
        $criteria->params = array(':chat_id' => $chat_id);
        $tokens = User::model()->findAll($criteria);

        $registrationIDs = array();
        foreach ($tokens as $t)
        {
            $registrationIDs[] = $t->push_token;
        }
        
        $fields = array(
            'registration_ids' => $registrationIDs,
            'data' => array(
                "type" => $type,
                'message_id' => $message_id,
                'chat_id' => $chat_id,
            ),
        );

        Helper::curlUnit($url, $apiKey, $fields);
    }

    public static function chat_delete_push($type, $user_id, $chat_id, $registrationIDs)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
        
        $fields = array(
            'registration_ids' => $registrationIDs,
            'data' => array(
                "type" => $type,
                'chat_id' => $chat_id,
            ),
        );

        Helper::curlUnit($url, $apiKey, $fields);
    }

    public static function contact_remove_push($type, $user_id, $chat_id, $registrationIDs)
    {
        $apiKey = Yii::app()->params['googleApiKey'];

        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';
        
        $user = User::model()->findByPk($user_id);
        $qrcode = $user->qrcode;
            
        $fields = array(
            'registration_ids' => $registrationIDs,
            'data' => array(
                "type" => $type,
                'qrcode' => $qrcode,
                'chat_id' => $chat_id
            ),
        );

        Helper::curlUnit($url, $apiKey, $fields);
    }
}

