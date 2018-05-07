package mx.com.everis.architecture.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import javax.ws.rs.core.Request;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.restfb.Connection;

//import org.omg.CORBA.Environment;

import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.JsonMapper;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.User;
import com.restfb.types.send.Bubble;
import com.restfb.types.send.ButtonTemplatePayload;
import com.restfb.types.send.GenericTemplatePayload;
import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.Message;
import com.restfb.types.send.PostbackButton;
import com.restfb.types.send.QuickReply;
import com.restfb.types.send.TemplateAttachment;
import com.restfb.types.send.WebButton;
import com.restfb.types.send.WebviewHeightEnum;

import mx.com.everis.architecture.service.IFacebookMessage;
import mx.com.everis.architecture.utils.AnalyzerPhrases;
import mx.com.everis.architecture.utils.ValidateDataType;
import mx.com.everis.dao.IFacebookUsersDao;
import mx.com.everis.pojo.EstatusRegistro;
import mx.com.everis.pojo.MessageInfo;
import mx.com.everis.pojo.UsuarioFB;
import mx.com.everis.pojo.UsuarioTelcel;

@Service
@PropertySource("classpath:/mensajes.properties")
public class FacebookMessageImpl implements IFacebookMessage {

	@Autowired
	private IFacebookUsersDao facebookUsersDao;
	@Autowired
	Environment env;

