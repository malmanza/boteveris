package mx.com.everis.architecture;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.restfb.DefaultJsonMapper;
import com.restfb.JsonMapper;
import com.restfb.types.send.MediaAttachment.Type;
import com.restfb.types.webhook.WebhookObject;

import mx.com.everis.architecture.service.IFacebookMessage;
import mx.com.everis.architecture.service.impl.FacebookMessageImpl;

/**
 * Servlet implementation class webhook
 */
@WebServlet("/webhook")
@PropertySource("classpath:/app_info.properties")
public class webhook extends HttpServlet {
	private static final long serialVersionUID = 1L;	

	@Autowired
	private IFacebookMessage facebookMessage;
	
	@Autowired
	private Environment env;

	//public String pageAccessToken = "EAAWpG5xBwqoBAK0ddERCZAavNyT8U0lBhZC1elqehzNKjqn10zhmtnAFXPLp6NiaEh4gNUHUSioSMCnKzKVAa7yhHWHB17ocFKejMoioGZBO6HssFUIOi8UrpiZBWvXZCgU7DflCW5XME1SoqwEWEGbC5ZCqpUbbiqkdI4tvEDZCMOezLpAiE9a";
//	public String pageAccessToken = "EAAYuKceGdYwBAN1y8J4vp9Vsi5ZAUUFTmHso4qvoqoliXNGCBW2qFV5sYdXPG8Kl3eTGQCJdDKWtpesXPxJ9ThZBQpbhATKZADMH1PHYZBLPGNEJ25vqwI6WE4aZAQBXCopmDrqLo4CyZAn54pSc1tZClDWG9ZBZCcxx8WjZCmLIi2zAOKBGeBBEbg";
	
