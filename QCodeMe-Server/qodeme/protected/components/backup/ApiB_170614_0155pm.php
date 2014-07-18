<?php

Class ApiB extends ApiBase {

    public static function account_logout() {
        static::_check_auth();
        $user = User::model()->findByPk(static::$_user_id);
        $user->auth_token = null;
       // $user->UUID = null;
        $user->save();
        static::_send_resp();
    }

    public static function contact_set_info() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $contact_id = Yii::app()->getRequest()->getPost('contact_id');
        if (!$contact_id)
            static::_send_resp(null, 701, 'user not found.');

        $title = Yii::app()->getRequest()->getPost('title');
        $color = Yii::app()->getRequest()->getPost('color');

        if (!$title && !$color)
            static::_send_resp(null, 101, 'Empty values title and color');

        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'contact_id=:contact_id and user_id =:user_id';
        $query->params = array(':contact_id' => $contact_id, ':user_id' => $user_id);
        $contact_info = Contact::model()->find($query);

        if ($contact_info) {
            $id = $contact_info['id'];
            Contact::model()->updateByPk($id, array('title' => $title, 'color' => $color));
            static::_send_resp();
        } else {
            static::_send_resp(null, 702, 'permission denied');
        }
    }

    public static function chat_set_info() {
        
        static::_check_auth();
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('id');
        $chat_title = Yii::app()->getRequest()->getPost('chat_title');
        $title = Yii::app()->getRequest()->getPost('title');
        $chat_color = Yii::app()->getRequest()->getPost('chat_color');
        $color = Yii::app()->getRequest()->getPost('color');
        $chat_height = Yii::app()->getRequest()->getPost('chat_height');
        $description = Yii::app()->getRequest()->getPost('description');
        $is_locked = Yii::app()->getRequest()->getPost('is_locked');
        $chat_status = Yii::app()->getRequest()->getPost('chat_status');
        $status = Yii::app()->getRequest()->getPost('status');        
        $latitude = Yii::app()->getRequest()->getPost('latitude');        
        $longitude = Yii::app()->getRequest()->getPost('longitude');   
        $tagString = Yii::app()->getRequest()->getPost('tags');
        $tags = Yii::app()->getRequest()->getPost('tags');
        $tags = explode(',', $tags);
        
        if($chat_height){ChatUserSettings::create($chat_id,$user_id,$chat_height);};  // расширять/переписать как будут известны остальные ChatUserSettings
        
        if (!$chat_id)
            static::_send_resp(null, 301, 'validation error');
        
//        $fp = fopen('log.txt', 'a');
//        fwrite($fp, 'Chat ID : '.$chat_id.', User ID : '.$user_id.'\r\n' );
//        fclose($fp);
        
        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'id=:id and user_id =:user_id';
        $query->params = array(':id' => $chat_id, ':user_id' => $user_id);
        $chat = Chat::model()->find($query);
        if ($chat != null) {
            $chat->description = $description;
            $chat->is_locked = $is_locked;
            $chat->color = $chat_color;
            $chat->status = $chat_status;
            $chat->title = $chat_title;
            $chat->latitude = $latitude;
            $chat->longitude = $longitude;
            if (!$chat->save())
            {
                static::_send_resp(null, 100, 'unknown error');
            }
            //static::_send_resp(null, 302, 'chat record not found');
        }
        
        $chatUserSettingsData = Yii::app()->db->createCommand()
        ->select('*')
        ->from('chat_user_settings')
        ->where('chat_id=:chat_id and user_id =:user_id', array(':chat_id'=>$chat_id ,':user_id'=>$user_id))
        ->queryRow();
        
        if(empty($chatUserSettingsData))
        {
            $chatUserSettingsObj = new ChatUserSettings;
            $chatUserSettingsObj->user_id = $user_id;
            $chatUserSettingsObj->chat_id = $chat_id;
            $chatUserSettingsObj->color = $chat_color;
            $result = $chatUserSettingsObj->save();
        }
        else
        {
            ChatUserSettings::model()->updateByPk($chatUserSettingsData['id'], array('color' => $chat_color));
        }
        
        if(!empty($tags))
        {
            $condition = 'chat_id=:chat_id';
            $params = array(':chat_id' => $chat_id);
            
            ChatTag::model()->deleteAll($condition,$params);
            foreach($tags as $tag)
            {
                ChatTag::create($chat_id,$tag);
            }
        }
        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'chat_id=:chat_id and user_id =:user_id';
        $query->params = array(':chat_id' => $chat_id, ':user_id' => $user_id);
        $chatMember = ChatMember::model()->find($query);
        if ($chatMember == null) {
            static::_send_resp(null, 302, 'chat member not found');
        }
        if(!$title || !$color || !$status)
        {
            Helper::set_chat_push('Chat updated', $user_id, $chat_id, $tagString, $status, $chat_color);
        }
        else
        {
            $is_public = $chatMember->chat->type;
            $chatMember->title = $title;
            $chatMember->color = $color;
            $chatMember->status = $status;

            if ($chatMember->save())
            {

                $query = new CDbCriteria;
                $query->select = '*';
                $query->condition = 'chat_id=:chat_id and user_id =:user_id';
                $query->params = array(':chat_id' => $chat_id, ':user_id' => $user_id);
                $contact = Contact::model()->find($query);

                if ($contact == null) {
                    static::_send_resp(null, 303, 'Contact not found');
                }

                $contact->title = $title;
                $contact->color = $color;

                if ($contact->save())
                {
                    Helper::set_chat_push('Chat updated', $user_id, $chat_id, $tagString, $status, $chat_color);
                    static::_send_resp();
                }
                else
                {
                    static::_send_resp(null, 100, 'unknown error');
                }

            }
            else
            {
                static::_send_resp(null, 100, 'unknown error');
            }
        }
    }

    public static function chat_drop_member() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        $qrcode = Yii::app()->getRequest()->getPost('qrcode');
        $chat = Chat::model()->findByPk($chat_id);
        if (!$chat) {
            static::_send_resp(null, 903, 'chat not foundt');
        }
        if ($chat->user_id != $user_id) {
            static::_send_resp(null, 901, 'permissions denied');
        }
        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'qrcode =:qrcode';
        $query->params = array(':qrcode' => $qrcode);
        $rem_user = User::model()->find($query);
        $rem_user_id = $rem_user->id;
        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'chat_id=:chat_id and user_id=:user_id';
        $query->params = array(':chat_id' => $chat_id, ':user_id' => $rem_user_id);

        $rem = ChatMember::model()->find($query);

        if (!$rem) {
            static::_send_resp(null, 902, 'not a member');
        }
        $rem->delete();
        static::_send_resp();
    }

    public static function contact_remove() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $contact_id = Yii::app()->getRequest()->getPost('contact_id');

        $chatData = Yii::app()->db->createCommand()
        ->select('*')
        ->from('contact')
        ->where('id=:id', array(':id' => $contact_id))
        ->queryRow();

        if (empty($chatData)) {
            static::_send_resp(null, 801, 'no such contact.');
        }
        
        $chat_id = $chatData['chat_id'];
        
        $query = "DELETE FROM `contact` WHERE id=:id";
        $parameters = array(":id"=>$contact_id);
        $command = Yii::app()->db->createCommand($query);
        $command->execute($parameters);
        
        $query = "DELETE FROM `chat_member` WHERE user_id=:user_id and chat_id=:chat_id";
        $parameters = array(':user_id' => $user_id, ':chat_id' => $chat_id);
        $command = Yii::app()->db->createCommand($query);
        $command->execute($parameters);
        
        $query = "DELETE FROM `chat` WHERE `id`=:id";
        $parameters = array(":id"=>$chat_id);
        $command = Yii::app()->db->createCommand($query);
        $command->execute($parameters);
        
        $registrationIDs[] = User::getToken($user_id);
        $registrationIDs[] = User::getToken($chatData['contact_id']);
        
        Helper::contact_remove_push('contact removed', $user_id, $chat_id, $registrationIDs);
                    
        static::_send_resp(array('contact_id' =>$contact_id));
        