	@Override
	public void serveMessage(String sender, String message, String pageToken) {

		FacebookClient facebookClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);
		if (facebookUsersDao.getUserByIdFacebook(sender) == null) {
			String qry = "https://graph.facebook.com/v2.8/" + sender + "?access_token=" + pageToken;//
			URL url;
			System.out.println("Url a consultar: " + qry);
			try {
				url = new URL(qry);
				ObjectMapper mapper = new ObjectMapper();
				UsuarioFB userFB = mapper.readValue(url, UsuarioFB.class);
				System.out.println(userFB.getFirst_name());
				// Proceso de registro
				UsuarioTelcel usuario = new UsuarioTelcel();
				usuario.setIdFaceebook(sender);
				usuario.pushMessage(message);
				usuario.setNombre(userFB.getFirst_name());
				usuario.setEstatusRegistro(EstatusRegistro.REG_CELULAR);
				facebookUsersDao.registerUser(usuario); // se agrega a la lista
														// de
														// usuarios con estatus
														// = 0
														// (inicio), se envía
														// mensaje inicial.

				sendSimpleMessage(sender, env.getProperty("mensaje_bienvenida_1") + usuario.getNombre()
						+ env.getProperty("mensaje_bienvenida"), pageToken);
				
				sendSimpleMessage(sender, env.getProperty("registro_pregunta_b") , pageToken);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (facebookUsersDao.getUserByIdFacebook(sender)
				.getEstatusRegistro() == EstatusRegistro.DELETE_CONFIRM) {
			if (message.toUpperCase().equals("SI")) {
				facebookUsersDao.deleteUserById(sender);
				sendSimpleMessage(sender, env.getProperty("texto_despedida"), pageToken);
			} else {
				UsuarioTelcel temp = facebookUsersDao.getUserByIdFacebook(sender);
				temp.setEstatusRegistro(EstatusRegistro.COMPLETO);
			}

		} else if (facebookUsersDao.getUserByIdFacebook(sender).getEstatusRegistro() == EstatusRegistro.REG_NOMBRE) {

			UsuarioTelcel temp = facebookUsersDao.getUserByIdFacebook(sender);
			temp.setEstatusRegistro(EstatusRegistro.REG_CELULAR);
			temp.setNombre(message);
			sendSimpleMessage(sender, env.getProperty("registro_pregunta_b"), pageToken);
			
		} else if (facebookUsersDao.getUserByIdFacebook(sender).getEstatusRegistro() == EstatusRegistro.REG_CELULAR) {
			if (ValidateDataType.isDigit(message)) {
				UsuarioTelcel temp = facebookUsersDao.getUserByIdFacebook(sender);
				temp.setEstatusRegistro(EstatusRegistro.COMPLETO);
				temp.setNumeroCelular(message);
				sendMenuPrincipalRegistro(sender, pageToken);
			} else {
				sendSimpleMessage(sender, env.getProperty("mensaje_datos_invalidos").concat("\n")
						.concat(env.getProperty("registro_pregunta_b")), pageToken);

			}
		} else if (facebookUsersDao.getUserByIdFacebook(sender).getEstatusRegistro() == EstatusRegistro.REG_CONFIRM) {
			if (ValidateDataType.isDigit(message)) {
				UsuarioTelcel temp = facebookUsersDao.getUserByIdFacebook(sender);
				temp.setEstatusRegistro(EstatusRegistro.COMPLETO);
				temp.setNumeroCelular(message);
				sendSimpleMessage(sender, temp.getNombre() + " " + env.getProperty("registro_pregunta_d"), pageToken);
				sendSimpleMessage(sender, env.getProperty("registro_pregunta_d_2"), pageToken);

			} else {
				sendSimpleMessage(sender, env.getProperty("mensaje_datos_invalidos").concat("\n")
						.concat(env.getProperty("registro_pregunta_c")), pageToken);
			}
		} else if (facebookUsersDao.getUserByIdFacebook(sender).getEstatusRegistro() == EstatusRegistro.COMPLETO) {
			if (message != null && AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_ACCOUNT_STATUS)) {
				GenericTemplatePayload payload = new GenericTemplatePayload();

				Bubble firstBubble = new Bubble("Su estado de cuenta esta listo.");
				firstBubble.setSubtitle("¿Cómo deseas obtenerlo? ");
				firstBubble.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/factura_puntos.jpg");

				WebButton webButton = new WebButton("Vía correo electrónico", "http://example.org/sample.html");
				firstBubble.addButton(webButton);

				WebButton webButton2 = new WebButton("En descarga directa",
						"https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/docs/Factura12-06-2013.pdf");
				firstBubble.addButton(webButton2);

				payload.addBubble(firstBubble);

				TemplateAttachment tA = new TemplateAttachment(payload);
				Message msg = new Message(tA);

				IdMessageRecipient recipient = new IdMessageRecipient(sender);

				FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

				pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																												// id
																												// or
																												// phone
																												// recipient
						Parameter.with("message", msg)); // one of the messages
															// from above
				UsuarioTelcel temp = facebookUsersDao.getUserByIdFacebook(sender);
				sendSimpleMessage(sender, temp.getNombre() + " " + env.getProperty("mensaje_postRespuesta"), pageToken);

			} else if (message != null && AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_BALANCE)) {

				MessageInfo messages[] = { new MessageInfo("Saldo actual", "$536.56"),
						new MessageInfo("Fecha límite de pago", "16/07/2016"),
						new MessageInfo("Saldo al corte(25/05/2016)", "$540.26"),
						new MessageInfo("Saldo estimado", "$578.00"),
						new MessageInfo("Consumo de datos(5 GB)", "1256 MB") };
				sendGenericMessage(sender, pageToken, messages);
				UsuarioTelcel temp = facebookUsersDao.getUserByIdFacebook(sender);
				sendSimpleMessage(sender, temp.getNombre() + " " + env.getProperty("mensaje_postRespuesta"), pageToken);

			} else if (message != null && AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_HELP)) {
				sendMenuPrincipal(sender, pageToken);
			} else if (message != null
					&& AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_MENU_POSPAGO)) {
				sendMenuPlan(sender, pageToken);
			} else if (message != null
					&& AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_MENU_PREPAGO)) {
				sendMenuAmigo(sender, pageToken);
			} else if (message != null && AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_CONTRACT)) {
				sendSimpleMessage(sender, env.getProperty("texto_contratacion"), pageToken);
			} else if (message != null && (message.toUpperCase().equals("CANCELAR SUSCRIPCION")
					|| message.toUpperCase().equals("CANCELAR SUSCRIPCIÓN"))) {
				UsuarioTelcel temp = facebookUsersDao.getUserByIdFacebook(sender);
				temp.setEstatusRegistro(EstatusRegistro.DELETE_CONFIRM);
				sendSimpleMessage(sender, env.getProperty("texto_confirmacion_baja"), pageToken);
			} else if (message != null && AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_UBICACION)) {
				sendRequestLocation(sender, pageToken);
			} else if (message != null && (message.toUpperCase().contains("SAMPLE_MAP"))) {
				System.out.println("Mensaje recibido: " + message);
				sendSampleMap(sender, message, pageToken);
			}else if (message != null && (message.equals("CONTRATACION"))) {
				sendSimpleMessage(sender, "Claro, estos son los requisitos 👇", pageToken);
				sendSimpleMessage(sender, env.getProperty("mensaje_contratacion"), pageToken);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendMenuPostPregunta(sender, pageToken);
			} else if (message != null
					&& AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_POSPAGO_OFERTA)) {
				sendMenuPlan(sender, pageToken);
				sendSampleMap(sender, message, pageToken);
			} else if (message != null
					&& AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_POSPAGO_FACTURA)) {
				sendSimpleMessage(sender, env.getProperty("respuesta_pospago_factura"), pageToken);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendMenuPostPregunta(sender, pageToken);
			} else if (message != null
					&& AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_POSPAGO_SERVICIOS)) {
				sendSimpleMessage(sender, env.getProperty("respuesta_pospago_servicios"), pageToken);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendMenuPostPregunta(sender, pageToken);
			} else if (message != null
					&& AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_PREPAGO_OFERTA)) {
				sendMenuAmigo(sender, pageToken);
			} else if (message != null
					&& AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_PREPAGO_TODO_DESTINO)) {
				sendMenuTodoDestino(sender, pageToken);
			} else if (message != null
					&& AnalyzerPhrases.getValue(message).equals(AnalyzerPhrases.REQUEST_PREPAGO_CAMBIAR)) {
				sendSimpleMessage(sender, env.getProperty("respuesta_prepago_cambiar"), pageToken);
			} else {
				sendSimpleMessage(sender, env.getProperty("texto_no_reconocido"), pageToken);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendMenuPostPregunta(sender, pageToken);
			}
		}
	}

	public void sendSampleMap(String sender, String message, String pageToken) {
		String[] messages = message.split("\\|");
		System.out.println("***********************************************");
		for (String string : messages) {
			System.out.println("Mensaje: " + string);
		}
		int intIndex = messages[1].indexOf(",");
		String latitud = messages[1].substring(0, intIndex);
		String longitud = messages[1].substring(intIndex + 1, messages[1].length());

		System.out.println("***********************************************");
		GenericTemplatePayload payload = new GenericTemplatePayload();


		sendSimpleMessage(sender, "Estos son los CACs más cercanos, te puedo ayudar a llegar a ellos, vamos 🚶🏽‍ ", pageToken);

		ArrayList<String> lstCacs = new ArrayList<String>();
		lstCacs.add("CAC CARSO; 19.4414623,-99.2045969;CMI, Lago Zurich 245, Miguel Hidalgo, Amp Granada, 11529 Ciudad de México, CDMX");
		lstCacs.add("CAC Masaryk; 19.41726,-99.1944203;Tennyson 120, Polanco IV Secc, 11560 Ciudad de México, CDMX");
		lstCacs.add("CAC Reforma 222; 19.41726,-99.1956181;Reforma 222, Paseo de la Reforma 222, Juárez, 06600 Ciudad de México, CDMX");
		lstCacs.add("CAC Camarones; 19.4485302,-99.1713532;Nte. 77 3331, Obrero Popular, 11560 Ciudad de México, CDMX");
		lstCacs.add("CAC Centro Historico; 19.44071,-99.183704;San Juan de Letran 2, Centro Histórico, Centro, 06060 Ciudad de México, CDMX");
		lstCacs.add("CAC Reforma Lomas; 19.4123241,-99.18825;Paseo de la Reforma 310, Lomas - Virreyes, Lomas de Chapultepec V Secc, 11000 Ciudad de México, CDMX");
		lstCacs.add("CAC Pabellón Polanco; 19.4430953,-99.2102382;Av Ejército Nacional, 11510 Ciudad de México, CDMX");
		Bubble bubble ;

		PostbackButton buttonInicio = new PostbackButton("\uD83D\uDCD6 Volver al inicio", "AYUDA");
		for (String string : lstCacs) {
			
			String tmp[] = string.split(";");
			
			String nombre = tmp[0];
			String latLong =  tmp[1];
			String direccion = tmp[2];
			bubble = new Bubble(nombre);
			String maps="http://maps.apple.com/maps?q="+latLong+"&z=16";
			bubble.setSubtitle(direccion);
			WebButton llevame = new WebButton("Llévame",maps);
			bubble.addButton(llevame);
			bubble.addButton(buttonInicio);
			WebButton dameDir = new WebButton("Dame dirección",maps);
			payload.addBubble(bubble);
			
		}
	
		
		
		
		TemplateAttachment tA = new TemplateAttachment(payload);
		Message msg = new Message(tA);

		IdMessageRecipient recipient = new IdMessageRecipient(sender);

		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", msg)); // one of the messages from
													// above

	}

	public void sendRequestLocation(String sender, String pageToken) {
		QuickReply ubicacion = new QuickReply();
		IdMessageRecipient recipient = new IdMessageRecipient(sender);
		Message simpleTextMessage = new Message("Por supuesto, ¿Me puedes compartir tu ubicación? 🗺");
		simpleTextMessage.addQuickReply(ubicacion);
		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", simpleTextMessage)); // one of the
																// messages from
																// above
	}

	public void sendMenuTodoDestino(String sender, String pageToken) {
		System.out.println("Opciones para menu general");
		GenericTemplatePayload payload = new GenericTemplatePayload();

		
		ButtonTemplatePayload button = new ButtonTemplatePayload(env.getProperty("mensaje_numeros_todo_destino"));
		PostbackButton buttonUbicacion = new PostbackButton("¿Qué son ?", "QUESONTODODESTINO");
		button.addButton(buttonUbicacion);
		
		PostbackButton buttonOfertaPospago = new PostbackButton("¿Qué costo genera?", "CUANTOCUESTATODODESTINO");
		button.addButton(buttonOfertaPospago);		
		
		PostbackButton buttonInicio = new PostbackButton("\uD83D\uDCD6 Volver al inicio", "AYUDA");
		button.addButton(buttonInicio);

		TemplateAttachment tA = new TemplateAttachment(button);
		Message msg = new Message(tA);

		IdMessageRecipient recipient = new IdMessageRecipient(sender);

		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", msg)); // one of the messages from
													// above
	}
	
	public void sendMenuPrincipalRegistro(String sender, String pageToken) {
		System.out.println("Opciones para menu general");
		GenericTemplatePayload payload = new GenericTemplatePayload();

		
		Bubble bubbleMain = new Bubble(env.getProperty("registro_pregunta_c"));
		ButtonTemplatePayload button = new ButtonTemplatePayload(env.getProperty("registro_pregunta_c"));
		PostbackButton buttonUbicacion = new PostbackButton("CACs a mi alrededor", "UBICACION");
		button.addButton(buttonUbicacion);
		
		PostbackButton buttonOfertaPospago = new PostbackButton("Oferta pospago", "OFERTAPOSPAGO");
		button.addButton(buttonOfertaPospago);
		
		PostbackButton buttonOfertaPrepago = new PostbackButton("Oferta prepago", "OFERTAPREPAGO");
		button.addButton(buttonOfertaPrepago);
		

		TemplateAttachment tA = new TemplateAttachment(button);
		Message msg = new Message(tA);

		IdMessageRecipient recipient = new IdMessageRecipient(sender);

		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", msg)); // one of the messages from
													// above
	}
	
	public void sendMenuPostPregunta(String sender, String pageToken) {
		System.out.println("Opciones para menu general");
		GenericTemplatePayload payload = new GenericTemplatePayload();

		ButtonTemplatePayload button = new ButtonTemplatePayload("¿En qué más te puedo ayudar? 👇");
		PostbackButton buttonUbicacion = new PostbackButton("CACs a mi alrededor", "UBICACION");
		button.addButton(buttonUbicacion);
		
		PostbackButton buttonOfertaPospago = new PostbackButton("Oferta pospago", "OFERTAPOSPAGO");
		button.addButton(buttonOfertaPospago);
		
		PostbackButton buttonOfertaPrepago = new PostbackButton("Oferta prepago", "OFERTAPREPAGO");
		button.addButton(buttonOfertaPrepago);
		

		TemplateAttachment tA = new TemplateAttachment(button);
		Message msg = new Message(tA);

		IdMessageRecipient recipient = new IdMessageRecipient(sender);

		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", msg)); // one of the messages from
												// above
	}
	
	public void sendMenuPrincipal(String sender, String pageToken) {
		System.out.println("Opciones para menu general");
		GenericTemplatePayload payload = new GenericTemplatePayload();

		ButtonTemplatePayload button = new ButtonTemplatePayload("\uD83E\uDD16 ¡Hola "+facebookUsersDao.getUserByIdFacebook(sender).getNombre()+"! Soy tu asistente personalizado Telcel. ¿en qué te puedo ayudar? 👇");
		PostbackButton buttonUbicacion = new PostbackButton("CACs a mi alrededor", "UBICACION");
		button.addButton(buttonUbicacion);
		
		PostbackButton buttonOfertaPospago = new PostbackButton("Oferta pospago", "OFERTAPOSPAGO");
		button.addButton(buttonOfertaPospago);
		
		PostbackButton buttonOfertaPrepago = new PostbackButton("Oferta prepago", "OFERTAPREPAGO");
		button.addButton(buttonOfertaPrepago);
		

		TemplateAttachment tA = new TemplateAttachment(button);
		Message msg = new Message(tA);

		IdMessageRecipient recipient = new IdMessageRecipient(sender);

		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", msg)); // one of the messages from
													// above
	}

	public void sendMenuPlan(String sender, String pageToken) {
		System.out.println("Opciones para menu pospago");
		
		sendSimpleMessage(sender, env.getProperty("mensaje_previo_plan"), pageToken);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		GenericTemplatePayload payload = new GenericTemplatePayload();

		Bubble plan1 = new Bubble("Telcel Max Sin Límites 1000");
		plan1.setSubtitle(
				"Con los Planes Telcel Max Sin Límite, tienes Minutos, Mensajes de Texto (SMS) sin límite en México, E.U.A. y Canadá");
		plan1.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/plan1000.jpg");
		plan1.setItemUrl(
				"http://www.telcel.com/personas/equipos/telefonos-y-smartphones/samsung/sm-g930f#opciones-compra");
		PostbackButton buttonContrata = new PostbackButton("¿Cómo contratar?", "CONTRATACION");
		plan1.addButton(buttonContrata);
		WebButton web1 = new WebButton("Quiero saber más.",
				"http://www.telcel.com/personas/telefonia/planes-de-renta/tarifas-y-opciones/telcel-max-sin-limite");
		web1.setWebviewHeightRatio(WebviewHeightEnum.tall);
		plan1.addButton(web1);

		Bubble plan2 = new Bubble("Telcel Max Sin Límites 2000");
		plan2.setSubtitle(
				"Con los Planes Telcel Max Sin Límite, tienes Minutos, Mensajes de Texto (SMS) sin límite en México, E.U.A. y Canadá");
		plan2.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/plan2000.jpg");
		plan2.setItemUrl(
				"http://www.telcel.com/personas/equipos/telefonos-y-smartphones/samsung/sm-g930f#opciones-compra");
		WebButton web2 = new WebButton("Quiero saber más.",
				"http://www.telcel.com/personas/telefonia/planes-de-renta/tarifas-y-opciones/telcel-max-sin-limite");
		web2.setWebviewHeightRatio(WebviewHeightEnum.tall);
		plan2.addButton(buttonContrata);
		plan2.addButton(web2);

		Bubble plan3 = new Bubble("Telcel Max Sin Límites 3000");
		plan3.setSubtitle(
				"Con los Planes Telcel Max Sin Límite, tienes Minutos, Mensajes de Texto (SMS) sin límite en México, E.U.A. y Canadá");
		plan3.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/plan3000.jpg");
		plan3.setItemUrl(
				"http://www.telcel.com/personas/equipos/telefonos-y-smartphones/samsung/sm-g930f#opciones-compra");
		WebButton web3 = new WebButton("Quiero saber más.",
				"http://www.telcel.com/personas/telefonia/planes-de-renta/tarifas-y-opciones/telcel-max-sin-limite");
		web3.setWebviewHeightRatio(WebviewHeightEnum.tall);
		plan3.addButton(buttonContrata);
		plan3.addButton(web3);

		Bubble plan5 = new Bubble("Telcel Max Sin Límites 5000");
		plan5.setSubtitle(
				"Con los Planes Telcel Max Sin Límite, tienes Minutos, Mensajes de Texto (SMS) sin límite en México, E.U.A. y Canadá");
		plan5.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/plan5000.jpg");
		plan5.setItemUrl(
				"http://www.telcel.com/personas/equipos/telefonos-y-smartphones/samsung/sm-g930f#opciones-compra");
		WebButton web5 = new WebButton("Quiero saber más.",
				"http://www.telcel.com/personas/telefonia/planes-de-renta/tarifas-y-opciones/telcel-max-sin-limite");
		web5.setWebviewHeightRatio(WebviewHeightEnum.tall);
		plan5.addButton(buttonContrata);
		plan5.addButton(web5);

		Bubble plan6 = new Bubble("Telcel Max Sin Límites 6000");
		plan6.setSubtitle(
				"Con los Planes Telcel Max Sin Límite, tienes Minutos, Mensajes de Texto (SMS) sin límite en México, E.U.A. y Canadá");
		plan6.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/plan6000.jpg");
		plan6.setItemUrl(
				"http://www.telcel.com/personas/equipos/telefonos-y-smartphones/samsung/sm-g930f#opciones-compra");
		WebButton web6 = new WebButton("Quiero saber más.",
				"http://www.telcel.com/personas/telefonia/planes-de-renta/tarifas-y-opciones/telcel-max-sin-limite");
		web6.setWebviewHeightRatio(WebviewHeightEnum.tall);
		plan6.addButton(buttonContrata);
		plan6.addButton(web6);

		Bubble plan7 = new Bubble("Telcel Max Sin Límites 7000");
		plan7.setSubtitle(
				"Con los Planes Telcel Max Sin Límite, tienes Minutos, Mensajes de Texto (SMS) sin límite en México, E.U.A. y Canadá");
		plan7.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/plan7000.jpg");
		plan7.setItemUrl(
				"http://www.telcel.com/personas/equipos/telefonos-y-smartphones/samsung/sm-g930f#opciones-compra");
		WebButton web7 = new WebButton("Quiero saber más.",
				"http://www.telcel.com/personas/telefonia/planes-de-renta/tarifas-y-opciones/telcel-max-sin-limite");
		web7.setWebviewHeightRatio(WebviewHeightEnum.tall);
		plan7.addButton(buttonContrata);
		plan7.addButton(web7);

		payload.addBubble(plan1);
		payload.addBubble(plan2);
		payload.addBubble(plan3);
		payload.addBubble(plan5);
		payload.addBubble(plan6);
		payload.addBubble(plan7);

		TemplateAttachment tA = new TemplateAttachment(payload);
		Message msg = new Message(tA);

		IdMessageRecipient recipient = new IdMessageRecipient(sender);

		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", msg)); // one of the messages from
													// above
	}

	public void sendMenuAmigo(String sender, String pageToken) {
		System.out.println("Opciones para menu prepago");
		
		sendSimpleMessage(sender, env.getProperty("mensaje_previo_prepago"), pageToken);
		
		GenericTemplatePayload payload = new GenericTemplatePayload();
		PostbackButton buttonInicio = new PostbackButton("\uD83D\uDCD6 Volver al inicio", "AYUDA");
		

		Bubble bubbleSinLimite = new Bubble("Amigo Sin Límite");
		bubbleSinLimite.setSubtitle("Amigo Sin Límite");
		bubbleSinLimite.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/amigoSinLimite.jpg");
		bubbleSinLimite
				.setItemUrl("http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!amigo-sin-limite");
		WebButton webButtonSinLimite = new WebButton("Quiero saber más",
				"http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!amigo-sin-limite");
		webButtonSinLimite.setWebviewHeightRatio(WebviewHeightEnum.tall);
		bubbleSinLimite.addButton(webButtonSinLimite);
		bubbleSinLimite.addButton(buttonInicio);

		Bubble bubblePorSegundo = new Bubble("Amigo Por Segundo");
		bubblePorSegundo.setSubtitle("Amigo Por Segundo");
		bubblePorSegundo.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/amigoSegundo.jpg");
		bubblePorSegundo
				.setItemUrl("http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!cobro-por-segundo");
		WebButton webButtonPorSegundo = new WebButton("Quiero saber más",
				"http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!cobro-por-segundo");
		webButtonPorSegundo.setWebviewHeightRatio(WebviewHeightEnum.tall);
		bubblePorSegundo.addButton(webButtonPorSegundo);
		bubblePorSegundo.addButton(buttonInicio);

		Bubble bubbleOptimoSinFrontera = new Bubble("Amigo Óptimo Plus Sin Frontera");
		bubbleOptimoSinFrontera.setSubtitle("Amigo Óptimo Plus Sin Frontera");
		bubbleOptimoSinFrontera.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/amigoFrontera.jpg");
		bubbleOptimoSinFrontera.setItemUrl(
				"http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!amigo-optimo-plus-sin-frontera");
		WebButton webButtonSinFrontera = new WebButton("Quiero saber más",
				"http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!amigo-optimo-plus-sin-frontera");
		webButtonSinFrontera.setWebviewHeightRatio(WebviewHeightEnum.tall);
		bubbleOptimoSinFrontera.addButton(webButtonSinFrontera);
		bubbleOptimoSinFrontera.addButton(buttonInicio);


		Bubble bubblePaqAmigoSinLimites = new Bubble("Paquetes Amigo Sin Límites");
		bubblePaqAmigoSinLimites.setSubtitle("Paquetes Amigo Sin Límites");
		bubblePaqAmigoSinLimites.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/amigoPaquete.jpg");
		bubblePaqAmigoSinLimites.setItemUrl(
				"http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!paquetes-amigo-sin-limite");
		WebButton webButtonPaqSinLimites = new WebButton("Quiero saber más",
				"http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!paquetes-amigo-sin-limite");
		webButtonPaqSinLimites.setWebviewHeightRatio(WebviewHeightEnum.tall);
		bubblePaqAmigoSinLimites.addButton(webButtonPaqSinLimites);
		bubblePaqAmigoSinLimites.addButton(buttonInicio);

		Bubble bubbleAmigoMasInformacion = new Bubble("Más información");
		bubbleAmigoMasInformacion.setSubtitle("Más información");
		bubbleAmigoMasInformacion.setImageUrl("https://everisbotsec-everis.1d35.starter-us-east-1.openshiftapps.com/images/masInformacion.jpg");
		bubbleAmigoMasInformacion
				.setItemUrl("http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!mas-informacion");
		WebButton webButtonAmigoMasInformacion = new WebButton("Más",
				"http://www.telcel.com/personas/telefonia/amigo/tarifas-y-opciones#!mas-informacion");
		webButtonAmigoMasInformacion.setWebviewHeightRatio(WebviewHeightEnum.tall);
		bubbleAmigoMasInformacion.addButton(webButtonAmigoMasInformacion);
		bubbleAmigoMasInformacion.addButton(buttonInicio);

		payload.addBubble(bubbleSinLimite);
		payload.addBubble(bubblePorSegundo);
		payload.addBubble(bubbleOptimoSinFrontera);
		payload.addBubble(bubblePaqAmigoSinLimites);
		payload.addBubble(bubbleAmigoMasInformacion);

		TemplateAttachment tA = new TemplateAttachment(payload);
		Message msg = new Message(tA);

		IdMessageRecipient recipient = new IdMessageRecipient(sender);

		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", msg)); // one of the messages from
													// above
	}

	public void sendSimpleMessage(String sender, String message, String pageToken) {
		IdMessageRecipient recipient = new IdMessageRecipient(sender);
		Message simpleTextMessage = new Message(message);
		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", simpleTextMessage)); // one of the
																// messages from
																// above
	}

	public void sendGenericMessage(String sender, String pageToken, MessageInfo... messages) {
		GenericTemplatePayload payload = new GenericTemplatePayload();
		for (MessageInfo message : messages) {
			Bubble bubble = new Bubble(message.getLabel());
			bubble.setSubtitle(message.getText());
			payload.addBubble(bubble);
		}

		TemplateAttachment tA = new TemplateAttachment(payload);
		Message msg = new Message(tA);

		IdMessageRecipient recipient = new IdMessageRecipient(sender);

		FacebookClient pageClient = new DefaultFacebookClient(pageToken, Version.VERSION_2_6);

		pageClient.publish("me/messages", FacebookType.class, Parameter.with("recipient", recipient), // the
																										// id
																										// or
																										// phone
																										// recipient
				Parameter.with("message", msg)); // one of the messages from
													// above
	}

	@Override
	public boolean sendMessage(String sender, String message, String pageToken) {
		boolean resp = true;
		try {
			if (sender.equals(
					"-1")) {/*
							 * En caso de que la seleccion sea -1, el mensaje se
							 * enviará a todos los usuarios registrados
							 */
				for (String idUsuarioTelcel : facebookUsersDao.getAllUsersRegistered().keySet()) {
					UsuarioTelcel usuarioTelcel = facebookUsersDao.getAllUsersRegistered().get(idUsuarioTelcel);
					this.sendSimpleMessage(usuarioTelcel.getIdFaceebook(), message, pageToken);
				}
			} else {
				this.sendSimpleMessage(sender, message, pageToken);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			resp = false;
		}
		return resp;
	}
}
