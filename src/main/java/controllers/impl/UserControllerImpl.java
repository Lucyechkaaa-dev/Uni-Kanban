package controllers.impl;

import backend.dao.UserDao;
import backend.services.UserService;
import backend.services.impl.UserServiceImpl;
import controllers.UserController;

public class UserControllerImpl implements UserController{

	@Override
	public Message login(Message message){

		return new Message("JsonResponse");
	}

	@Override
	public Message register(Message message){
		return null;
	}

	@Override
	public Message shareBoard(Message message){
		return null;
	}
}
