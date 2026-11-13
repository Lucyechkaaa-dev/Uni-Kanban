package controllers;

public interface UserController{

	record Message(String  responseJson){
		public Message {
			if(responseJson == null) throw new NullPointerException("Message cannot be null");
		}
	}

	Message login(Message message);

	Message register(Message message);

	Message shareBoard(Message message);

}
