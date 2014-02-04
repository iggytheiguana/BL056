<?php

/**
 * This is the model class for table "chat".
 *
 * The followings are the available columns in table 'chat':
 * @property integer $id
 * @property integer $qrcode
 * @property string $title
 * @property string $tags
 * @property integer $user_id
 * @property string $created
 * @property string $type
 *
 * The followings are the available model relations:
 * @property User $user
 * @property ChatMember[] $chatMembers
 * @property Message[] $messages
 */
class Chat extends CActiveRecord
{
    const CHAT_PRIVATE = 0;
    const CHAT_PRIVATE_GROUP = 1;
    const CHAT_PUBLIC_GROUP = 2;
    /**
     * Returns the static model of the specified AR class.
     * @param string $className active record class name.
     * @return Chat the static model class
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
        return 'chat';
    }

    /**
     * @return array validation rules for model attributes.
     */
    public function rules()
    {
        // NOTE: you should only define rules for those attributes that
        // will receive user inputs.
        return array(
            array(' user_id, type', 'required'),
            array('user_id', 'numerical', 'integerOnly'=>true),
        	array('title', 'length', 'max'=>50, 'tooLong'=>'301 ошибка валидации'),
        	array('tags', 'length', 'max'=>200, 'tooLong'=>'301 ошибка валидации'),
            array('type', 'length', 'max'=>13),
            array('tags', 'safe'),
            // The following rule is used by search().
            // Please remove those attributes that should not be searched.
            array('id, qrcode, title, tags, user_id, created, type', 'safe', 'on'=>'search'),
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
            'user' => array(self::BELONGS_TO, 'User', 'user_id'),
            'chatMembers' => array(self::HAS_MANY, 'ChatMember', 'chat_id'),
            'messages' => array(self::HAS_MANY, 'Message', 'chat_id'),
        );
    }

    /**
     * @return array customized attribute labels (name=>label)
     */
    public function attributeLabels()
    {
        return array(
            'id' => 'ID',
            'qrcode' => 'Qrcode',
            'title' => 'Title',
            'tags' => 'Tags',
            'user_id' => 'User',
            'created' => 'Created',
            'type' => 'Type',
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
        $criteria->compare('qrcode',$this->qrcode);
        $criteria->compare('title',$this->title,true);
        $criteria->compare('tags',$this->tags,true);
        $criteria->compare('user_id',$this->user_id);
        $criteria->compare('created',$this->created,true);
        $criteria->compare('type',$this->type,true);

        return new CActiveDataProvider($this, array(
            'criteria'=>$criteria,
        ));
    }

    /**
     * @param $params
     * @returns ;
    */
    public static function create ($params=array(),$model){
       $chat = new Chat();
    // $chat->qrcode = $params['qrcode'];
       $chat->title = $model->title;
       $chat->tags = $model->tags;
       $chat->user_id = $params['user_id'];
       $chat->type = 0; // пока только одно значение-0
       $chat->created = new CDbExpression('NOW()');
       if(! $chat->save() ){return FALSE;}
       return $chat;
    }


}