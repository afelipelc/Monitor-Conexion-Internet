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
 * @author afelipe
 * 
 * Versión 0.1 Beta.
 */

package mx.afelipe.android.alarma.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import mx.afelipe.android.alarma.AlarmaConexionInternet;
import mx.afelipe.android.alarma.R;
import mx.afelipe.android.alarma.adapters.SucesoItemAdapter;
import mx.afelipe.android.alarma.model.Suceso;
import mx.afelipe.android.alarma.services.MonitorAlarm;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Esta clase es la encargada de mostrar la parte gráfica para interactuar con el servicio de monitoreo
 *
 */
public class MainActivity extends Activity {

	MediaPlayer mediaPlayer;
	final long tiempoCheckSucess = (1000 * 60) * 3; // 3 mins
	private final long tiempoMonitor = (1000 * 60) * 5; // 5 mins
	ToggleButton ActDescAlarmBtn;
	TextView EstadoText;
	ListView SucesosListView;
	ImageButton detenerSonidoBtn;

	AlertDialog.Builder notificAlerta;
	private boolean mensajeMostrado = false, statusAlarma = false;
	private SharedPreferences prefsMonitor;

	SucesoItemAdapter adapterSucesos;
	List<Suceso> listaSucesos = new ArrayList<Suceso>();
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	
	//Tarea que actualiza la ui con los sucesos generados en el servicio de monitoreo
	private Handler actualizarUIHandler = new Handler();
	private Runnable actualizaUITask = new Runnable() {
		public void run() {
			checkStatus();
			checkSucesos();
			actualizarUIHandler.postDelayed(this, tiempoCheckSucess);
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		((AlarmaConexionInternet) getApplication()).getSucesos().add(
				new Suceso("Vista gráfica iniciada...", new Date(), true));
		
		this.ActDescAlarmBtn = (ToggleButton) findViewById(R.id.ActDescAlarmBtn);
		this.EstadoText = (TextView) findViewById(R.id.EstadoText);
		this.SucesosListView = (ListView) findViewById(R.id.SucesosListView);
		this.detenerSonidoBtn = (ImageButton) findViewById(R.id.detenerSonidoBtn);
		
		// color toogleButton
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.HONEYCOMB)
			this.ActDescAlarmBtn.setTextColor(getResources().getColor(
					R.color.BlackText));

		// initialice preferences
		prefsMonitor = getSharedPreferences("prefsmonitor",
				Context.MODE_PRIVATE);
		this.statusAlarma = prefsMonitor.getBoolean("monitorstatus", false);
		
		alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(), MonitorAlarm.class);
		alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
		
		//EncenderServicio(); //Activar el servicio

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
			notificAlerta = new AlertDialog.Builder(this, R.style.dialog_light);
		else
			notificAlerta = new AlertDialog.Builder(this);

