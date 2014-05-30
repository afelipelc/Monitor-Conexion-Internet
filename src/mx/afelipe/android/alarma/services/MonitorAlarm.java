package mx.afelipe.android.alarma.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import mx.afelipe.android.alarma.AlarmaConexionInternet;
import mx.afelipe.android.alarma.R;
import mx.afelipe.android.alarma.activities.MainActivity;
import mx.afelipe.android.alarma.model.Suceso;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class MonitorAlarm extends BroadcastReceiver{
	private SharedPreferences prefsMonitor;
	private int NOTIFICATION = R.string.local_service;
	private NotificationManager mNM;
	boolean monitorActivado;
	//private final long tiempoMonitor = (1000 * 60) * 5; // 5 mins
	MediaPlayer mediaPlayer;
	private int contadorPing = 0;
	private Context context;
	MonitorTask monitorTask = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		CargarEstadoInPrefs();
		monitorTask = new MonitorTask();
		monitorTask.execute((Void) null);
		//Monitor();
		
		//Log.d("Monitor Internet", "Monitor status attemp");
	}
	
	//Al iniciar el servicio, cargar el estado inicial del servicio 
	private void CargarEstadoInPrefs() {
		// Retrive store shared preferences
		prefsMonitor = context.getSharedPreferences("prefsmonitor",
				Context.MODE_PRIVATE);
		//Log.i("Monitor Internet", "Reiniciar monitoreo, guardado como activado.");
		//return prefsMonitor.getBoolean("monitorstatus", false);
		contadorPing = prefsMonitor.getInt("pings", 0);
	}
	
	private void guardarEstatusPrefs() {
		// save status on preferences
		SharedPreferences.Editor editor = prefsMonitor.edit();
		//editor.putBoolean("monitorstatus", status);
		editor.putInt("pings", contadorPing);
		editor.commit();
	}
	
	private boolean Monitor(){
		String completMsg = "";
		boolean ntConect = isWifiConnected(); // esta por WIFI
		completMsg = ntConect ? "OK" : "NO";
		((AlarmaConexionInternet) context.getApplicationContext()).getSucesos().add(
				new Suceso("Wifi Conectado: " + completMsg, new Date(),
						ntConect));
		
		//Log.d("Monitor", "Wifi Conectado: " + completMsg);
		
		if (ntConect)// if is connected via WIFI
		{
			try {
				ntConect = isOnline(); // Google are you there?
			} catch (Exception e) {
				//e.printStackTrace();
				ntConect = false; // reset connection var
			}
			completMsg = ntConect ? "Conectado"
					: "DESCONECTADO, inten. "
							+ (contadorPing + 1)
							+ "\nRevisar el estado de la línea telefónica.";
			((AlarmaConexionInternet) context.getApplicationContext()).getSucesos()
					.add(new Suceso("Internet: " + completMsg,
							new Date(), ntConect));
			
			//Log.d("Monitor", "Internet: " + completMsg + " | Ping: " + contadorPing);
			
			if (!ntConect) {
				contadorPing++;
				if (contadorPing >= 2) {
					// If isn't Connected, play sound alert
					//if (!sonarAlarma) {
						// if sound alert is activated
						if (emitirAlarma()) {
							((AlarmaConexionInternet) context.getApplicationContext())
									.getSucesos()
									.add(new Suceso(
											"Sonido de Alarma Activado... ",
											new Date(), false));
						} else {
							((AlarmaConexionInternet) context.getApplicationContext())
									.getSucesos()
									.add(new Suceso(
											"Error al activar el sonido... ",
											new Date(), false));
						}
					//}
				}
				//Log.i("Monitor internet", "Pings sin respuesta: " + contadorPing);
			} else { // si se recupero
				if (contadorPing > 0)
				{
					contadorPing = 0;
				}
			}
		} else {
			contadorPing = 0; //no wifi
			//detenerSonidoAlarma();
		}
		
		return ntConect;
	}
	
	private boolean emitirAlarma() {
	if(((AlarmaConexionInternet) context.getApplicationContext()).isSonandoAlarma())
		return true;
		
		showNotification();
		try {
			mediaPlayer = MediaPlayer.create(context,
					R.raw.alert_sound);
			mediaPlayer.setLooping(true);
			//mediaPlayer.start();
			((AlarmaConexionInternet) context.getApplicationContext()).setMediaPlayer(mediaPlayer);
			((AlarmaConexionInternet) context.getApplicationContext()).soundPlayer().start();
			((AlarmaConexionInternet) context.getApplicationContext()).setSonandoAlarma(true);
			return true;
		} catch (Exception ex) {
			((AlarmaConexionInternet) context.getApplicationContext()).setSonandoAlarma(false);
			return false;
		}
	}
	
	private boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean wifiConectado = false;
		if (cm != null) {
			NetworkInfo[] netInfo = cm.getAllNetworkInfo();

			for (NetworkInfo ni : netInfo) {
				if (ni.getTypeName().equalsIgnoreCase("WIFI")
						&& ni.isConnected() && ni.isAvailable()) {
					wifiConectado = true;
				}
			}
		}
		return wifiConectado;
	}

	// CHECK CONNECTIVITY
	public boolean isOnline() {
		//Log.d("Monitor Internet", "Checking online status");
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			try {
				URL url = new URL("http://74.125.137.94"); // www.google.com.mx
				HttpURLConnection urlc = (HttpURLConnection) url
						.openConnection();
				urlc.setConnectTimeout(3000);
				urlc.connect();
				//Log.d("Monitor Internet", "Ping status: " + urlc.getResponseCode() + " == " + HttpURLConnection.HTTP_OK);
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_OK) {
					return true;
				}
			} catch (MalformedURLException e1) {
				//Log.d("Error conexion", e1.getMessage());
				return false;
			} catch (IOException e) {
				//Log.d("Error conexion IO", e.getMessage());
				//e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void showNotification() {
		mNM = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		CharSequence text = "No se detecta la conexión a Internet\nPings fallidos: "
				+ contadorPing; // getText(R.string.local_service);
		Notification notification = new Notification(R.drawable.ic_launcher,
				text, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MainActivity.class), 0);
		notification.setLatestEventInfo(context, "Monitor Internet", text,
				contentIntent);
		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
	}
	
	public class MonitorTask extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return Monitor();
		}
		@Override
		protected void onPostExecute(Boolean result) {
			guardarEstatusPrefs();
			monitorTask = null;
		}
	}
}
