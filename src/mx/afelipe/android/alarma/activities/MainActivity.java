/**
 * 
 * La aplicaci�n est� destinada principalmente a los usuarios de Internet por l�nea telef�nica que 
 * habitan en Xochiltepec, Puebla, Mexico y San Mart�n Totoltepec, Puebla, Mexico donde el cable telef�nico 
 * es robado -principalmente por la madrugada cuando nadie vigila-. 
 * 
 * Al activar el monitoreo la aplicaci�n estar� haciendo pruebas de conexi�n cada cierto tiempo, 
 * al detectar que no puede conectarse a un servidor despu�s de algunos intentos, emitir� un sonido de alarma.
 * Nota: Se requiere que el dispositivo est� siempre conectado a la red WiFi.
 * 
 * 
 * Proyecto empezado por AFelipe Lima Cort�s el 28 de agosto de 2013
 * 
 * Nota: Si has adquirido este c�digo entonces ayuda a mejorarlo, de lo contrario, espero sirva como parte
 * del aprendizaje sobre programaci�n.
 * 
 * El c�digo a�n necesita ser pulido, mientras pueda cotinuar� mejorandolo
 * 
 * Mi email: afelipelc@gmail.com
 * 
 * Versi�n 0.1 Beta.
 */

package mx.afelipe.android.alarma.activities;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import mx.afelipe.android.alarma.AlarmaConexionInternet;
import mx.afelipe.android.alarma.R;
import mx.afelipe.android.alarma.adapters.SucesoItemAdapter;
import mx.afelipe.android.alarma.model.Suceso;
import mx.afelipe.android.alarma.services.MonitorService;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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

public class MainActivity extends Activity {

	final long tiempoCheckSucess = (1000 * 60) * 3; // 3 mins
	ToggleButton ActDescAlarmBtn;
	TextView EstadoText;
	ListView SucesosListView;
	ImageButton detenerSonidoBtn;

	AlertDialog.Builder notificAlerta;
	private boolean mensajeMostrado = false;
	private SharedPreferences prefsMonitor;

	SucesoItemAdapter adapterSucesos;
	List<Suceso> listaSucesos = new ArrayList<Suceso>();

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
		
		EncenderServicio();

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
			notificAlerta = new AlertDialog.Builder(this, R.style.dialog_light);
		else
			notificAlerta = new AlertDialog.Builder(this);

		notificAlerta.setTitle("Sin conexi�n a Internet");

		notificAlerta
				.setMessage("No se ha podido conectar a www.google.com.mx, revise el estado de la l�nea telef�nica."
						+ "\nSi estamos sin l�nea, hagamos algo, esta alarma est� sonando en los dispositivos donde se ha instalado y activado.");

		notificAlerta.setPositiveButton("Ver M�dem",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						// If says OK, stop sound alert and open home modem in browser
						((AlarmaConexionInternet) getApplication())
								.getMonitorService().detenerSonidoAlarma();
						checkSucesos();

						detenerSonidoBtn.setVisibility(View.GONE);

						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("http://192.168.1.254"));
						startActivity(intent);

