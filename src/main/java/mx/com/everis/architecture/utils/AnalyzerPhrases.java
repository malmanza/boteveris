package mx.com.everis.architecture.utils;

public class AnalyzerPhrases {
	public static String REQUEST_ACCOUNT_STATUS = "Account Status";
	public static String REQUEST_BALANCE = "Balance";
	public static String REQUEST_NOT_FOUND = "Not found";
	public static String REQUEST_HELP = "Help";
	public static String REQUEST_CONTRACT = "Contract";
	public static String REQUEST_MENU_POSPAGO = "MenuPospago";
	public static String REQUEST_MENU_PREPAGO = "MenuPrepago";
	public static String REQUEST_UBICACION = "Ubicacion";
	public static String REQUEST_POSPAGO_SERVICIOS = "Servicios adicionales";
	public static String REQUEST_POSPAGO_FACTURA = "Pago Factura";
	public static String REQUEST_POSPAGO_OFERTA = "Oferta Pospago";
	public static String REQUEST_PREPAGO_OFERTA = "Oferta Prepago";
	public static String REQUEST_PREPAGO_TODO_DESTINO = "TODO_DESTINO";
	public static String REQUEST_PREPAGO_CAMBIAR = "Cambiar";

	private static String[] WORDS_ACCOUNT_STATUS = { "ESTADO DE CUENTA", "ESTADODE CUENTA", "ESTADO DECUENTA",
			"STADO CUENTA", "CUENTA", "FACTURA" };
	private static String[] WORDS_BALANCE = { "SALDO", "CONSUMO" };

	private static String[] WORDS_HELP = { "AYUDA", "HELP", "AYÚDAME" };

	private static String[] WORDS_CONTRACT = { "CONTRATACION", "CONTRATAR", "CONTRATACIÓN" };

	private static String[] WORDS_MENU_POSPAGO = { "POSPAGO", "PLANES", "POSTPAGO"};
	private static String[] WORDS_MENU_PREPAGO = { "PREPAGO", "AMIGO" };
	private static String[] WORDS_UBICACION = { "CAC", "CACS" };
	private static String[] WORDS_PREPAGO_NUMEROS_TODO_DESTINO = { "NÚMEROS GRATIS TODO DESTINO", "NUMEROS GRATIS TODO DESTINO","NUMEROS GRATUITOS TODO DESTINO","NÚMEROS GRATUITOS TODO DESTINO"};
	private static String[] WORDS_POSPAGO_SERVICIOS = {"SERVICIOS ADICIONALES" };
	private static String[] WORDS_POSPAGO_FACTURA = { "PAGO DE MI FACTURA", "PAGAR MI PLAN", "RECIBO" };

	public static String getValue(String phrase) {
		for (String word : WORDS_HELP) {
			if (phrase.toUpperCase().contains(word)) {
				return AnalyzerPhrases.REQUEST_HELP;
			}
		}
		for (String word : WORDS_MENU_POSPAGO) {
			if (phrase.toUpperCase().contains(word)) {
				return AnalyzerPhrases.REQUEST_MENU_POSPAGO;
			}
		}
		for (String word : WORDS_MENU_PREPAGO) {
			if (phrase.toUpperCase().contains(word)) {
				return AnalyzerPhrases.REQUEST_MENU_PREPAGO;
			}
		}
		for (String word : WORDS_UBICACION) {
			if (phrase.toUpperCase().contains(word)) {
				return AnalyzerPhrases.REQUEST_UBICACION;
			}
		}
		for (String word : WORDS_POSPAGO_SERVICIOS) {
			if (phrase.toUpperCase().contains(word)) {
				return AnalyzerPhrases.REQUEST_POSPAGO_SERVICIOS;
			}
		}
		for (String word : WORDS_POSPAGO_FACTURA) {
			if (phrase.toUpperCase().contains(word)) {
				return AnalyzerPhrases.REQUEST_POSPAGO_FACTURA;
			}
		}


		for (String word : WORDS_PREPAGO_NUMEROS_TODO_DESTINO) {
			if (phrase.toUpperCase().contains(word)) {
				return AnalyzerPhrases.REQUEST_PREPAGO_TODO_DESTINO;
			}
		}
		
		return AnalyzerPhrases.REQUEST_NOT_FOUND;
	}
}
