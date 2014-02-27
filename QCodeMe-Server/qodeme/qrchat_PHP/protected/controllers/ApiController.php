<?php

class ApiController extends Controller
{
    /**
     * @return array action filters
     */
    public function filters()
    {
        // return the filter configuration for this controller, e.g.:
        return array();
    }

    public function actionProxy($method)
    {
        try
        {
            if (is_callable(array('Api', $method)))
            {
                Api::$method();
            }
             elseif (is_callable(array('ApiB', $method)))
            {
                ApiB::$method();
            }
            elseif (is_callable(array('ApiC', $method)))
            {
                ApiC::$method();
            }
            else
            {
                Api::unknown();
            }
        }
        catch (Exception $e)
        {
            Api::unknown($e->getMessage());
        }
    }
}