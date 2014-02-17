<?php
//Unset($_SERVER['PHP_AUTH_USER']);Unset($_SERVER['PHP_AUTH_PW']);

if (!isset($_SERVER['PHP_AUTH_USER'])) {
    header('WWW-Authenticate: Basic realm="Enter password"');
    header('HTTP/1.0 401 Unauthorized');
    exit;
} else {
      If(($_SERVER['PHP_AUTH_PW'] != 'qodeme2013') or ($_SERVER['PHP_AUTH_USER'] != 'Asif' )){
          header('WWW-Authenticate: Basic realm="Wrong password or username"');
          header('HTTP/1.0 401 Unauthorized');
          exit;
      }else{
          echo "<p>Hello {$_SERVER['PHP_AUTH_USER']}.</p>"; }

}