	public String pageAccessToken ;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public webhook() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
				config.getServletContext());
		pageAccessToken = env.getProperty("page_token");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//		Codigo usado para el registro de la aplicacion como webhook
		System.out.println("**************Entering Callback Servlet :) **************************");
		Map<String, String[]> parametersMap = request.getParameterMap();

		Enumeration<String> params = request.getParameterNames(); 
		while(params.hasMoreElements()){
			String paramName = (String)params.nextElement();
			System.out.println("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
		}

		if (parametersMap.size() > 0) {
			System.out.println("************ Webhook verification ***********");
			if (request.getParameter("hub.mode").equals("subscribe")) {
				System.out.println("Verify Token: " + request.getParameter("hub.verify_token"));
				System.out.println("Challenge number:" + request.getParameter("hub.challenge"));
				String responseToClient = request.getParameter("hub.challenge");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseToClient);
				response.getWriter().flush();
				response.getWriter().close();
				response.getWriter().append("Fetch-Mode").append(request.getParameter("hub.mode"));
				response.getWriter().append("App Verify Token:").append(request.getParameter("hub.verify_token"));
				response.getWriter().append("App Challenge No").append(request.getParameter("hub.challenge"));
				System.out.println("**************Callback Successful**************************");
			}

		} else {
			System.out.println("**************Messages received**************************");

			/*Lectura de cadena enviada vía POST*/
			StringBuffer jb = new StringBuffer();
			String line = null;
			try {
				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);

				/*Parseo a través del API RestFB*/
				JsonMapper mapper = new DefaultJsonMapper();
				WebhookObject webhookObject = mapper.toJavaObject(jb.toString(), WebhookObject.class);
				String sender = webhookObject.getEntryList().get(0).getMessaging().get(0).getSender().getId();
				
				if ( !webhookObject.getEntryList().isEmpty()  &&   webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback()!=null && 
						"START".equals(webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload())){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("****ENTRA AL POSTBACK: senderId:[" + sender + "] mensaje: "+ env.getProperty("mensaje_bienvenida"));
					facebookMessage.serveMessage(sender, env.getProperty("mensaje_bienvenida"), pageAccessToken);
					
				}else if ( !webhookObject.getEntryList().isEmpty()  &&   webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback()!=null && 
						"UBICACION".equals(webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload())){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("****ENTRA AL POSTBACK: senderId:[" + sender + "] mensaje: "+  webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload());
					facebookMessage.serveMessage(sender, "UBICACION", pageAccessToken);
					
				}else if ( !webhookObject.getEntryList().isEmpty()  &&   webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback()!=null && 
						"AYUDA".equals(webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload())){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("****ENTRA AL POSTBACK: senderId:[" + sender + "] mensaje: "+  webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload());
					facebookMessage.serveMessage(sender, "AYUDA", pageAccessToken);
					
				}else if ( !webhookObject.getEntryList().isEmpty()  &&   webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback()!=null && 
						"OFERTAPREPAGO".equals(webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload())){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("****ENTRA AL POSTBACK: senderId:[" + sender + "] mensaje: "+  webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload());
					facebookMessage.serveMessage(sender, "Oferta prepago", pageAccessToken);
					
				}else if ( !webhookObject.getEntryList().isEmpty()  &&   webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback()!=null && 
						"OFERTAPOSPAGO".equals(webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload())){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("****ENTRA AL POSTBACK: senderId:[" + sender + "] mensaje: "+  webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload());
					facebookMessage.serveMessage(sender, "Oferta pospago", pageAccessToken);
					
				}else if ( !webhookObject.getEntryList().isEmpty()  &&   webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback()!=null && 
						"QUESONTODODESTINO".equals(webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload())){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("****ENTRA AL POSTBACK: senderId:[" + sender + "] mensaje: "+  webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload());
					((FacebookMessageImpl)facebookMessage).sendSimpleMessage(sender, env.getProperty("mensaje_que_son_todo_destino_1"), pageAccessToken);
//					((FacebookMessageImpl)facebookMessage).sendSimpleMessage(sender, env.getProperty("mensaje_que_son_todo_destino_2"), pageAccessToken);
//					((FacebookMessageImpl)facebookMessage).sendSimpleMessage(sender, env.getProperty("mensaje_que_son_todo_destino_3"), pageAccessToken);
					((FacebookMessageImpl)facebookMessage).sendMenuTodoDestino(sender, pageAccessToken);
				}
				else if ( !webhookObject.getEntryList().isEmpty()  &&   webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback()!=null && 
						"CUANTOCUESTATODODESTINO".equals(webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload())){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("****ENTRA AL POSTBACK: senderId:[" + sender + "] mensaje: "+  webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload());
					((FacebookMessageImpl)facebookMessage).sendSimpleMessage(sender, env.getProperty("mensaje_cuanto_todo_destino_1"), pageAccessToken);
//					((FacebookMessageImpl)facebookMessage).sendSimpleMessage(sender, env.getProperty("mensaje_cuanto_todo_destino_2"), pageAccessToken);
//					((FacebookMessageImpl)facebookMessage).sendSimpleMessage(sender, env.getProperty("mensaje_cuanto_todo_destino_3"), pageAccessToken);
					((FacebookMessageImpl)facebookMessage).sendMenuTodoDestino(sender, pageAccessToken);
				}
				else if ( !webhookObject.getEntryList().isEmpty()  &&   webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback()!=null && 
						"CONTRATACION".equals(webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload())){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("****ENTRA AL POSTBACK: senderId:[" + sender + "] mensaje: "+  webhookObject.getEntryList().get(0).getMessaging().get(0).getPostback().getPayload());
					facebookMessage.serveMessage(sender, "CONTRATACION", pageAccessToken);
					
				}else if ( !webhookObject.getEntryList().isEmpty() &&  !webhookObject.getEntryList().get(0).getMessaging().get(0).getMessage().getAttachments().isEmpty() && webhookObject.getEntryList().get(0).getMessaging().get(0).getMessage().getAttachments().get(0).getType().equals("location") ){
					//					facebookMessage.sendMessage(sender, "Bienvenido", pageAccessToken);
					System.out.println("(Lat,Lon)("+webhookObject.getEntryList().get(0).getMessaging().get(0).getMessage().getAttachments().get(0).getPayload().getCoordinates().getLat() +")("
							+webhookObject.getEntryList().get(0).getMessaging().get(0).getMessage().getAttachments().get(0).getPayload().getCoordinates().getLongVal()+")" );
					String lat = webhookObject.getEntryList().get(0).getMessaging().get(0).getMessage().getAttachments().get(0).getPayload().getCoordinates().getLat().toString();
					String lon = webhookObject.getEntryList().get(0).getMessaging().get(0).getMessage().getAttachments().get(0).getPayload().getCoordinates().getLongVal().toString();
					facebookMessage.serveMessage(sender, "SAMPLE_MAP|"+lat+","+lon, pageAccessToken);
				}else{
					String mensajeResp = webhookObject.getEntryList().get(0).getMessaging().get(0).getMessage().getText();
					facebookMessage.serveMessage(sender, mensajeResp, pageAccessToken);
				}
				
//				String mensajeResp = webhookObject.getEntryList().get(0).getMessaging().get(0).getMessage().getText();				
//				facebookMessage.serveMessage(sender, mensajeResp, pageAccessToken);
//				String message = "Su mensaje ( "+ mensajeResp + " ) ha sido recibido sin embargo aún no tengo una respuesta a esa pregunta.";
//				
//				IdMessageRecipient recipient = new IdMessageRecipient(sender);
//				Message simpleTextMessage = new Message(message);
//
//				FacebookClient pageClient = new DefaultFacebookClient(pageAccessToken, Version.VERSION_2_6);
//
//				pageClient.publish("me/messages", FacebookType.class,
//						Parameter.with("recipient", recipient), // the id or phone recipient
//						Parameter.with("message", simpleTextMessage)); // one of the messages from above
			}catch(Exception ex){
				System.out.println("Error: " + ex.getMessage());
				ex.printStackTrace();
			}

		}
		System.out.println("**************Exiting Callback Servlet**************************");


	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
