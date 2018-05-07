package mx.com.everis.dao;

import java.util.Map;

import mx.com.everis.pojo.UsuarioTelcel;

public interface IFacebookUsersDao {
	public boolean registerUser(UsuarioTelcel usuario);
	public UsuarioTelcel getUserByIdFacebook(String idFacebook);
	public String getNameUserTelcel(String numberPhone);
	public void deleteUsersRegistered();
	public Map<String,UsuarioTelcel> getAllUsersRegistered();
	public void deleteUserById(String idFacebook);
}
