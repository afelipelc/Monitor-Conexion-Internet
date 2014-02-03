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
