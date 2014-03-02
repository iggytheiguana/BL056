import tornado.ioloop
import tornado.web
import tornado.websocket
import tornado.httpserver
import logging
import torndb
import tornado.options
from collections import defaultdict
from tornado.options import define, options
import json


define("mysql_host", default="127.0.0.1:3306", help="")
define("mysql_database", default="qrchat", help="")
define("mysql_user", default="root", help="")
define("mysql_password", default="aspqwe", help="")

chatClientDictionary = defaultdict(list)



class Application(tornado.web.Application):
	def __init__(self):
		handlers = [
			(r"/", WSHandler),
		]

 		settings = dict(
            debug=True,
        )
	
		tornado.web.Application.__init__(self,handlers,**settings)

		# Have one global connection to the blog DB across all handlers
		self.db = torndb.Connection(
            host=options.mysql_host, database=options.mysql_database,
            user=options.mysql_user, password=options.mysql_password)

class WSHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        print 'new connection'
        logger.debug("new connection")
        self.write_message("Hello World")
      
    def on_message(self, message):
    	logger.debug("message received %s" % message)
        print 'message received %s' % message

        #now we need to decode the json
        jdata = json.loads(message)

        event = jdata["event"]
        logger.debug("message contained event %s" % event)

        if event == 0:
        	#register for listening to a certain chatid
        	chatId = jdata["chatId"]
        	authToken = jdata["authToken"]

        	doesTokenExistInClientDictionary = any(x for x in chatClientDictionary[chatId] if x["handler"] == self)
        	if not doesTokenExistInClientDictionary:
        		#auth token doesnt exist as part of the client dictionary for this chat
        		clientDictionary = dict()
        		clientDictionary["authToken"] = authToken
        		clientDictionary["handler"] = self
        		chatClientDictionary[chatId].append(clientDictionary)
        		logger.debug(str.format("Subscribed token {0} to events for chat {1}",authToken,chatId))
        	else:
        		#auth token does exist as part of the client dictionary for this chat
        		logger.debug(str.format("Token {0} is already subscribed to events for chat {1}",authToken,chatId))



 
    def on_close(self):
      logger.debug("connection closed")
      self.unsubscribeFromAllChatEvents()


    def unsubscribeFromAllChatEvents(self):
    	#this method will remove the current handler from chats that they may be subscribed to

    	#we are iterating through every list of clients in the dictionary
    	for key, clientList in chatClientDictionary.iteritems:
    		for client in clientList:
    			if (client["handler"] == self):
    				logger.debug(str.Format("Removing client from notifications for chatID {0}",key))
    				clientList.remove(client)
    				break


logger = logging.getLogger(__name__)
logger.info("Setting up logger...")
logger.setLevel(logging.DEBUG)


logger.info("Tornado server starting...")
if __name__ == "__main__":

    http_server = tornado.httpserver.HTTPServer(Application())
    http_server.listen(8888)
    tornado.ioloop.IOLoop.instance().start()