//        $chat_id = $res->chat_id;
//        $query1 = new CDbCriteria();
//        $query1->select = '*';
//        $query1->condition = 'contact_id=:contact_id and user_id=:user_id';
//        $query1->params = array(':contact_id' => $contact_id, ':user_id' => $user_id);
//        $res1 = Contact::model()->find($query1);
//        $query2 = new CDbCriteria();
//        $query2->select = '*';
//        $query2->condition = 'user_id=:user_id and chat_id=:chat_id';
//        $query2->params = array(':user_id' => $user_id, ':chat_id' => $chat_id);
//        $res2 = ChatMember::model()->find($query2);
//        $query3 = new CDbCriteria();
//        $query3->select = '*';
//        $query3->condition = 'user_id=:contact_id and chat_id=:chat_id';
//        $query3->params = array(':contact_id' => $contact_id, ':chat_id' => $chat_id);
//        $res3 = ChatMember::model()->find($query3);
//        $chat = Chat::model()->findByPk($chat_id);
//        $chatm = Chat::model();
//        $remove_transaction = $chatm->dbConnection->beginTransaction();
//        if ($res->delete() && $res1->delete() && $res2->delete() && $res3->delete() && $chat->delete()
//        ) {
//            $remove_transaction->commit();
//        } else {
//            $transaction->rollBack();
//            static::_send_resp(null, 100, 'unknown error.');
//        }

        //':contact_id' => $contact_id,
        //$chat_id = $res->chat_id;
        //$resul=array('first_contact'=>$res,'second_contact'=>$res1,'first_member'=>$res2,'second_member'=>$res3);
        //  static::_send_resp($resul);
