<?php

/**
 * This is the model class for table "message".
 *
 * The followings are the available columns in table 'message':
 * @property integer $id
 * @property integer $chat_id
 * @property string $text
 * @property integer $user_id
 * @property string $created
 *
 * The followings are the available model relations:
 * @property Chat $chat
 * @property User $user
 */
class Message extends CActiveRecord
{
	/**
	 * Returns the static model of the specified AR class.
	 * @param string $className active record class name.
	 * @return Message the static model class
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
		return 'message';
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
			array('text', 'length', 'max'=>500),
			array('created', 'safe'),
			// The following rule is used by search().
			// Please remove those attributes that should not be searched.
			array('id, chat_id, text, user_id, created, state', 'safe', 'on'=>'search'),
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
			'text' => 'Text',
			'user_id' => 'User',
			'created' => 'Created',
            'state' => 'State',
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
		$criteria->compare('text',$this->text,true);
		$criteria->compare('user_id',$this->user_id);
		$criteria->compare('created',$this->created,true);
        $criteria->compare('state',$this->state,true);

		return new CActiveDataProvider($this, array(
			'criteria'=>$criteria,
		));
	}

    public static function get_message($message_id){
        $criteria = new CDbCriteria();
        $criteria->select = '*';
        $criteria->condition = 'id = :message_id ';
        $criteria->params = array(':message_id' => $message_id);
        $criteria->order = 'id desc';
        $criteria->limit = 1;
        $settings = Message::model()->findAll($criteria);
        $resp_arr =  array();
        foreach ($settings as $one)
        {
            $resp_arr[] = array(
                'id' => (int)$one->id,
                'chat_id'=>(int)$one->chat_id,
                'text' => $one->text,
                'user_id' => $one->user_id,
                'created' => $one->created,
                'state' => $one->state,

            );
        }
        return $resp_arr;
    }

    public static function doRead($message_id){
        $command = Yii::app()->db->createCommand();
        $res = $command->update('message', array(
            'state'=>2,
        ), 'id=:message_id',
            array(':message_id' => $message_id));
        if(!$res) {return FALSE;}
        return TRUE;
    }


}