		//Preparar la alerta informativa que se mostrará al usuario
		notificAlerta.setTitle("Sin conexión a Internet");
		notificAlerta
				.setMessage("No se ha podido conectar a www.google.com.mx, revise el estado de la línea telefónica."
						+ "\nSi estamos sin línea, hagamos algo, esta alarma está sonando en los dispositivos donde se ha instalado y activado.");
		notificAlerta.setPositiveButton("Ver Módem",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						// If says OK, stop sound alert and open home modem in browser
//						((AlarmaConexionInternet) getApplication())
//								.getMonitorService().detenerSonidoAlarma();
						DetenerSonidoAlarma(true);
						checkSucesos();
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("http://192.168.1.254"));
						startActivity(intent);
						mensajeMostrado = false;
					}
				});

		notificAlerta.setNegativeButton("Cerrar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						mensajeMostrado = false;
					}
				});

		//Botón para activar o desactivar el monitor de internet
		this.ActDescAlarmBtn
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {
//						if (isChecked
//								&& !((AlarmaConexionInternet) getApplication())
//										.getMonitorService()
//										.isMonitorActivado()) {
						if (isChecked && !statusAlarma) {
							//Log.d("Monitor Internet", "Paso a activar.");
							// start monitoring
							//((AlarmaConexionInternet) getApplication())
							//		.getMonitorService().IniciarMonitor();

							//Arrancar AlarmManager
							EncenderAlarm();
							
							checkStatus();
							checkSucesos();
							//Log.i("Monitor Internet","Activando monitoreo");
							actualizarUIHandler
									.removeCallbacks(actualizaUITask);
							actualizarUIHandler.postDelayed(actualizaUITask,
									2000);
//
//						} else if (isChecked == false
//								&& ((AlarmaConexionInternet) getApplication())
//										.getMonitorService()
//										.isMonitorActivado()) {

						} else if (isChecked == false && statusAlarma) {
							//Log.d("Monitor Internet", "Paso a DESactivar.");
							// stop monitoring
//							((AlarmaConexionInternet) getApplication())
//									.getMonitorService().DetenerMonitor(false);
//							((AlarmaConexionInternet) getApplication()).getSucesos().add(
//									new Suceso("Monitoreo de conexión Apagado", new Date(), false));
							
							
							//detener AlarmManager
							CancelAlarm(true);
							actualizarUIHandler
									.removeCallbacks(actualizaUITask);
							checkStatus();
							checkSucesos();
							//Log.i("Monitor Internet","Desactivando monitoreo");
						}
						//Log.d("Monitor", "Status alarma: " + statusAlarma + ", Button checked: " + isChecked);
					}
				});

		// Botón para detener el sonido de la alarma si es activada
		this.detenerSonidoBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				((AlarmaConexionInternet) getApplication()).getMonitorService()
//						.detenerSonidoAlarma();
				DetenerSonidoAlarma(true);
				checkSucesos();
//				if (!((AlarmaConexionInternet) getApplication())
//						.isSonandoAlarma()) {
//					detenerSonidoBtn.setVisibility(View.GONE);
//				}
			}
		});

		// load sucess
		adapterSucesos = new SucesoItemAdapter(this, this.listaSucesos);

		this.SucesosListView.setAdapter(adapterSucesos);

		// checking monitor service
		actualizarUIHandler.removeCallbacks(actualizaUITask);
		actualizarUIHandler.postDelayed(actualizaUITask, 1000);

