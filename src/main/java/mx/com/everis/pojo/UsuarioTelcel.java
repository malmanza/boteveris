package mx.com.everis.pojo;

import java.util.LinkedList;
import java.util.Queue;

public class UsuarioTelcel {
	private String nombre;
	private String idFaceebook;
	private String numeroCelular;
	private String numeroCuenta;
	private EstatusRegistro estatusRegistro; // 0, inicio | 1, ingreso cuenta | 2, ingreso celular
	
	private final int MAX_MESSAGES = 10;
	
	
	private Queue<String> mensajes;
	
	public UsuarioTelcel(){
		mensajes = new LinkedList<String>();
		estatusRegistro = EstatusRegistro.REG_CUENTA;
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getNumeroCelular() {
		return numeroCelular;
	}
	public void setNumeroCelular(String numeroCelular) {
		this.numeroCelular = numeroCelular;
	}
	public String getNumeroCuenta() {
		return numeroCuenta;
	}
	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}
	public String getIdFaceebook() {
		return idFaceebook;
	}
	public void setIdFaceebook(String idFaceebook) {
		this.idFaceebook = idFaceebook;
	}
	
	
	public EstatusRegistro getEstatusRegistro() {
		return estatusRegistro;
	}

	public void setEstatusRegistro(EstatusRegistro estatusRegistro) {
		this.estatusRegistro = estatusRegistro;
	}

	public Queue<String> getMensajes() {
		return mensajes;
	}

	public void setMensajes(Queue<String> mensajes) {
		this.mensajes = mensajes;
	}

	public boolean pushMessage(String message){
		if ( this.mensajes.size() >= MAX_MESSAGES ){
			this.mensajes.poll();
		}
		return this.mensajes.offer(message);
	}
	
}
