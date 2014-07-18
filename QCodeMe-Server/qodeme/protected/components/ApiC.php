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
        
        $chatUserId = Yii::app()->db->createCommand()
        ->select('user_id')
        ->from('chat')
        ->where('id=:id', array(':id'=>$chat_id))
        ->queryScalar();
        
        $adminQRCode = Yii::app()->db->createCommand()
        ->select('qrcode')
        ->from('user')
        ->where('id=:id', array(':id'=>$chatUserId))
        ->queryScalar();
        
        $chatMemberData = Yii::app()->db->createCommand()
        ->select('color,status')
        ->from('chat_member')
        ->where('chat_id=:chat_id and user_id=:user_id', array(':chat_id'=>$chat_id, ':user_id'=>$user_id))
        ->queryRow();
        
        $favoriteData = Yii::app()->db->createCommand()
        ->select('*')
        ->from('favorite')
        ->where('chat_id=:chat_id and user_id =:user_id', array(':chat_id'=>$chat_id ,':user_id'=>$user_id))
        ->queryRow();
        
        $tagsData = Yii::app()->db->createCommand()
        ->select('tag')
        ->from('chat_tag')
        ->where('chat_id=:chat_id', array(':chat_id'=>$chat_id))
        ->queryAll();
        
        $tagString = '';
        if(!empty($tagsData))
        {
            $tagArray = array();
            foreach($tagsData as $tag)
            {
                $tagArray[] = $tag['tag'];
            }
            $tagString .= implode(',',$tagArray);
        }
        
        $is_favorite = 1;
        if(empty($favoriteData))
        {
            $is_favorite = 0;
        }
        
        $favoriteListData = Yii::app()->db->createCommand()
        ->select('COUNT(*) as count')
        ->from('favorite')
        ->where('chat_id=:chat_id', array(':chat_id'=>$chat_id))
        ->queryRow();
        
        $number_of_favorites = $favoriteListData['count'];
        
        $chatUserSettingsData = Yii::app()->db->createCommand()
        ->select('color')
        ->from('chat_user_settings')
        ->where('chat_id=:chat_id and user_id =:user_id', array(':chat_id'=>$chat_id ,':user_id'=>$user_id))
        ->queryRow();
        
        $response = array(
                'id' => $chat->id,
                'type' => $chat->type,
                'members' => $qrData,
                'qrcode' => $chat->qrcode,
                'latitude' => $chat->latitude,
                'longitude' => $chat->longitude,
                //'number_of_likes' => $chat->number_of_likes,
                'number_of_likes' => $number_of_favorites,
                'description' => $chat->description,
                'number_of_dislikes' => $chat->number_of_dislikes,
                'user_qrcode' => $adminQRCode,
                'is_locked' => $chat->is_locked,
                'number_of_flagged' => $chat->number_of_flagged,
                'chat_status' => $chat->status,
                'chat_title' => $chat->title,
                'color' => $chatMemberData['color'],
                'status' => $chatMemberData['status'],
                'number_of_members' => $chat->number_of_members,
                'chat_color' => $chatUserSettingsData['color'],
                'chat_height' => $chat->chat_height,
                'created' => $chat->created,
                'is_favorite' => $is_favorite,
                'tags' => $tagString,
                'messages' => $messagesData,
        );
        static::_send_resp($response);
    }

}
