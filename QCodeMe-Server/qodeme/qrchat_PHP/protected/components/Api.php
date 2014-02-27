<?php

Class Api extends ApiBase {

    public static function account_create() {

        $password = Yii::app()->getRequest()->getPost('password');
        if($password == '')
            static::_send_resp(null,101,'Empty password.');

        $qrcode = Helper::generate_qrcode_string($password);
        $token = Helper::generate_token($password);
        $user = new User();
        $user->password = $password;
        $user->qrcode = $qrcode;
        $user->auth_token = $token;
        $user->last_active = new CDbExpression('NOW()');
        $user->last_login = new CDbExpression('NOW()');
        $user->created = new CDbExpression('NOW()');

        if($user->save())
        {
            $response = array(
                    'id' =>$user->getPrimaryKey(),
                    'qrcode' => $qrcode,
                    'token' => $token,
            );
            static::_send_resp($response);
        }
        else
        {
            static::_send_resp($user->getErrors());
        }

    }

    public static function account_login()
    {
        $data = array(
                'qrcode' => Yii::app()->getRequest()->getPost('qrcode'),
                'password' => Yii::app()->getRequest()->getPost('password'),
        );

        if(($data['qrcode'] == '') || ($data['password'] == ''))
            static::_send_resp(null,201,'Empty password or qrcode.');

        $user = User::model()->check_login($data['qrcode'], $data['password']);


        if(!$user)
        {
            static::_send_resp(null,201,'Wrong password or qrcode.');
        }
        $token = Helper::generate_token($user->getPrimaryKey());
        $user->auth_token = $token;
        $user->last_active = new CDbExpression('NOW()');
        $user->last_login = new CDbExpression('NOW()');

        if($user->save())
        {
            $response = array();
            /*$response = static::get_contacts($user->getPrimaryKey());*/
            $response['id'] = (int)$user->getPrimaryKey();
            $response['token'] = $token;

            static::_send_resp($response);

        }
        else
        {
            static::_send_resp($user->getErrors());
        }
    }

    public static function account_contacts()
    {
        static::_check_auth();
        $response = static::get_contacts(static::$_user_id);
        static::_send_resp($response);
    }

    private static function get_contacts($user_id)
    {
        $criteria = new CDbCriteria();
        $criteria->alias='c';
        $criteria->select = 'c.id, u.qrcode as qrcode, c.title, c.color,c.chat_id,c.state,c.message,c.public_name,c.datetime,c.location';
        $criteria->condition = 'c.user_id = :user_id';
        $criteria->join = 'JOIN user u ON c.contact_id=u.id';
        $criteria->params = array(':user_id' => $user_id);
        $contacts = Contact::model()->findAll($criteria);
        $cont_arr =  array();
        foreach ($contacts as $one)
        {
            $cont_arr[] = array(
                    'id' => $one->id,
                    'private_chat_id'=>(int)$one->chat_id,
                    'qrcode' => $one->qrcode,
                    'title' => $one->title,
                    'color' => (int)$one->color,
                    'state' => (int)$one->state,
                    'message' => $one->message,
                    'public_name' => $one->public_name,
                    'datetime' => $one->datetime,
                    'location' => $one->location,

            );
        }

        $criteria_chat = new CDbCriteria();
        $criteria_chat->alias='cm';
        $criteria_chat->select = 'c.id, c.qrcode, c.title, c.tags, c.type';
        $criteria_chat->condition = 'cm.user_id = :user_id AND (c.type = '.Chat::CHAT_PRIVATE_GROUP.' OR c.type = '.Chat::CHAT_PUBLIC_GROUP.')';
        $criteria_chat->join = 'JOIN chat c ON cm.chat_id=c.id';
        $criteria_chat->params = array(':user_id' => $user_id);
        $chats = ChatMember::model()->findAll($criteria_chat);
        $chat_arr =  array();
        foreach ($chats as $one)
        {
            $chat_height =ChatUserSettings::getHeight($user_id,$one->id);
            $chat_arr[] = array(
                    'id' => $one->id,
                    'qrcode' => $one->qrcode,
                    'title' => $one->title,
                    'tags' => $one->tags,
                    'color' => $one->color,
                    'type' => $one->type,
                    'chat_height' => $chat_height,
            );
        }
        $response = array(
                'chat_member' => $chat_arr,
                'contacts' => $cont_arr,
        );
        return $response;
    }

}
