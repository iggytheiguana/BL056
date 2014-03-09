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
        $title = Yii::app()->getRequest()->getPost('title');
        $color = Yii::app()->getRequest()->getPost('color');
        $chat_height = Yii::app()->getRequest()->getPost('chat_height');
        if($chat_height){ChatUserSettings::create($chat_id,$user_id,$chat_height);};  // расширять/переписать как будут известны остальные ChatUserSettings
        if (!$chat_id || !$title || !$color)
            static::_send_resp(null, 301, 'validation error');
        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'chat_id=:chat_id and user_id =:user_id';
        $query->params = array(':chat_id' => $chat_id, ':user_id' => $user_id);
        $chat = ChatMember::model()->find($query);
        if ($chat == null) {
            static::_send_resp(null, 303, 'chat not found');
        }
        $is_public = $chat->chat->type;
        $chat->title = $title;
        $chat->color = $color;

        if ($chat->save())
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

            if ($contact->save()){
                //Helper::sent_push('Chat status changed', null, $chat['id'], $user_id);
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

        $query = new CDbCriteria();
        $query->select = '*';
        $query->condition = 'contact_id=:contact_id and user_id=:user_id';
        $query->params = array(':contact_id' => $contact_id, ':user_id' => $user_id);
        $res = Contact::model()->find($query);

        if (!$res) {
            static::_send_resp(null, 801, 'no such contact.');
        }
        $chat_id = $res->chat_id;
        $query1 = new CDbCriteria();
        $query1->select = '*';
        $query1->condition = 'contact_id=:user_id and user_id=:contact_id';
        $query1->params = array(':contact_id' => $contact_id, ':user_id' => $user_id);
        $res1 = Contact::model()->find($query1);
        $query2 = new CDbCriteria();
        $query2->select = '*';
        $query2->condition = 'user_id=:user_id and chat_id=:chat_id';
        $query2->params = array(':user_id' => $user_id, ':chat_id' => $chat_id);
        $res2 = ChatMember::model()->find($query2);
        $query3 = new CDbCriteria();
        $query3->select = '*';
        $query3->condition = 'user_id=:contact_id and chat_id=:chat_id';
        $query3->params = array(':contact_id' => $contact_id, ':chat_id' => $chat_id);
        $res3 = ChatMember::model()->find($query3);
        $chat = Chat::model()->findByPk($chat_id);
        $chatm = Chat::model();
        $remove_transaction = $chatm->dbConnection->beginTransaction();
        if ($res->delete() && $res1->delete() && $res2->delete() && $res3->delete() && $chat->delete()
        ) {
            $remove_transaction->commit();
        } else {
            $transaction->rollBack();
            static::_send_resp(null, 100, 'unknown error.');
        }

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
        static::_send_resp();
    }

    public static function chat_message() {
        static::_check_auth();
        date_default_timezone_set('UTC');
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        $message = Yii::app()->getRequest()->getPost('message');
        (int)$datetime = Yii::app()->getRequest()->getPost('datetime');
        $converted_date = date( 'Y-m-d H:i:s',$datetime);
        if (!$chat_id && !$message && !$datetime) {
            static::_send_resp(null, 101, 'Empty values');
        }

        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 't.chat_id=:chat_id and t.user_id=:user_id and contact.state < 1';
        $query->join = 'JOIN user ON t.user_id=user.id JOIN contact ON user.id=contact.user_id ';
        $query->params = array(':chat_id' => $chat_id, ':user_id' => $user_id);
        $posts = ChatMember::model()->find($query);

        if ($posts == null) {
            static::_send_resp(null, 501, 'not a member or chat not found');
        }

        $new_message = new Message;
        $new_message->user_id = $user_id;
        $new_message->chat_id = $chat_id;
        $new_message->text = $message;
        $new_message->created = $converted_date;
        if (
                $new_message->save()) {
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

        $user = User::model()->findByPk(static::$_user_id);
        $user_code = $user->qrcode;

        $type = Yii::app()->getRequest()->getPost('type');

        $title = Yii::app()->getRequest()->getPost('title');

        $tags = Yii::app()->getRequest()->getPost('tags');

        $qrcode = Helper::generate_qrcode_string($user_id);

        $chat = Chat::model();

        $crreate_transaction = $chat->dbConnection->beginTransaction();

        $new_chat = new Chat;
        $new_chat->qrcode = $qrcode;
        $new_chat->title = $title;
        $new_chat->tags = $tags;
        $new_chat->user_id = $user_id;
        $new_chat->type = $type;
        $new_chat->created = new CDbExpression('NOW()');
        if ($new_chat->save()) {
            $chat_id = $new_chat->id;

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
        $user_id = static::$_user_id;
        $user_inf = User::model()->findByPK($user_id);
        $user_code = $user_inf->qrcode;
        $partner_qrcode = Yii::app()->getRequest()->getPost('partner_qrcode');
        $pub_name= Yii::app()->getRequest()->getPost('public_name');

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
            'state' => 1,
            'who'=> $contact_id),$model);
        if (!$contact) {$add_transaction->rollBack();
            static::_send_resp(null, 100, 'unknown error.');}

        $contact1 = Contact::createContact( array('chat_id' => $chat->id,
            'user_id' =>$contact_id,
            'contact_id' => $user_id,
            'who_init' => $who_init,
            'public_name' => $pub_name,
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
            'location' => $contact->location,

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
        $param_serch = Yii::app()->getRequest()->getPost('q');
        $find_chats = array();
        $searchResult = array();
        $find_chats = Chat::model()->findAll(
                'title LIKE :match AND type = 2 or tags LIKE :match AND type = 2', array(':match' => "%$param_serch%")
        );
        foreach ($find_chats as $one) {
            $searchResult[] = array(
                'id' => (int) $one->id,
                'qrcode' => $one->qrcode,
                'title' => $one->title,
                'tags' => $one->tags
            );
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
        if(!Settings::create($model,$user_id)){
            static::_send_resp(null, 100, 'unknown error.');
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
            static::_send_resp(null, 100, 'unknown error.');
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
}

