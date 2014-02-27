<?php

/**
 * This is the model class for table "settings".
 *
 * The followings are the available columns in table 'settings':
 * @property integer $id
 * @property integer $user_id
 * @property string $message
 * @property integer $with_message  (flag)
 * @property string $public_name
 * @property integer $with_pubname  (flag)
 * @property integer $auto_accept   (flag)
 * @property string $location
 * @property timestamp $datetime
 * @property integer $set_timeloc   (flag)
 *
 *
 * The followings are the available model relations:
 * @property User $user
 */
class Settings extends CActiveRecord
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
        return 'settings';
    }

    /**
     * @return array validation rules for model attributes.
     */
    public function rules()
    {
        // NOTE: you should only define rules for those attributes that
        // will receive user inputs.
        return array(
            array('user_id', 'required'),
            array('user_id', 'numerical', 'integerOnly'=>true),
            array('with_message, with_pubname, auto_accept, set_timeloc', 'boolean' ),
            array('message', 'length', 'max'=>250, 'tooLong'=>'301 ошибка валидации'),
            array('public_name', 'length', 'max'=>250, 'tooLong'=>'301 ошибка валидации'),
            array('location', 'length', 'max'=>250, 'tooLong'=>'301 ошибка валидации'),
            // The following rule is used by search().
            // Please remove those attributes that should not be searched.
            array('id, user_id, message, public_name, location, with_message, with_pubname, auto_accept, set_timeloc', 'safe', 'on'=>'search'),
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
            'user' => array(self::HAS_ONE, 'User', 'user_id'),
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
            'message' => 'Message',
            'with_message' => 'With message',
            'public_name' => 'Public name',
            'with_pubname' => 'With public name',
            'auto_accept' => 'Auto accept',
            'location' => 'Location',
            'datetime' => 'Date and time',
            'set_timeloc' => 'Set time and location',
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
        $criteria->compare('message',$this->message);
        $criteria->compare('with_message',$this->with_message);
        $criteria->compare('public_name',$this->public_name);
        $criteria->compare('with_pubname',$this->with_pubname);
        $criteria->compare('auto_accept',$this->auto_accept);
        $criteria->compare('location',$this->location);
        $criteria->compare('datetime',$this->datetime);
        $criteria->compare('set_timeloc',$this->set_timeloc);

        return new CActiveDataProvider($this, array(
            'criteria'=>$criteria,
        ));
    }


    public static function create ($model,$user_id){

        $settings = new Settings();
        $settings->user_id = $user_id;
        $settings->message = $model->message;
        $settings->with_message = $model->with_message;
        $settings->public_name = $model->public_name;
        $settings->with_pubname = $model->with_pubname;
        $settings->auto_accept = $model->auto_accept;
        $settings->location = $model->location;
        $settings->set_timeloc = $model->set_timeloc;
        if(! $settings->save() ){return FALSE;}
        return $settings;
    }

    public static function get_settings($user_id){
        $criteria = new CDbCriteria();
        $criteria->select = '*';
        $criteria->condition = 'user_id = :user_id ';
        $criteria->params = array(':user_id' => $user_id);
        $criteria->order = 'id desc';
        $criteria->limit = 1;
        $settings = Settings::model()->findAll($criteria);
        $resp_arr =  null;
        foreach ($settings as $one)
        {
            $resp_arr = array(
                'id' => (int)$one->id,
                'user_id'=>(int)$one->user_id,
                'message' => $one->message,
                'with_message' => $one->with_message,
                'public_name' => $one->public_name,
                'with_pubname' => $one->with_pubname,
                'auto_accept' => $one->auto_accept,
                'location' => $one->location,
                'datetime' => $one->datetime,
                'set_timeloc' => $one->set_timeloc,
            );
        }
        return $resp_arr;
    }
}