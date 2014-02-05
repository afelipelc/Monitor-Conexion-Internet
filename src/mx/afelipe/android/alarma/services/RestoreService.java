package mx.afelipe.android.alarma.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class RestoreService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Toast.makeText(context, "Intentando reiniciar servicio de monitoreo de Internet",
		//		Toast.LENGTH_SHORT).show();
		context.startService(new Intent(context, MonitorService.class));
	}

}