						mensajeMostrado = false;
					}
				});

		notificAlerta.setNegativeButton("Cerrar",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						// no hacer nada
						mensajeMostrado = false;
					}
				});

		this.ActDescAlarmBtn
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {
						// TODO Auto-generated method stub
						if (isChecked
								&& !((AlarmaConexionInternet) getApplication())
										.getMonitorService()
										.isMonitorActivado()) {
							// ((AlarmaConexionInternet) getApplication())
							// .setEstatusAlarma(true);
							// start monitoring
							((AlarmaConexionInternet) getApplication())
									.getMonitorService().IniciarMonitor();

							guardarEstatusPrefs(true);

							checkStatus();
							checkSucesos();
							actualizarUIHandler
									.removeCallbacks(actualizaUITask);
							actualizarUIHandler.postDelayed(actualizaUITask,
									2000);

						} else if (isChecked == false
								&& ((AlarmaConexionInternet) getApplication())
										.getMonitorService()
										.isMonitorActivado()) {
							// ((AlarmaConexionInternet) getApplication())
							// .setEstatusAlarma(false);
							// stop monitoring
							((AlarmaConexionInternet) getApplication())
									.getMonitorService().DetenerMonitor();
							guardarEstatusPrefs(false);

							actualizarUIHandler
									.removeCallbacks(actualizaUITask);
							checkStatus();
							checkSucesos();

						}

						// checkStatus();
						// checkSucesos();
					}
				});

		this.detenerSonidoBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				((AlarmaConexionInternet) getApplication()).getMonitorService()
						.detenerSonidoAlarma();
				checkSucesos();
				if (!((AlarmaConexionInternet) getApplication())
						.isSonandoAlarma()) {
					detenerSonidoBtn.setVisibility(View.GONE);
				}
			}
		});

		// load sucess
		adapterSucesos = new SucesoItemAdapter(this, this.listaSucesos);

		this.SucesosListView.setAdapter(adapterSucesos);

		// checking monitor service
		actualizarUIHandler.removeCallbacks(actualizaUITask);
		actualizarUIHandler.postDelayed(actualizaUITask, 1000);

		if (((AlarmaConexionInternet) getApplication()).getMonitorService() != null
				&& ((AlarmaConexionInternet) getApplication())
						.isSonandoAlarma())
			this.detenerSonidoBtn.setVisibility(View.VISIBLE);
		else
			this.detenerSonidoBtn.setVisibility(View.GONE);

		this.ActDescAlarmBtn.setEnabled(true);

		((AlarmaConexionInternet) getApplication()).getSucesos().add(
				new Suceso("Vista gr�fica iniciada...", new Date(), true));

		checkStatus();
		checkSucesos();

	}

	private void guardarEstatusPrefs(boolean status) {
		// save status on preferences
		//if Android stop our Service, this will restar and continue checking Internet Connection
		SharedPreferences.Editor editor = prefsMonitor.edit();
		editor.putBoolean("monitorstatus", status);
		editor.commit(); // save changes
	}
	
	private void EncenderServicio()
	{
		Intent serviciointent = new Intent(MainActivity.this,
				MonitorService.class);
		startService(serviciointent);

		Log.i("Monitor Internet", "Se ha solicitado Encender el servicio.");
		conectarServicio();
	}

	private void checkStatus() {
		if (((AlarmaConexionInternet) getApplication()).getMonitorService() == null) {
			EncenderServicio();
			return;
		}

		try {

			this.EstadoText.setText(((AlarmaConexionInternet) getApplication())
					.Estatus());
			if (((AlarmaConexionInternet) getApplication()).isEstatusAlarma())
				this.EstadoText.setTextColor(getResources().getColor(
						R.color.GreenColor));
			else
				this.EstadoText.setTextColor(getResources().getColor(
						R.color.RedText));

			this.ActDescAlarmBtn
					.setChecked(((AlarmaConexionInternet) getApplication())
							.isEstatusAlarma());

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
			EncenderServicio();
		}

	}

	private void checkSucesos() {
		// Log.d("Check Sucesos", ((AlarmaConexionInternet)
		// getApplication()).getSucesos().size()+"");
		try {
			if (((AlarmaConexionInternet) getApplication()).getSucesos().size() > 0) {

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					// this for android 4.0+
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
				// Log.d("Sucesos:",((AlarmaConexionInternet)
				// getApplication()).getSucesos().size()+"");
			}
		} catch (Exception ex) {
			Log.e("Error", ex.getStackTrace().toString());
		}
	}

	boolean servicioConectado = false;
	private ServiceConnection conexionServicio = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName arg0) {
			((AlarmaConexionInternet) getApplication()).setMonitorService(null);
			servicioConectado = false;
			Log.i("Servicio monitor", "Se ha desconectado el servicio");
		}

		public void onServiceConnected(ComponentName className, IBinder service) {


			// save connected Service as global var
			((AlarmaConexionInternet) getApplication())
					.setMonitorService(((MonitorService.LocalBinder) service)
							.getService());

			Log.d("Servicio monitor", "Se ha conectado al servicio");
			servicioConectado = true;
		}
	};

	private void conectarServicio() {
		bindService(new Intent(MainActivity.this, MonitorService.class),
				conexionServicio, Context.BIND_AUTO_CREATE);
	}

	private void desconectarServicio() {
		if (servicioConectado) {
			unbindService(conexionServicio);
			servicioConectado = false;
		}
	}

	@Override
	public void onBackPressed() {

		boolean checkMonitor = ((AlarmaConexionInternet) getApplication())
				.getMonitorService().CheckMonitorDesactivado();

		if (((AlarmaConexionInternet) getApplication()).getMonitorService() != null) {
			desconectarServicio();
		}

		if (checkMonitor) {
			getApplicationContext().stopService(
					new Intent(MainActivity.this, MonitorService.class));
		}

		actualizarUIHandler.removeCallbacks(actualizaUITask);// remove UI update thread
		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
