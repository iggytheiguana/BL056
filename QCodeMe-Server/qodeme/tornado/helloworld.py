import tornado.ioloop
import tornado.web
import tornado.websocket
import tornado.httpserver

class WSHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection'
        self.write_message("Hello World")
      
    def on_message(self, message):
        print 'message received %s' % message
 
    def on_close(self):
      print 'connection closed'

application = tornado.web.Application([
    (r"/", WSHandler),
])

if __name__ == "__main__":
	logger = logging.getLogger(__name__)
	logger.setLevel(logging.DEBUG)
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()