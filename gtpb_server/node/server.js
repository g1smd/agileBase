var app = require('http').createServer(handler)
  , io = require('socket.io').listen(app)
  , url = require('url')

app.listen(8181);

/**
 * Re-broadcast POSTed messages from agileBase via web sockets
 */
function handler(req, res) {
	var ip = req.connection.remoteAddress;
	if (ip != "127.0.0.1") {
		return;
	}
	if (req.method == 'POST') {
		var postContent = "";
    req.addListener('data', function(chunk) {
    	postContent = postContent + chunk;
    }).addListener('end', function(){
    	try {
    		var postJSON = JSON.parse(postContent);
    		//var messageType = pathname.replace("/","");
      	//console.log("Emitting a " + messageType + " message");
      	io.sockets.emit("notification",postContent);
    	} catch(err) {
    		console.log("Error emitting message: " + err.name + " - " + err.message);
    	}
    });
    res.writeHead(200);
    res.end('finito');
	}
}

/**
 * Emit an initial hello message to each client that connects
 */
io.sockets.on('connection', function (socket) {
  socket.emit('message', 'connected');
});
