<?php

/**
 * This is the model class for table "chat_user_settings".
 *
 * The followings are the available columns in table 'settings':
 * @property integer $id
 * @property integer $user_id
 * @property integer $chat_id
 * @property string $chat_height
 *
 * The followings are the available model relations:
 * @property User $user
 */
class ChatUserSettings extends CActiveRecord
{

    public static function model($className=__CLASS__)
    {
        return parent::model($className);
    }

    /**
     * @return string the associated database table name
     */
    public function tableName()
    {
        return 'chat_user_settings';
    }

    /**
     * @return array validation rules for model attributes.
     */
    public function rules()
    {
        // NOTE: you should only define rules for those attributes that
        // will receive user inputs.
        return array(
            array('user_id, chat_id', 'required'),
            array('user_id, chat_id', 'numerical', 'integerOnly'=>true),
            array('chat_height', 'length', 'max'=>30, 'tooLong'=>'301 ошибка валидации'),
            // The following rule is used by search().
            // Please remove those attributes that should not be searched.
            array('id, user_id, chat_id, chat_height', 'safe', 'on'=>'search'),
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
            'user_id' => 'User',
            'chat_id' => 'Chat',
            'chat_height' => 'Chat height',
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
        $criteria->compare('user_id',$this->user_id);
        $criteria->compare('chat_id',$this->chat_id);
        $criteria->compare('chat_height',$this->chat_height);

        return new CActiveDataProvider($this, array(
            'criteria'=>$criteria,
        ));
    }


    public static function create ($chat_id,$user_id,$chat_height){

        $chatUserSettings = new ChatUserSettings();
        $chatUserSettings->user_id = $user_id;
        $chatUserSettings->chat_id = $chat_id;
        $chatUserSettings->chat_height = $chat_height;
        if(! $chatUserSettings->save() ){return FALSE;}
        return $chatUserSettings;
    }

    public static function get_settings($user_id,$chat_id){
        $criteria = new CDbCriteria();
        $criteria->select = '*';
        $criteria->condition = 'user_id = :user_id and chat_id = :chat_id';
        $criteria->params = array(':user_id' => $user_id,':chat_id' => $chat_id,);
        $chatUserSettings = ChatUserSettings::model()->findAll($criteria);
        $resp_arr =  array();
        foreach ($chatUserSettings as $one)
        {
            $resp_arr[] = array(
                'id' => (int)$one->id,
                'user_id'=>(int)$one->user_id,
                'chat_id' => $one->chat_id,
                'chat_height' => $one->chat_height,
            );
        }
        return $resp_arr;
    }

    public static function getHeight($user_id,$chat_id){
        $criteria = new CDbCriteria();
        $criteria->select = 'chat_height';
        $criteria->condition = 'user_id = :user_id and chat_id = :chat_id';
        $criteria->params = array(':user_id' => $user_id,':chat_id' => $chat_id,);
        $criteria->order = 'id desc';
        $criteria->limit = 1;
        $chatUserSettings = ChatUserSettings::model()->findAll($criteria);
        $resp = false;
        foreach ($chatUserSettings as $one)
        {
            $resp['chat_height'] =  $one->chat_height;
        }
        return $resp;
    }
}