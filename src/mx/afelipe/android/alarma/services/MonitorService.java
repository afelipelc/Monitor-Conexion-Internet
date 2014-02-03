/**
 * 
 * La aplicación está destinada principalmente a los usuarios de Internet por línea telefónica que 
 * habitan en Xochiltepec, Puebla, Mexico y San Martín Totoltepec, Puebla, Mexico donde el cable telefónico 
 * es robado -principalmente por la madrugada cuando nadie vigila-. 
 * 
 * Al activar el monitoreo la aplicación estará haciendo pruebas de conexión cada cierto tiempo, 
 * al detectar que no puede conectarse a un servidor después de algunos intentos, emitirá un sonido de alarma.
 * Nota: Se requiere que el dispositivo esté siempre conectado a la red WiFi.
 * 
 * 
 * Proyecto empezado por AFelipe Lima Cortés el 28 de agosto de 2013
 * 
 * Nota: Si has adquirido este código entonces ayuda a mejorarlo, de lo contrario, espero sirva como parte
 * del aprendizaje sobre programación.
 * 
 * El código aún necesita ser pulido, mientras pueda cotinuaré mejorandolo
 * 
 * Mi email: afelipelc@gmail.com
 * 
 * Versión 0.1 Beta.
 */

package mx.afelipe.android.alarma.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import mx.afelipe.android.alarma.AlarmaConexionInternet;
import mx.afelipe.android.alarma.R;
import mx.afelipe.android.alarma.activities.MainActivity;
import mx.afelipe.android.alarma.model.Suceso;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MonitorService extends Service {

	// si se hereda IntentService haria que el servicio termine automaticamente
	private int NOTIFICATION = R.string.local_service;
	private NotificationManager mNM;
	private SharedPreferences prefsMonitor;

	Timer monitorTimer = new Timer();
	TimerTask monitorTask;
	boolean monitorActivado;
	private final long tiempoMonitor = (1000 * 60) * 5; // 5 mins
	// private long tiempoCritico = (1000 * 60 )* 1;

	MediaPlayer mediaPlayer;

	public class LocalBinder extends Binder {
		public MonitorService getService() {
			return MonitorService.this;
		}
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (CargarEstadoInPrefs())
			IniciarMonitor();

		Toast.makeText(getApplicationContext(),
				"Activando servicio de monitoreo de Internet.",
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		// mNM.cancel(NOTIFICATION);

		// Tell the user we stopped.
		DetenerMonitor(); // si Android lo detiene, detener la tarea de
							// monitoreo para reiniciar despues
		Toast.makeText(this, "Apagando servicio de monitoreo de Internet",
				Toast.LENGTH_SHORT).show();
		((AlarmaConexionInternet) getApplication()).setMonitorService(null);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	private int contadorPing = 0;

	private boolean sucesoCritico = false;
	private boolean sonarAlarma = false;

	private boolean CargarEstadoInPrefs() {
		// Retrive store shared preferences
		prefsMonitor = getSharedPreferences("prefsmonitor",
				Context.MODE_PRIVATE);
		Log.i("Monitor Internet",
				"Reiniciar monitoreo, guardado como activado.");
		return prefsMonitor.getBoolean("monitorstatus", false);

	}

	public boolean IniciarMonitor() {

		monitorTask = new TimerTask() {
			@Override
			public void run() {
				String completMsg = "";
				boolean ntConect = isWifiConnected(); // esta por WIFI

				completMsg = ntConect ? "OK" : "NO";
				((AlarmaConexionInternet) getApplication()).getSucesos().add(
						new Suceso("Wifi Conectado: " + completMsg, new Date(),
								ntConect));

				if (ntConect)// if is connected via WIFI
				{

					try {
						ntConect = isOnline(); // Google are you there?
					} catch (Exception e) {
						ntConect = false; // reset connection var
					}

					completMsg = ntConect ? "Conectado"
							: "DESCONECTADO, inten. "
									+ (contadorPing + 1)
									+ "\nRevisar el estado de la línea telefónica.";
					((AlarmaConexionInternet) getApplication()).getSucesos()
							.add(new Suceso("Internet: " + completMsg,
									new Date(), ntConect));

					if (!ntConect) {

						contadorPing++;

						if (contadorPing >= 2) {
							// If isn't Connected, play sound alert
							if (!sonarAlarma) {
								// if sound alert is activated
								if (emitirAlarma()) {
									((AlarmaConexionInternet) getApplication())
											.getSucesos()
											.add(new Suceso(
													"Sonido de Alarma Activado... ",
													new Date(), false));
								} else {
									((AlarmaConexionInternet) getApplication())
											.getSucesos()
											.add(new Suceso(
													"Error al activar el sonido... ",
													new Date(), false));
								}
							}
						}

						if (!sucesoCritico) {
							// monitorTimer.schedule(monitorTask, 0,
							// tiempoCritico); // verificar
							// en
							// 2
							// minutos
							// monitorTask.cancel();
							// monitorTimer.cancel();

							sucesoCritico = true;
							// IniciarMonitor();
							// Log.d("Monitor Internet",
							// "Entrando en estado critico");
						}

						Log.i("Monitor internet", "Pings sin respuesta: "
								+ contadorPing);
					} else {
						if (sucesoCritico && contadorPing > 0) // si se recupero
						{
							// monitorTask.cancel();
							// monitorTimer.cancel();
							sucesoCritico = false;
							contadorPing = 0;
							// IniciarMonitor();

							// Log.d("Monitor Internet",
							// "Recuperado de estado critico");
						}
					}
				} else {
					sucesoCritico = false;
					contadorPing = 0;
					detenerSonidoAlarma();
				}
			}
		};

		monitorTimer = new Timer();
		monitorTimer.schedule(monitorTask, 100, tiempoMonitor);

		this.monitorActivado = true;

		if (!sucesoCritico)
			((AlarmaConexionInternet) getApplication()).getSucesos().add(
					new Suceso("Monitoreo de conexión Activado", new Date(),
							true));

		return true;

	}

	public void DetenerMonitor() {

		if (monitorTimer != null)
			monitorTimer.cancel();
		if (monitorTask != null)
			monitorTask.cancel();

		((AlarmaConexionInternet) getApplication()).getSucesos().add(
				new Suceso("Monitoreo de conexión Apagado", new Date(), false));

		detenerSonidoAlarma();
		this.sonarAlarma = false;
		this.monitorActivado = false;

		// Log.d("Monitor Internet", "Status: Monitoreo apagado... ");
	}

	private boolean emitirAlarma() {

		showNotification();
		try {
			mediaPlayer = MediaPlayer.create(getApplicationContext(),
					R.raw.alert_sound);
			mediaPlayer.setLooping(true);

			mediaPlayer.start();

			sonarAlarma = true;
			return true;
		} catch (Exception ex) {
			sonarAlarma = false;
			return false;
		}
	}

	public void detenerSonidoAlarma() {
		sucesoCritico = false;
		contadorPing = 0;
		sonarAlarma = false;

		if (mediaPlayer == null)
			return;

		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;

		((AlarmaConexionInternet) getApplication()).getSucesos().add(
				new Suceso("Sonido de alarma apagado", new Date(), true));
	}

	public boolean isSonandoAlarma() {
		return sonarAlarma;
	}

	public boolean CheckMonitorDesactivado() {
		// Log.i("Estado Servicio", "Comprobando estado de conexion: "
		// + monitorActivado);

		if (!monitorActivado) {
			Log.i("Monitor internet",
					"Apagado, programando para detener servicio");
			detenerSonidoAlarma();
			stopSelf();
			return true;
		} else
			return false;
	}

	public boolean isMonitorActivado() {
		return monitorActivado;
	}

	private boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

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
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			try {
				URL url = new URL("http://74.125.137.94"); // www.google.com.mx
				HttpURLConnection urlc = (HttpURLConnection) url
						.openConnection();
				urlc.setConnectTimeout(3000);
				urlc.connect();
				if (urlc.getResponseCode() == HttpURLConnection.HTTP_OK) {
					return true;
				}
			} catch (MalformedURLException e1) {
				// Log.d("Error conexión", e1.getStackTrace().toString());
			} catch (IOException e) {
				// Log.d("Error conexión IO", e.getStackTrace().toString());
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void showNotification() {
		CharSequence text = "No se detecta la conexión a Internet\nPings fallidos: "
				+ contadorPing; // getText(R.string.local_service);

		Notification notification = new Notification(R.drawable.ic_launcher,
				text, System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);
		notification.setLatestEventInfo(this, "Monitor Internet", text,
				contentIntent);

		// Send the notification.
		mNM.notify(NOTIFICATION, notification);
	}
}
