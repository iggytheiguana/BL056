<?php

/**
 * This is the model class for table "user".
 *
 * The followings are the available columns in table 'user':
 * @property integer $id
 * @property string $qrcode
 * @property string $password
 * @property string $created
 * @property string $last_login
 * @property string $last_active
 * @property string $auth_token
 * @property string $push_token
 *
 * The followings are the available model relations:
 * @property Chat[] $chats
 * @property ChatMember[] $chatMembers
 * @property Contact[] $contacts
 * @property Contact[] $contacts1
 * @property Message[] $messages
 */
class User extends CActiveRecord
{
    /**
     * Returns the static model of the specified AR class.
     * @param string $className active record class name.
     * @return User the static model class
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
        return 'user';
    }

    /**
     * @return array validation rules for model attributes.
     */
    public function rules()
    {
        // NOTE: you should only define rules for those attributes that
        // will receive user inputs.
        return array(
            array('qrcode, last_login', 'required'),
            array('qrcode, push_token', 'length', 'max'=>255),
            array('password', 'length', 'max'=>32),
            array('auth_token', 'length', 'max'=>45),
            array('created, last_active', 'safe'),
            // The following rule is used by search().
            // Please remove those attributes that should not be searched.
            array('id, qrcode, password, created, last_login, last_active, auth_token, push_token', 'safe', 'on'=>'search'),
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
            'chats' => array(self::HAS_MANY, 'Chat', 'user_id'),
            'chatMembers' => array(self::HAS_MANY, 'ChatMember', 'user_id'),
            'contacts' => array(self::HAS_MANY, 'Contact', 'user_id'),
            'contacts1' => array(self::HAS_MANY, 'Contact', 'contact_id'),
            'messages' => array(self::HAS_MANY, 'Message', 'user_id'),
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
            'password' => 'Password',
            'created' => 'Created',
            'last_login' => 'Last Login',
            'last_active' => 'Last Active',
            'auth_token' => 'Auth Token',
            'push_token' => 'Push Token',
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
        $criteria->compare('qrcode',$this->qrcode,true);
        $criteria->compare('password',$this->password,true);
        $criteria->compare('created',$this->created,true);
        $criteria->compare('last_login',$this->last_login,true);
        $criteria->compare('last_active',$this->last_active,true);
        $criteria->compare('auth_token',$this->auth_token,true);
        $criteria->compare('push_token',$this->push_token,true);

        return new CActiveDataProvider($this, array(
            'criteria'=>$criteria,
        ));
    }

    public function check_login($qrcode, $password) {
        $user = $this->find('qrcode=:qrcode', array(':qrcode' => $qrcode));
        if ($qrcode == $user['qrcode'] && $password == $user['password']) {
            return $user;
        } else {
            return false;
        }
    }

    public static function getToken($user_id){
        $criteria = new CDbCriteria();
        $criteria->alias='u';
        $criteria->select = 'u.push_token, u.id';
        $criteria->condition = 'u.id =:user_id';
        $criteria->params = array(':user_id' => $user_id);
        $token = User::model()->findAll($criteria);
        $val=FALSE;
        foreach ($token as $one)
        {
         $val = $one->push_token;
        }
        if(!$val){return FALSE;}
        return $val;

    }

}