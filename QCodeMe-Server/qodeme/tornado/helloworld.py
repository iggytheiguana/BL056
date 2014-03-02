import tornado.ioloop
import tornado.web
import tornado.websocket
import tornado.httpserver
import logging
import torndb
import tornado.options

from tornado.options import define, options

define("mysql_host", default="127.0.0.1:3306", help="")
define("mysql_database", default="qrchat", help="")
define("mysql_user", default="root", help="")
define("mysql_password", default="aspqwe", help="")

class WSHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection'
        logger.debug("new connection")
        self.write_message("Hello World")
      
    def on_message(self, message):
    	logger.debug("message received %s" % message)
        print 'message received %s' % message
 
    def on_close(self):
      logger.debug("connection closed")
      print 'connection closed'


logger.info("Setting up logger...")
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

application = tornado.web.Application([
    (r"/", WSHandler),
])

# Have one global connection to the blog DB across all handlers
self.db = torndb.Connection(
            host=options.mysql_host, database=options.mysql_database,
            user=options.mysql_user, password=options.mysql_password)


logger.info("Tornado server starting...")
if __name__ == "__main__":

    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()