//		if (((AlarmaConexionInternet) getApplication()).getMonitorService() != null
//				&& ((AlarmaConexionInternet) getApplication())
//						.isSonandoAlarma())
//			this.detenerSonidoBtn.setVisibility(View.VISIBLE);
//		else
//			this.detenerSonidoBtn.setVisibility(View.GONE);

		this.ActDescAlarmBtn.setEnabled(true);

		checkStatus();
		checkSucesos();
	}

	private void EncenderAlarm(){
		if (alarmMgr!= null){
			CancelAlarm(false);
		}
		
		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), tiempoMonitor, alarmIntent);
		
		((AlarmaConexionInternet) getApplication()).getSucesos().add(
				new Suceso("Monitoreo de conexión Activado", new Date(),
						true));
		statusAlarma = true;
		guardarEstatusPrefs(true);
	}
	
	private void CancelAlarm(boolean notif){
		// If the alarm has been set, cancel it.
		if (alarmMgr!= null) {
		    alarmMgr.cancel(alarmIntent);
		}
		statusAlarma = false;
		guardarEstatusPrefs(statusAlarma);
		DetenerSonidoAlarma(false);
		((AlarmaConexionInternet) getApplication()).setSonandoAlarma(false);
		if(notif)
			((AlarmaConexionInternet) getApplication()).getSucesos().add(
				new Suceso("Monitoreo de conexión apagado", new Date(),
						false));
	}
	
	private void DetenerSonidoAlarma(boolean notif){
		try {
			if(((AlarmaConexionInternet) getApplicationContext()).soundPlayer()!=null)
				((AlarmaConexionInternet) getApplicationContext()).soundPlayer().stop();
			
			((AlarmaConexionInternet) getApplicationContext()).setSonandoAlarma(false);
			detenerSonidoBtn.setVisibility(View.GONE);
			if(notif)
				((AlarmaConexionInternet) getApplication()).getSucesos().add(
					new Suceso("Sonido de alarma apagado", new Date(), true));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private void guardarEstatusPrefs(boolean status) {
		// save status on preferences
		SharedPreferences.Editor editor = prefsMonitor.edit();
		editor.putBoolean("monitorstatus", status);
		editor.putInt("pings", 0);
		editor.commit();
	}
	
//	private void EncenderServicio()
//	{
//		Intent serviciointent = new Intent(MainActivity.this,
//				MonitorService.class);
//		startService(serviciointent);
//		//Log.i("Monitor Internet", "Se ha solicitado Encender el servicio.");
//		conectarServicio();
//	}

	private void checkStatus() {
//		if (((AlarmaConexionInternet) getApplication()).getMonitorService() == null) {
//			EncenderServicio();
//			return;
//		}

		try {
//			this.EstadoText.setText(((AlarmaConexionInternet) getApplication())
//					.Estatus());
			this.EstadoText.setText(statusAlarma ? "Activado" : "Apagado");
			//if (((AlarmaConexionInternet) getApplication()).isEstatusAlarma())
			if (statusAlarma)
				this.EstadoText.setTextColor(getResources().getColor(
						R.color.GreenColor));
			else
				this.EstadoText.setTextColor(getResources().getColor(
						R.color.RedText));

			this.ActDescAlarmBtn
			.setChecked(statusAlarma);

			if (((AlarmaConexionInternet) getApplication()).isSonandoAlarma()) {
				this.detenerSonidoBtn.setVisibility(View.VISIBLE);
				if (!mensajeMostrado) {
					mensajeMostrado = true;
					notificAlerta.show();
				}
			} else {
				this.detenerSonidoBtn.setVisibility(View.GONE);
				mensajeMostrado = false;
			}
			
			return;
		} catch (Exception ex) {
			//EncenderServicio();
			ex.printStackTrace();
		}

	}

	private void checkSucesos() {
		try {
			if (((AlarmaConexionInternet) getApplication()).getSucesos().size() > 0) {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					// this is for android 4.0+
					adapterSucesos
							.addAll(((AlarmaConexionInternet) getApplication())
									.getSucesos());
				} else {
					for (Suceso item : ((AlarmaConexionInternet) getApplication())
							.getSucesos()) {
						adapterSucesos.add(item);
					}
				}

				adapterSucesos.notifyDataSetChanged();
				((AlarmaConexionInternet) getApplication()).getSucesos()
						.clear();
			}
		} catch (Exception ex) {
			Log.e("Error", ex.getStackTrace().toString());
		}
	}

//	boolean servicioConectado = false;
//	private ServiceConnection conexionServicio = new ServiceConnection() {
//
//		public void onServiceDisconnected(ComponentName arg0) {
//			((AlarmaConexionInternet) getApplication()).setMonitorService(null);
//			servicioConectado = false;
//			Log.i("Servicio monitor", "Se ha desconectado el servicio");
//		}
//
//		public void onServiceConnected(ComponentName className, IBinder service) {
//			// save connected Service as global var
//			((AlarmaConexionInternet) getApplication())
//					.setMonitorService(((MonitorService.LocalBinder) service)
//							.getService());
//			//Log.d("Servicio monitor", "Se ha conectado al servicio");
//			servicioConectado = true;
//		}
//	};
//
//	private void conectarServicio() {
//		bindService(new Intent(MainActivity.this, MonitorService.class),
//				conexionServicio, Context.BIND_AUTO_CREATE);
//	}
//
//	private void desconectarServicio() {
//		if (servicioConectado) {
//			unbindService(conexionServicio);
//			servicioConectado = false;
//		}
//	}

	@Override
	public void onBackPressed() {
//		boolean checkMonitor = ((AlarmaConexionInternet) getApplication())
//				.getMonitorService().CheckMonitorDesactivado();

//		if (((AlarmaConexionInternet) getApplication()).getMonitorService() != null) {
//			desconectarServicio();
//		}
//
//		if (checkMonitor) {
//			getApplicationContext().stopService(
//					new Intent(MainActivity.this, MonitorService.class));
//		}
		actualizarUIHandler.removeCallbacks(actualizaUITask);// remove UI update thread
		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_about:
			Intent aboutIn = new Intent(MainActivity.this, AboutActivity.class);
			startActivity(aboutIn);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
