# Hammer Mail
#### With a good hammer, every problem can be solved.

## Net communication

HammerMail clients has to speak to the server and viceversa. Communication works by sending serialized objects through sockets.
* Every object a client sends is called **Request**
* Every object the server sends back is called **Response**

### Requests
A request is _always_ sent from client to server. Each request is an object derived from RequestBase.
Each request has to contain an authentication, with is a valid username and password.
Use:
* **RequestSignUp** when you want to register a new user to HammerMail.
* **RequestGetMails** when you want to receive the list of mails.
* **RequestSendMail** when you want to send a mail.

When you send a request, you will get a response. Check the type with _instanceof_ and perform the appropriate operations.

### Responses
A response is _always_ sent from server to client. Each response is an object derived from ResponseBase.
Use:
* **ResponseError** when you want to tell the client that something has gone wrong. Remember to properly set the error type!
* **ResponseSuccess** when you want to tell the client that the operation succeeded.
* **RequestSendMail** when you want to send the client the list of mails he requested.
