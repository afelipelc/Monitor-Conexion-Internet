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

package mx.afelipe.android.alarma.model;

import java.util.Date;


public class Suceso {
	private String titulo;
	private Date tiempo;
	private boolean status;
	
	public Suceso()
	{
		
	}
	
	public Suceso(String title, Date time, boolean status)
	{
		this.titulo = title;
		this.tiempo = time;
		this.status = status;
	}
	
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public Date getTiempo() {
		return tiempo;
	}
	public void setTiempo(Date tiempo) {
		this.tiempo = tiempo;
	}
}