//        if (!$res) {
//            static::_send_resp(null, 801, 'Not found contact');
//        }
//
//        $res->delete();
//        $res1->delete();
//        static::_send_resp();
    }

    public static function chat_message() {
        static::_check_auth();
        date_default_timezone_set('UTC');
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        $message = Yii::app()->getRequest()->getPost('message');
        //(int)$datetime = Yii::app()->getRequest()->getPost('datetime');
        //$converted_date = date( 'Y-m-d H:i:s',$datetime);        
        $converted_date = Yii::app()->getRequest()->getPost('datetime');        
        $photourl = Yii::app()->getRequest()->getPost('photourl');        
        $has_photo = Yii::app()->getRequest()->getPost('has_photo');        
        $replyto_id = Yii::app()->getRequest()->getPost('replyto_id');        
        $is_flagged = Yii::app()->getRequest()->getPost('is_flagged');        
        $latitude = Yii::app()->getRequest()->getPost('latitude');        
        $longitude = Yii::app()->getRequest()->getPost('longitude');        
        $sendername = Yii::app()->getRequest()->getPost('sendername');    
        $is_search = Yii::app()->getRequest()->getPost('is_search');
        
        if (!$chat_id && !$message && !$datetime) {
            static::_send_resp(null, 101, 'Empty values');
        }

//        $query = new CDbCriteria;
//        $query->select = '*';
//        $query->condition = 't.chat_id=:chat_id and t.user_id=:user_id and contact.state < 1';
//        $query->join = 'JOIN user ON t.user_id=user.id JOIN contact ON user.id=contact.user_id ';
//        $query->params = array(':chat_id' => $chat_id, ':user_id' => $user_id);
//        $posts = ChatMember::model()->find($query);
//
//        if ($posts == null) {
//            static::_send_resp(null, 501, 'not a member or chat not found');
//        }

        if($is_search == 1)
        {
            $searchData = Yii::app()->db->createCommand()
            ->select('*')
            ->from('search')
            ->where('chat_id=:chat_id and user_id =:user_id', array(':chat_id'=>$chat_id ,':user_id'=>$user_id))
            ->queryRow();
            
            if(empty($searchData))
            {
                $chat_type = Yii::app()->db->createCommand()
                ->select('type')
                ->from('chat')
                ->where('id=:id', array(':id'=>$chat_id))
                ->queryRow();

                $sql = "insert into search (user_id, chat_id, chat_type) values (:user_id, :chat_id, :chat_type)";
                $parameters = array(":user_id"=>$user_id, ':chat_id' => $chat_id, ':chat_type' => $chat_type['type']);
                Yii::app()->db->createCommand($sql)->execute($parameters);
            }
        }
        
        $new_message = new Message;
        $new_message->user_id = $user_id;
        $new_message->chat_id = $chat_id;
        $new_message->text = $message;
        $new_message->created = $converted_date;
        $new_message->photourl = $photourl;
        $new_message->has_photo = $has_photo;
        $new_message->replyto_id = $replyto_id;
        $new_message->is_flagged = $is_flagged;
        $new_message->latitude = $latitude;
        $new_message->longitude = $longitude;
        $new_message->sendername = $sendername;
        if ($new_message->save()) 
        {
            Helper::sent_push('Message in Chat', $chat_id, $user_id, $new_message);
            static::_send_resp(array('message_id' =>$new_message->id));
        }
    }

    public static function chat_add_member() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        $member_qrcode = Yii::app()->getRequest()->getPost('member_qrcode');

        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'qrcode=:qrcode';
        $query->params = array(':qrcode' => $member_qrcode);
        $to_add = User::model()->find($query);

        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'chat_id=:chat_id';
        $query->params = array(':chat_id' => $chat_id);

        $chat_members = ChatMember::model()->findAll($query);

        $cm_qrcodes[] = $member_qrcode;

        foreach ($chat_members as $chat_member) {
            $cm_qrcodes[] = $chat_member->user->qrcode;
        }
        $chat = Chat::model()->findByPk($chat_id);

        $qrcode = $chat->user->qrcode;
        foreach ($chat_members as $chat_member) {
            $memb_arr[] = $chat_member['user_id'];
        }
        if ($chat['type'] == 0) {
            static::_send_resp(null, 403, 'permissions denied');
        }
        if ($chat['type'] == 1 && $chat['user_id'] != $user_id) {
            static::_send_resp(null, 404, 'permissions denied');
        }
        if (in_array($to_add['id'], $memb_arr)) {
            static::_send_resp(null, 402, 'already member');
        } else {

        }
        $add_member_off_chat = new ChatMember;
        $add_member_off_chat->user_id = $to_add['id'];
        $add_member_off_chat->chat_id = $chat_id;
        $result = $add_member_off_chat->save();

        $userIds = Yii::app()->db->createCommand()
        ->select('user_id')
        ->from('chat_member')
        ->where('chat_id=:chat_id', array(':chat_id'=>$chat_id))
        ->queryAll();
        
        if(!empty($userIds))
        {
            $numberOfMembers = count($userIds);
            
            Chat::model()->updateByPk($chat_id, array('number_of_members' => $numberOfMembers));

            $chatTagsData = Yii::app()->db->createCommand()
            ->select('tag')
            ->from('chat_tag')
            ->where('chat_id=:chat_id', array(':chat_id'=>$chat_id))
            ->queryAll();

            $tags = array();
            if(!empty($chatTagsData))
            {
                foreach($chatTagsData as $chatTag)
                {
                    $tags[] = $chatTag['tag'];
                }
            }
            $tagString = implode(',',$tags);
            
            Helper::chat_add_member_push('Chat add member', $to_add['id'], $chat_id, $numberOfMembers, $chat['type'], $tagString);
        }
        
        if ($result) {
            $response = array();
            $response['id'] = $chat_id;
            $response['type'] = $chat['type'];
            $response['members'] = $cm_qrcodes;
            $response['qrcode'] = $chat['qrcode'];
        }
        static::_send_resp($response);
    }

    public static function chat_create() {
        static::_check_auth();
        $user_id = static::$_user_id;

        $user = User::model()->findByPk($user_id);
        $user_code = $user->qrcode;

        $type = Yii::app()->getRequest()->getPost('type');
        $title = Yii::app()->getRequest()->getPost('title');
        $latitude = Yii::app()->getRequest()->getPost('latitude');
        $longitude = Yii::app()->getRequest()->getPost('longitude');
        $description = Yii::app()->getRequest()->getPost('description');
        $is_locked = Yii::app()->getRequest()->getPost('is_locked');
        $status = Yii::app()->getRequest()->getPost('status');
        $tags = Yii::app()->getRequest()->getPost('tags');
        $tags = explode(',', $tags);
        $qrcode = Helper::generate_qrcode_string($user_id);

        $chat = Chat::model();

        $crreate_transaction = $chat->dbConnection->beginTransaction();

        $new_chat = new Chat;
        $new_chat->qrcode = $qrcode;
        $new_chat->title = $title;
        $new_chat->latitude = $latitude;
        $new_chat->longitude = $longitude;
        $new_chat->description = $description;
        $new_chat->is_locked = $is_locked;
        $new_chat->status = $status;
        $new_chat->user_id = $user_id;
        $new_chat->type = $type;
        $new_chat->created = new CDbExpression('NOW()');
        if ($new_chat->save()) {
            $chat_id = $new_chat->id;
            if(!empty($tags))
            {
                $condition = 'chat_id=:chat_id';
                $params = array(':chat_id' => $chat_id);

                ChatTag::model()->deleteAll($condition,$params);
                foreach($tags as $tag)
                {
                    ChatTag::create($chat_id,$tag);
                }
            }

            $new_chat_member = new ChatMember;
            $new_chat_member->user_id = $user_id;
            $new_chat_member->chat_id = $chat_id;

            if ($new_chat_member->save()) {
                $crreate_transaction->commit();
            } else {
                $transaction->rollBack();
                static::_send_resp(null, 100, 'unknown error.');
            }
        }
        $result = array('id' => $chat_id, 'type' => $type, 'members' => array($user_code), 'qrcode' => $qrcode);
        static::_send_resp($result);
    }

    public static function contact_add() {
        static::_check_auth();
        date_default_timezone_set('UTC');
        $user_id = static::$_user_id;
        $user_inf = User::model()->findByPK($user_id);
        $user_code = $user_inf->qrcode;
        $partner_qrcode = Yii::app()->getRequest()->getPost('partner_qrcode');
        $pub_name= Yii::app()->getRequest()->getPost('public_name');
        $latitude= Yii::app()->getRequest()->getPost('latitude');
        $longitude= Yii::app()->getRequest()->getPost('longitude');

        $model = New Contact();
        $model->attributes=$_POST;
        $model->validate();
        $ChatModel = New Chat();
        $ChatModel->attributes=$_POST;
        $ChatModel->validate();
        $contact_id = static::getPartner($partner_qrcode);

        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'contact_id=:contact_id and user_id=:user_id';
        $query->params = array(':contact_id' => $contact_id, ':user_id' => $user_id);
        if (
                $is_cont = Contact::model()->find($query)) {
            $response = Array(
                'state ' =>$is_cont->state ,
            );
            static::_send_resp($response);
        }
        $chat = Chat::create($params=array('user_id' => $user_id,'qrcode' => $partner_qrcode),$ChatModel);
        $add_transaction = $chat->dbConnection->beginTransaction();
        $who_init=$user_id;
        $contact = Contact::createContact( array('chat_id' => $chat->id,
            'user_id' =>$user_id,
            'contact_id' => $contact_id,
            'who_init' => $who_init,
            'public_name' => $pub_name,
            'latitude' => $latitude,
            'longitude' => $longitude,
            'state' => 1,
            'who'=> $contact_id),$model);
        if (!$contact) {$add_transaction->rollBack();
            static::_send_resp(null, 100, 'unknown error.');}

        $contact1 = Contact::createContact( array('chat_id' => $chat->id,
            'user_id' =>$contact_id,
            'contact_id' => $user_id,
            'who_init' => $who_init,
            'public_name' => $pub_name,
            'latitude' => $latitude,
            'longitude' => $longitude,
            'state' => 2,
            'who'=> $user_id),$model);

        if (!$contact1) {$add_transaction->rollBack();
            static::_send_resp(null, 100, 'unknown error.');}

        $chat_member = ChatMember::createCM($params=array('chat_id' => $chat->id, 'user_id' => $contact_id)) ;
        $chat_u_member = ChatMember::createCM($params=array('chat_id' => $chat->id, 'user_id' => $user_id)) ;
        if ($chat_member && $chat_u_member) {
            $add_transaction->commit();
        } else {
            $add_transaction->rollBack();
            static::_send_resp(null, 100, 'unknown error.');
        }


        $cont_arr = array(
            'id' => (int)$contact->id,
            'private_chat_id'=>(int)$contact->chat_id,
            'qrcode' => $contact->qrcode,
            'title' => $contact->title,
            'color' => $contact->color,
            'state' => (int)$contact->state,
            'message' => $contact->message,
            'public_name' => $contact->public_name,
            'datetime' => $contact->datetime,
            'latitude' => $contact->latitude,
            'longitude' => $contact->longitude,

        );

      $contact_settings = Settings::get_settings($contact_id);
      if($contact_settings['auto_accept'] == 1){
          if(!Contact::doAccept($params = array('contact_id' => $contact_id,'user_id' => $user_id ))){
          static::_send_resp(null, 100, 'unknown error.');}
          $cont_arr['state'] = 0;
      }


        $response = array(
           // 'id' => $chat['id'],
           // 'type' => 0,
           // 'members' => array($user_code, $partner_qrcode),
          // 'qrcode' => null,
          //  'private_chat_id'=>$chat['id'],
            'contact' => $cont_arr
        );
         Helper::sent_push('Contact added', $chat['id'], $user_id, null,  $partner_qrcode, $contact_id);
       // Helper::sent_push('Contact added', $chat['id'], $contact_id, null, $user_code);
        static::_send_resp($response);
    }

    public static function lookup() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $limit = 20;
        $param_serch = Yii::app()->getRequest()->getPost('q');
        $type = Yii::app()->getRequest()->getPost('chat_type');
        $page = Yii::app()->getRequest()->getPost('page_no');
        
        $searchResult = array();
        
        $offset = ($limit * ($page - 1));
        if($type == 0)
        {
            $chatIds = Yii::app()->db->createCommand()
            ->selectDistinct('chat_id')
            ->from('contact')
            ->where('title LIKE :title', array(':title'=>"%$param_serch%"))
            ->limit($limit)
            ->offset($offset)
            ->queryAll();
            
            $chatIdArray = array();
            if(!empty($chatIds))
            {   
                foreach($chatIds as $chatId)
                {
                    $chatIdArray[] = $chatId['chat_id'];
                }
            }
            
            $searchResult = Yii::app()->db->createCommand()
            ->selectDistinct('c.*,ct.title as title')
            ->from('chat c')
            ->leftJoin('contact ct', 'ct.chat_id = c.id')
            ->where(array('in', 'c.id', $chatIdArray))
            ->andWhere('ct.title LIKE :title', array(':title'=>"%$param_serch%"))
            ->andWhere('ct.title IS NOT NULL') 
            ->queryAll();
        }
        else if($type == 1)
        {
            $searchResult = Yii::app()->db->createCommand()
            ->select('*')
            ->from('chat')
            ->where('title LIKE :match AND type = '.$type.'', array(':match' => "%$param_serch%"))
            ->limit($limit)
            ->offset($offset)
            ->queryAll();
        }
        else
        {
            $searchResult = Yii::app()->db->createCommand()
            ->select('*')
            ->from('chat')
            ->where('title LIKE :match AND type = '.$type.' AND is_searchable = 1 or tags LIKE :match AND type = '.$type.' AND is_searchable = 1', array(':match' => "%$param_serch%"))
            ->limit($limit)
            ->offset($offset)
            ->queryAll();
        }
        
        if(!empty($searchResult))
        {
            foreach($searchResult as $key => $result)
            {
                $favoriteData = Yii::app()->db->createCommand()
                ->select('*')
                ->from('favorite')
                ->where('chat_id=:chat_id and user_id =:user_id', array(':chat_id'=>$result['id'] ,':user_id'=>$user_id))
                ->queryRow();

                $is_favorite = 1;
                if(empty($favoriteData))
                {
                    $is_favorite = 0;
                }

                $searchResult[$key]['number_of_likes'] = count($favoriteData);
                $searchResult[$key]['is_favorite'] = $is_favorite;
            }
        }
        
        $response = Array(
            'searchResult' => $searchResult,
        );
        static::_send_resp($response);
    }

    public static function register_token() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $push_token = Yii::app()->getRequest()->getPost('push_token');
        //$user = User::model()->findByPk($user_id);
        //$user->push_token = $push_token;
        $user = User::model()->updateByPk($user_id, array('push_token' => $push_token));

        if ($user > 0)
            static::_send_resp(array('success' => true));
        else
            static::_send_resp(array('success' => false));
    }

    /* US1307_31_12-2  */


    public static function getPartner($partner_qrcode){

        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'qrcode=:qrcode';
        $query->params = array(':qrcode' => $partner_qrcode);
        $user = User::model()->find($query);
        if (!$user){return false;}
        return $user->id;
    }


    public static function contact_reject() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $partner_qrcode = Yii::app()->getRequest()->getPost('partner_qrcode');
        $contact_id=static::getPartner($partner_qrcode);
        Helper::sent_individual_push('contact reject',$contact_id,null,$user_id);
        if(!Contact::doReject($params = array('contact_id' => $contact_id,'user_id' => $user_id ))){
            static::_send_resp(null, 100, 'unknown error.');
        }
        static::_send_resp('all clear');

    }

    public static function contact_block(){
        static::_check_auth();
        $user_id = static::$_user_id;
        $partner_qrcode = Yii::app()->getRequest()->getPost('partner_qrcode');
        $contact_id=static::getPartner($partner_qrcode);
        if(!Contact::doBlock($params = array('contact_id' => $contact_id,'user_id' => $user_id ))){
            static::_send_resp(null, 100, 'unknown error.');
        }
        Helper::sent_individual_push('contact block',$contact_id,null,$user_id);
        static::_send_resp('all clear');
    }

    public static function contact_accept(){
        date_default_timezone_set('UTC');
        static::_check_auth();
        $user_id = static::$_user_id;
        $partner_qrcode = Yii::app()->getRequest()->getPost('partner_qrcode');
        $contact_id=static::getPartner($partner_qrcode);
        if(!Contact::doAccept($params = array('contact_id' => $contact_id,'user_id' => $user_id ))){
            static::_send_resp(null, 100, 'unknown error.');
        }
        Helper::sent_individual_push('contact accept',$contact_id,null,$user_id);
        static::_send_resp('all clear');
    }

    public static function set_user_settings(){
        static::_check_auth();
        $user_id = static::$_user_id;
        
        $model = New Settings();
        $model->attributes=$_POST;
        $model->validate();
        //echo "<pre>";print_r($model->attributes);die;
        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'user_id =:user_id';
        $query->params = array(':user_id' => $user_id);
        $settings_info = Settings::model()->find($query);

        if ($settings_info) {
            $id = $settings_info['id'];
            Settings::model()->updateByPk($id, $_POST);
            static::_send_resp();
        } else {
            if(!Settings::create($model,$user_id)){
                static::_send_resp(null, 100, 'unknown error.');
            }
        }

        static::_send_resp('all clear');
    }

    public static function get_user_settings(){
        static::_check_auth();
        $response = array( 'settingsObj' => Settings::get_settings(static::$_user_id));
        static::_send_resp($response);
    }

    public static function messageRead(){
        static::_check_auth();
        $message_id = Yii::app()->getRequest()->getPost('message_id');
        if(!Message::doRead($message_id)){
            static::_send_resp(null, 0, 'unknown error.');
        }
        $message = Message::get_message($message_id);
        Helper::sent_message_push('Message was read', $message[0]['user_id'], $message[0]['id'] );
        $response = array(
            'message_id' => $message[0]['id'],
            'chat_id' => $message[0]['chat_id'],
            'state' =>  $message[0]['state']
        );
        static::_send_resp($response);
    }

    public static function get_chat_height(){
        static::_check_auth();
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        $response = ChatUserSettings::getHeight(static::$_user_id,$chat_id);
        static::_send_resp($response);
    }

    public static function contactSetLocation() {
        static::_check_auth();
        $location = Yii::app()->getRequest()->getPost('location');
        $contactId = Yii::app()->getRequest()->getPost('contact_id');
        $model = Contact::model()->findByPk($contactId);
        $model->location = $location;
        if(!$model->save(false)){static::_send_resp(null, 100, 'unknown error.');}
        static::_send_resp('all clear');
    }

    //upload image
    public static function uploadImage() {
        ini_set('post_max_size', '10M');
        ini_set('upload_max_filesize', '10M');
        //static::_check_auth();
        $encodedString = Yii::app()->getRequest()->getPost('image');
        $messageID = Yii::app()->getRequest()->getPost('message_id');
        
//        $fp = fopen('log.txt', 'a');
//        fwrite($fp, 'String : '.$encodedString.'\r\n' );
//        fclose($fp);
        if (!$encodedString)
            static::_send_resp(null, 301, 'validation error');
        
        $decodedString = base64_decode($encodedString);
        $imageName = rand(100000000000,10000000000000000);
        
        file_put_contents('uploads/'.$imageName.'.png',$decodedString);
        
        $imageUrl = Yii::app()->getBaseUrl(true).'/uploads/'.$imageName.'.png';
        $response = array(
            'message_id' => $messageID,
            'image_url' => $imageUrl
        );
        static::_send_resp($response);
    }

    //set favorite
    public static function setFavorite() {
        static::_check_auth();
        $userID = static::$_user_id;
        $chatID = Yii::app()->getRequest()->getPost('chat_id');
        $is_favorite = Yii::app()->getRequest()->getPost('is_favorite');
        $dateTime = Yii::app()->getRequest()->getPost('date_time');
        
        if (!$chatID)
            static::_send_resp(null, 301, 'validation error');
        
        
        if($is_favorite == 1)
        {
            $favoriteData = Yii::app()->db->createCommand()
            ->select('*')
            ->from('favorite')
            ->where('chat_id=:chat_id and user_id =:user_id', array(':chat_id'=>$chatID ,':user_id'=>$userID))
            ->queryRow();

            if(empty($favoriteData))
            {
                $favoriteObj = new Favorite;
                $favoriteObj->user_id = $userID;
                $favoriteObj->chat_id = $chatID;
                $favoriteObj->datetime = $dateTime;

                if (!$favoriteObj->save()) 
                {
                    static::_send_resp(null, 100, 'unknown error');
                }
            }
        }
        else
        {
            $query = "DELETE FROM `favorite` WHERE `user_id`=:user_id AND `chat_id`=:chat_id";
            $parameters = array(":user_id"=>$userID, ':chat_id' => $chatID);
            $command = Yii::app()->db->createCommand($query);
            $command->execute($parameters);
        }
        
//        $userIds = Yii::app()->db->createCommand()
//        ->select('user_id')
//        ->from('chat_member')
//        ->where('chat_id=:chat_id', array(':chat_id'=>$chatID))
//        ->queryAll();
//
//        if(!empty($userIds))
//        {
            Helper::set_favorite_push('Set favorite', $userID, $chatID);
//        }
        
        static::_send_resp();
    }

    //set flagged
    public static function setFlagged() {
        static::_check_auth();
        $chatID = Yii::app()->getRequest()->getPost('chat_id');
        $isFlagged = Yii::app()->getRequest()->getPost('is_flagged');
        $messageID = Yii::app()->getRequest()->getPost('message_id');
        
        if (!$chatID || !$messageID)
            static::_send_resp(null, 301, 'validation error');
        
        
        $messageData = Message::model()->findByPK($messageID);

        if ($messageData) 
        {
            Message::model()->updateByPk($messageID, array('is_flagged' => $isFlagged));
            
            $numberOfFlagged = Yii::app()->db->createCommand()
            ->select('COUNT(*) as count')
            ->from('message')
            ->where('is_flagged=:is_flagged', array(':is_flagged'=>1))
            ->queryRow();
            
            Chat::model()->updateByPk($chatID, array('number_of_flagged' => $numberOfFlagged['count']));
        
            $userIds = Yii::app()->db->createCommand()
            ->select('user_id')
            ->from('chat_member')
            ->where('chat_id=:chat_id', array(':chat_id'=>$chatID))
            ->queryAll();

            if(!empty($userIds))
            {
                Helper::set_flagged_push('Set flagged', $userIds, $messageID, $numberOfFlagged['count'], $isFlagged);
            }

            static::_send_resp();
        }
        else
        {
            static::_send_resp(null, 302, 'message not found');
        }
    }
    
    public static function clear_search() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $chat_type = Yii::app()->getRequest()->getPost('chat_type');
        
        if (!$chat_type) {
            static::_send_resp(null, 902, 'chat type is required');
        }
        
        $query = "DELETE FROM `search` WHERE `user_id`=:user_id AND `chat_type`=:chat_type";
        $parameters = array(":user_id"=>$user_id, ':chat_type' => $chat_type);
        $command = Yii::app()->db->createCommand($query);
        $command->execute($parameters);
        
        static::_send_resp();
    }
    
    public static function set_searchable() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        $is_searchable = Yii::app()->getRequest()->getPost('is_searchable');
        
        if (!$chat_id) {
            static::_send_resp(null, 902, 'chat id is required');
        }
        
        $chatResult = Chat::model()->updateByPk($chat_id, array('is_searchable' => $is_searchable));
        
        static::_send_resp();
    }
    
    public static function delete_message() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        $message_id = Yii::app()->getRequest()->getPost('message_id');
        
        if (!$chat_id && !$message_id) {
            static::_send_resp(null, 302, 'chat type and message id are required');
        }
        
        $messageResult = Message::model()->deleteByPk($message_id);
        
        Helper::delete_message_push('Message deleted', $user_id, $chat_id, $message_id);
        
        static::_send_resp(array('message_id' =>$message_id));
    }
    
    public static function chat_delete() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        
        if (!$chat_id) {
            static::_send_resp(null, 302, 'chat id is required');
        }
        
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
        
        $query = "DELETE FROM `chat_member` WHERE `chat_id`=:chat_id";
        $parameters = array(":chat_id"=>$chat_id);
        $command = Yii::app()->db->createCommand($query);
        $command->execute($parameters);
        
        $query = "DELETE FROM `chat_tag` WHERE `chat_id`=:chat_id";
        $parameters = array(":chat_id"=>$chat_id);
        $command = Yii::app()->db->createCommand($query);
        $command->execute($parameters);
        
        $query = "DELETE FROM `chat_user_settings` WHERE `chat_id`=:chat_id";
        $parameters = array(":chat_id"=>$chat_id);
        $command = Yii::app()->db->createCommand($query);
        $command->execute($parameters);
        
        $query = "DELETE FROM `chat` WHERE `id`=:id";
        $parameters = array(":id"=>$chat_id);
        $command = Yii::app()->db->createCommand($query);
        $command->execute($parameters);
        
        Helper::chat_delete_push('chat deleted', $user_id, $chat_id, $registrationIDs);
        
        static::_send_resp(array('chat_id' =>$chat_id));
    }
}

