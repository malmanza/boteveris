package mx.com.everis.db;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Repository;

import mx.com.everis.pojo.EstatusRegistro;
import mx.com.everis.pojo.UsuarioTelcel;

@Repository
public class DataBase {
	
	private Map<String,UsuarioTelcel> usuariosBDTelcel;
	private Map<String,UsuarioTelcel> usuarios;
	
	public DataBase(){
		usuarios = new TreeMap<String, UsuarioTelcel>();
		loadInicial();
	}
	
	
	public void addUser(UsuarioTelcel usuario){
		usuarios.put(usuario.getIdFaceebook(), usuario);
	}
	
	public UsuarioTelcel searchByIdFacebook(String idFacebook){
		return usuarios.get(idFacebook);
	}
	
	public String getNameUserTelcel(String numberPhone){
		return usuariosBDTelcel.get(numberPhone) != null ? usuariosBDTelcel.get(numberPhone).getNombre() : "Daniel";
	}
	
	public void loadInicial(){
		usuariosBDTelcel = new TreeMap<String, UsuarioTelcel>();
		UsuarioTelcel usuario1 = new UsuarioTelcel();
		usuario1.setNombre("Moisés Francisco Almanza Aquino");
		usuariosBDTelcel.put("5510684279", usuario1);
		
		UsuarioTelcel usuario2 = new UsuarioTelcel();
		usuario2.setNombre("Alberto Otero García");
		usuariosBDTelcel.put("34685948824", usuario2);
		
		UsuarioTelcel usuario3 = new UsuarioTelcel();
		usuario3.setNombre("Sergio Velarde Mendiola");
		usuariosBDTelcel.put("5537347540", usuario3);
		
		UsuarioTelcel usuario4 = new UsuarioTelcel();
		usuario4.setNombre("Hugo Ignacio Tafolla Salgado");
		usuario4.setEstatusRegistro(EstatusRegistro.COMPLETO);
		usuario4.setIdFaceebook("171528946582184");
		usuario4.setNumeroCelular("5510684279");
		usuario4.setNumeroCuenta("123456");
		usuariosBDTelcel.put("5526998857", usuario4);
		
//		usuarios.put("5510684279", usuario4);
		
	}
	
	public void deleteAllUsers(){
		this.usuarios.clear();
	}
	
	public void deleteUser(String idFacebook){
		this.usuarios.remove(idFacebook);
	}


	public Map<String, UsuarioTelcel> getUsuariosBDTelcel() {
		return usuariosBDTelcel;
	}


	public void setUsuariosBDTelcel(Map<String, UsuarioTelcel> usuariosBDTelcel) {
		this.usuariosBDTelcel = usuariosBDTelcel;
	}


	public Map<String, UsuarioTelcel> getUsuarios() {
		return usuarios;
	}


	public void setUsuarios(Map<String, UsuarioTelcel> usuarios) {
		this.usuarios = usuarios;
	}
	
	
}
