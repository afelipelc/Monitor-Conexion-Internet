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


package mx.afelipe.android.alarma.activities;

import mx.afelipe.android.alarma.R;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class AboutActivity extends Activity {

	Button irTwitterBtn;
	Button irCodigoBtn; // https://drive.google.com/folderview?id=0B1rlXLGYuE8eQ21CTGFfc0R0NGc&usp=sharing
	Button fallaBtn;

	final String twitter = "https://twitter.com/afelipelc";
	final String codigo = "https://drive.google.com/folderview?id=0B1rlXLGYuE8eQ21CTGFfc0R0NGc&usp=sharing";
	final String email = "afelipelc@gmail.com";
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.activity_about);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		this.irTwitterBtn = (Button) findViewById(R.id.irTwitterBtn);
		this.irCodigoBtn = (Button) findViewById(R.id.irCodigoBtn);
		this.fallaBtn = (Button) findViewById(R.id.fallaBtn);
		
		
		this.irTwitterBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(twitter));
				startActivity(i);
			}
		});

		this.irCodigoBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(codigo));
				startActivity(i);
			}
		});
		
		this.fallaBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri data = Uri.parse("mailto:"+email+"?subject=Falla en Monitor Internet App");
				intent.setData(data);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
