<?php

/**
 * This is the model class for table "chat_member".
 *
 * The followings are the available columns in table 'chat_member':
 * @property integer $id
 * @property integer $chat_id
 * @property integer $user_id
 *
 * The followings are the available model relations:
 * @property Chat $chat
 * @property User $user
 */
class ChatMember extends CActiveRecord
{
    public $qrcode;
    public $title;
    public $tags;
    public $type;
    /**
     * Returns the static model of the specified AR class.
     * @param string $className active record class name.
     * @return ChatMember the static model class
     */
    public static function model($className=__CLASS__)
    {
        return parent::model($className);
    }

    /**
     * @return string the associated database table name
     */
    public function tableName()
    {
        return 'chat_member';
    }

    /**
     * @return array validation rules for model attributes.
     */
    public function rules()
    {
        // NOTE: you should only define rules for those attributes that
        // will receive user inputs.
        return array(
            array('chat_id, user_id', 'required'),
            array('chat_id, user_id', 'numerical', 'integerOnly'=>true),
            // The following rule is used by search().
            // Please remove those attributes that should not be searched.
            array('id, chat_id, user_id', 'safe', 'on'=>'search'),
        );
    }

    /**
     * @return array relational rules.
     */
    public function relations()
    {
        // NOTE: you may need to adjust the relation name and the related
        // class name for the relations automatically generated below.
        return array(
            'chat' => array(self::BELONGS_TO, 'Chat', 'chat_id'),
            'user' => array(self::BELONGS_TO, 'User', 'user_id'),
        );
    }

    /**
     * @return array customized attribute labels (name=>label)
     */
    public function attributeLabels()
    {
        return array(
            'id' => 'ID',
            'chat_id' => 'Chat',
            'user_id' => 'User',
        );
    }

    /**
     * Retrieves a list of models based on the current search/filter conditions.
     * @return CActiveDataProvider the data provider that can return the models based on the search/filter conditions.
     */
    public function search()
    {
        // Warning: Please modify the following code to remove attributes that
        // should not be searched.

        $criteria=new CDbCriteria;

        $criteria->compare('id',$this->id);
        $criteria->compare('chat_id',$this->chat_id);
        $criteria->compare('user_id',$this->user_id);

        return new CActiveDataProvider($this, array(
            'criteria'=>$criteria,
        ));
    }

    public static function createCM($params=array()){
        $chat_member = new ChatMember();
        $chat_member->chat_id = $params['chat_id'];
        $chat_member->user_id = $params['user_id'];
        if(! $chat_member->save() ){return FALSE;}
        return $chat_member;
    }
}