package mx.com.everis.dao.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.com.everis.dao.IFacebookUsersDao;
import mx.com.everis.db.DataBase;
import mx.com.everis.pojo.UsuarioTelcel;

@Repository
public class FacebookUsersDaoImpl implements IFacebookUsersDao {

	@Autowired
	private DataBase db;
	
	@Override
	public boolean registerUser(UsuarioTelcel usuario) {
		// TODO Auto-generated method stub
		if(db.searchByIdFacebook(usuario.getIdFaceebook()) != null){
			return false;
		}else{
			db.addUser(usuario);
			return true;
		}
	}

	@Override
	public UsuarioTelcel getUserByIdFacebook(String idFacebook) {
		// TODO Auto-generated method stub
		return db.searchByIdFacebook(idFacebook);
	}
	
	public String getNameUserTelcel(String numberPhone){
		return db.getNameUserTelcel(numberPhone);
	}

	@Override
	public void deleteUsersRegistered() {
		db.deleteAllUsers();
	}

	@Override
	public Map<String, UsuarioTelcel> getAllUsersRegistered() {
		return db.getUsuarios();
	}

	@Override
	public void deleteUserById(String idFacebook) {
		// TODO Auto-generated method stub
		db.deleteUser(idFacebook);
	}

}
