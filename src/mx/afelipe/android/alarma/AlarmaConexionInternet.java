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

package mx.afelipe.android.alarma;

import java.util.ArrayList;
import java.util.List;

import mx.afelipe.android.alarma.model.Suceso;
import mx.afelipe.android.alarma.services.MonitorService;
import android.app.Application;

public class AlarmaConexionInternet extends Application {
	private MonitorService monitorService;
	
	public boolean isSonandoAlarma() {
		return monitorService!= null && monitorService.isSonandoAlarma();
	}


	private List<Suceso> sucesos = new ArrayList<Suceso>();
	
	public List<Suceso> getSucesos() {
		return sucesos;
	}

	public void setSucesos(List<Suceso> sucesos) {
		this.sucesos = sucesos;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	public AlarmaConexionInternet()
	{
		super();
	}

	public boolean isEstatusAlarma() {
		return monitorService!= null && monitorService.isMonitorActivado();
	}

	public String Estatus()
	{
		return isEstatusAlarma() ? "Activado" : "Apagado";
	}
}