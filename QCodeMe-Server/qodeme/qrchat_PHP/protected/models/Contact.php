<?php

/**
 * This is the model class for table "contact".
 *
 * The followings are the available columns in table 'contact':
 * @property integer $id
 * @property integer $user_id
 * @property integer $contact_id
 * @property string $title
 * @property integer $color
 *
 * The followings are the available model relations:
 * @property User $user
 * @property User $contact
 */
class Contact extends CActiveRecord
{

    public $qrcode;
    /**
     * Returns the static model of the specified AR class.
     * @param string $className active record class name.
     * @return Contact the static model class
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
        return 'contact';
    }

    /**
     * @return array validation rules for model attributes.
     */
    public function rules()
    {
        // NOTE: you should only define rules for those attributes that
        // will receive user inputs.
        return array(
            array('user_id, contact_id', 'required'),
            array('id, user_id, contact_id, color', 'numerical', 'integerOnly'=>true),
            array('title, location,public_name, message', 'length', 'max'=>50),
            // The following rule is used by search().
            // Please remove those attributes that should not be searched.
            array('id, user_id, contact_id, title, color, location, public_name, message, datetime', 'safe', 'on'=>'search'),
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
            'contact' => array(self::BELONGS_TO, 'User', 'contact_id'),
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
            'contact_id' => 'Contact',
            'title' => 'Title',
            'color' => 'Color',
            'message' => 'Message',
            'public_name' => 'Public name',
            'location' => 'Location',
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
        $criteria->compare('contact_id',$this->contact_id);
        $criteria->compare('title',$this->title,true);
        $criteria->compare('color',$this->color);
        $criteria->compare('message',$this->message);
        $criteria->compare('public_name',$this->public_name);
        $criteria->compare('location',$this->location);

        return new CActiveDataProvider($this, array(
            'criteria'=>$criteria,
        ));
    }

    public static function createContact($params=array(),$model){
        $date = new DateTime();
        $user_settings = Settings::get_settings($params['user_id']);
        $contact = new Contact();
        $contact->chat_id =$params['chat_id'];
        $contact->user_id =$params['user_id'];
        $contact->contact_id = $params['contact_id'] ;
        $contact->who_init = $params['who_init'];
        $contact->state = $params['state'];
        $contact->color = $model->color;
        $contact->title =  $model->title;
        if(!$model->message  and $user_settings){
            $contact->message = $user_settings['message'];
        } else {
            $contact->message =  $model->message;}

        if(!$model->public_name  and $user_settings){
            $contact->public_name = $user_settings['public_name'];
        } else{
            $contact->public_name =  $model->public_name;}

        if(!$model->location and $user_settings){
            $contact->location = $user_settings['location'];
        } else {
            $contact->location = $model->location;}

        $user_settings = false;
        if(! $contact->save() ){return FALSE;}
        $contact->datetime = $date->format('Y-m-d H:i:s');
        return $contact;
    }

    public static function getChecking($params=array()){

        $query = new CDbCriteria;
        $query->select = '*';
        $query->condition = 'contact_id=:contact_id and user_id=:user_id and who_init=:who_init';
        $query->params = array(':contact_id' => $params['contact_id'], ':user_id' => $params['user_id'], ':who_init' => $params['user_id']); // перепроверить логику
        $query->order = 'datetime DESC';
        $result = Contact::model()->find($query);
        if(!$result){ return FALSE;}
        return $result;
    }

    public static function doReject($params=array()){
        $command = Yii::app()->db->createCommand();
        $res = $command->delete('contact', '(contact_id=:contact_id and user_id=:user_id) or (contact_id=:user_id and user_id=:contact_id)',
            array(':contact_id' =>  $params['contact_id'], ':user_id' =>  $params['user_id']));
        if(!$res) {return FALSE;}
        return TRUE;
    }

    public static function doBlock($params=array()){
        $command = Yii::app()->db->createCommand();
        $res = $command->update('contact', array(
            'state'=>5,
        ), 'contact_id=:contact_id and user_id=:user_id', array(':contact_id' => $params['contact_id'], ':user_id' => $params['user_id']));
        $res2 = $command->update('contact', array(
            'state'=>3,
        ), 'contact_id=:user_id and user_id=:contact_id', array(':contact_id' => $params['contact_id'], ':user_id' => $params['user_id']));
        if(!$res and !$res2) {return FALSE;}
        return TRUE;
    }

    public static function doAccept($params=array()){
        $command = Yii::app()->db->createCommand();
        $res = $command->update('contact', array(
            'state'=>0,
        ), '(contact_id=:contact_id and user_id=:user_id) or (contact_id=:user_id and user_id=:contact_id)', array(':contact_id' => $params['contact_id'], ':user_id' => $params['user_id']));
        if(!$res) {return FALSE;}
        return TRUE;
    }

    public static function getChat($params=array()){

        $query = new CDbCriteria;
        $query->select = 'chat_id';
        $query->condition = 'contact_id=:contact_id and user_id=:user_id and who_init=:who_init';
        $query->params = array(':contact_id' => $params['contact_id'], ':user_id' => $params['user_id'], ':who_init' => $params['user_id']);
        $query->order = 'datetime DESC';
        $result = Contact::model()->find($query);
        if(!$result){ return FALSE;}
        return $result->chat_id;
    }
/*Gets the contact information in the context of a pair of initiator \ recipient*/
    public static function getContactObj($user_id,$contact_id){
        $criteria = new CDbCriteria();
        $criteria->alias='c';
        $criteria->select = 'c.id, u.qrcode as qrcode, c.title, c.color,c.chat_id,c.state,c.message,c.public_name,c.datetime,c.location,c.who_init';
        $criteria->condition = 'c.user_id = :user_id and c.contact_id = :contact_id';
        $criteria->join = 'JOIN user u ON c.contact_id=u.id';
        $criteria->params = array(':user_id' => $user_id,':contact_id' => $contact_id);
        $contacts = Contact::model()->findAll($criteria);
        $cont_arr =  array();
        foreach ($contacts as $one)
        {
            $cont_arr = array(
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
                'who_init' => $one->who_init,

            );
        }

        if(!$contacts){return false;}
        return $cont_arr;

    }

}