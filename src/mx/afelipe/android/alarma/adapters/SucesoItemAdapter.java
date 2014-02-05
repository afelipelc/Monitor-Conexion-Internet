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

package mx.afelipe.android.alarma.adapters;

import java.text.SimpleDateFormat;
import java.util.List;
import mx.afelipe.android.alarma.R;
import mx.afelipe.android.alarma.model.Suceso;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SucesoItemAdapter extends ArrayAdapter<Suceso> {
	Context context;
	List<Suceso> sucesos;
	
	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat format = new SimpleDateFormat("MMMMM dd, hh:mm:ss a");
	
	public SucesoItemAdapter(Context context, List<Suceso> sucesos) {
		super(context, R.layout.sucesoitem_layout, sucesos);
		this.context = context;
		this.sucesos = sucesos;
		
	}
	
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		 LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	     
		 View item = inflater.inflate(R.layout.sucesoitem_layout, null);
		 
		 TextView tituloSucesoText = (TextView) item.findViewById(R.id.DescSucesoTxt);
		 TextView tiempoSucesoText = (TextView) item.findViewById(R.id.TiempoSucesoTxt);
	     
		 tituloSucesoText.setText(sucesos.get(position).getTitulo());
		 tiempoSucesoText.setText(format.format(sucesos.get(position).getTiempo()));
		 
		 if(!sucesos.get(position).isStatus())
		 {
			 tituloSucesoText.setTextColor(context.getResources().getColor(R.color.RedText));
			 //tiempoSucesoText.setTextColor(context.getResources().getColor(R.color.RedText));
		 }
		 
		 return item;
	}
}
