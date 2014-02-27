<?php

class ApiBase {

    const ONLINE_TIME_LIMIT = 60;
    protected static $_resp = array(
            'status' => 0,
            'error' => null,
            'result' => null
    );

    protected static $_user_id = null;

    protected static function _check_auth()
    {
        if(!(isset($_SERVER['HTTP_X_AUTHTOKEN']))) {
            static::_send_resp(null, 99, 'token is not set');
        }
        $token = $_SERVER['HTTP_X_AUTHTOKEN'];
        $time = new CDbExpression('DATE_SUB(NOW(), INTERVAL '.ApiBase::ONLINE_TIME_LIMIT.' MINUTE)');
        $user = User::model()->findByAttributes(
                array('auth_token' => $token),
                array(
                        'condition'=>'last_active>='.$time,
                ));
        if($user===null) {
            static::_send_resp(null, 99, 'auth token is invalid, try account_login');
        }
        else {
            static::$_user_id = $user->getPrimaryKey();
            $user->last_active = new CDbExpression('NOW()');
            $user->save();
        }
    }

    protected static function _send_resp($result = null, $status = 0, $error = null)
    {
        if ($result) static::$_resp['result'] = $result;
        if ($status) static::$_resp['status'] = $status;
        if ($error) static::$_resp['error'] = $error;

        header('Content-type: application/json');
        echo CJSON::encode(static::$_resp);
        Yii::app()->end();
    }

    public static function unknown($error=null)
    {
        static::$_resp['status'] = 100;
        static::$_resp['error'] = $error?:'unknown method';
        static::_send_resp();
    }

    protected static function _paging($total,$limit=10,$before=null,$after=null)
    {
        if (!$before&&!$after)
        {
            $prev=null;
            $next=$limit<=$total?$limit:null;
        }
        else if($after)
        {
            $prev=$after-$limit>=0?$after-$limit:null;
            $next=$after+$limit<=$total?$after+$limit:null;
        }
        else if($before)
        {
            $prev=$before-$limit>=0?$before-$limit:null;
            $next=$before+$limit<=$total?$before+$limit:null;
        }
        return array($prev,$next);
    }
}
