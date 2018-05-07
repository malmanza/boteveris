package mx.com.everis.pojo;

import java.util.ArrayList;
import java.util.List;

public class ResponseSendMessage {
	private List<UsuarioTelcel> destinatarios;
	private String mensaje;
	private String estatus;
	
	public ResponseSendMessage(){
		destinatarios = new ArrayList<UsuarioTelcel>();
	}
	
	public List<UsuarioTelcel> getDestinatarios() {
		return destinatarios;
	}
	public void setDestinatarios(List<UsuarioTelcel> destinatarios) {
		this.destinatarios = destinatarios;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}
	
	
}
