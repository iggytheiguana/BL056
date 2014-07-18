<?php

Class ApiC extends ApiBase {

    public static function chat_load() {
        static::_check_auth();
        $user_id = static::$_user_id;
        $chat_id = Yii::app()->getRequest()->getPost('chat_id');
        $offset = Yii::app()->getRequest()->getPost('page', 0);
        $limit = Yii::app()->getRequest()->getPost('limit', 25);

        $chat = Chat::model()->findByPk((int) $chat_id);
        if ( ! $chat) {
            static::_send_resp(null, 602, 'chat not found');
        }

        $query=new CDbCriteria;
        $query->condition = 'chat_id=:chat_id and user_id=:user_id';
        $query->params=array(':chat_id'=>$chat_id, ':user_id'=>$user_id);
        $isMember = ChatMember::model()->find($query);
        if ( ! $isMember) {
            static::_send_resp(null, 601, 'not a member');
        }

        $query=new CDbCriteria;
        $query->condition = 'chat_id=:chat_id';
        $query->params=array(':chat_id'=>$chat_id);
        $members = ChatMember::model()->with('user')->findAll($query);

        $qrData = array();
        foreach ($members as $member) {
            $qrData[] = $member->user->qrcode;
        }

        $query=new CDbCriteria;
        $query->condition = 'chat_id=:chat_id';
        $query->offset = (int) $offset * (int) $limit;
        $query->limit = (int) $limit;
        $query->order = 't.created DESC';
        $query->params=array(':chat_id'=>$chat_id);
        $messages = Message::model()->with('user')->findAll($query);

        $messagesData = array();
        foreach ($messages as $message) {
            $messagesData[] = array(
                    'id' => $message->id,
                    'created' => $message->created,
                    'from_qrcode' => $message->user->qrcode,
                    'message' => $message->text,
                    'state' => $message->state,
                    'has_photo' => $message->has_photo,
                    'photourl' => $message->photourl,
                    'replyto_id' => $message->replyto_id,
                    'is_flagged' => $message->is_flagged,
                    'latitude' => $message->latitude,
                    'longitude' => $message->longitude,
                    'sendername' => $message->sendername,
            );
        }
        
        $adminQRCode = Yii::app()->db->createCommand()
        ->select('qrcode')
        ->from('user')
        ->where('id=:id', array(':id'=>$user_id))
        ->queryScalar();
        
        $response = array(
                'id' => $chat->id,
                'type' => $chat->type,
                'members' => $qrData,
                'qrcode' => $chat->qrcode,
                'latitude' => $chat->latitude,
                'longitude' => $chat->longitude,
                'number_of_likes' => $chat->number_of_likes,
                'description' => $chat->description,
                'number_of_dislikes' => $chat->number_of_dislikes,
                'user_qrcode' => $adminQRCode,
                'is_locked' => $chat->is_locked,
                'number_of_flagged' => $chat->number_of_flagged,
                'status' => $chat->status,
                'number_of_members' => $chat->number_of_members,
                'chat_color' => $chat->color,
                'chat_height' => $chat->chat_height,
                'created' => $chat->created,
                'messages' => $messagesData,
        );
        static::_send_resp($response);
    }

}
