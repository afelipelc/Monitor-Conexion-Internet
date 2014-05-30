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

package mx.afelipe.android.alarma;

import java.util.ArrayList;
import java.util.List;

import mx.afelipe.android.alarma.model.Suceso;
import mx.afelipe.android.alarma.services.MonitorService;
import android.app.Application;
import android.media.MediaPlayer;

public class AlarmaConexionInternet extends Application {
	//private MonitorService monitorService;
	private boolean sonidoAlarma = false;
	private MediaPlayer player;
	
	public void setMediaPlayer(MediaPlayer player){
		this.player = player;
	}
	public MediaPlayer soundPlayer(){
		return player;
	}
	public boolean isSonandoAlarma() {
		//return monitorService!= null && monitorService.isSonandoAlarma();
		return sonidoAlarma;
	}
	public void setSonandoAlarma(boolean status){
		this.sonidoAlarma = status;
	}

	private List<Suceso> sucesos = new ArrayList<Suceso>();
	
	public List<Suceso> getSucesos() {
		return sucesos;
	}

	public void setSucesos(List<Suceso> sucesos) {
		this.sucesos = sucesos;
	}

//	public MonitorService getMonitorService() {
//		return monitorService;
//	}
//
//	public void setMonitorService(MonitorService monitorService) {
//		this.monitorService = monitorService;
//	}

	public AlarmaConexionInternet()
	{
		super();
	}

//	public boolean isEstatusAlarma() {
//		return monitorService!= null && monitorService.isMonitorActivado();
//	}

//	public String Estatus()
//	{
//		return isEstatusAlarma() ? "Activado" : "Apagado";
//	}
}
