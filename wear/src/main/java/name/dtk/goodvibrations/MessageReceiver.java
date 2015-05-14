package name.dtk.goodvibrations;

import android.content.Context;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;


public class MessageReceiver extends WearableListenerService {

    private final static String TAG = MessageReceiver.class.getSimpleName();
    private final static String COMMAND = "VIBRATE";

    private Vibrator mVibrator;

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating Message Receiver");

        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        String command = event.getPath();
        String data = new String(event.getData(), Charset.forName("UTF-8"));

        long[] pattern;

        Log.d(TAG, String.format("Received %s message with data %s", command, data));

        try {
            pattern = deserializePattern(data);
        } catch (IOException | ClassNotFoundException error) {
            Log.e(TAG, String.format("Failed to deserialize pattern %s: %s", data, error));
            return;
        }

        if (command.equals(COMMAND)) {
            Log.d(
                TAG,
                String.format("Triggering vibration with pattern %s", Arrays.toString(pattern))
            );

            mVibrator.vibrate(pattern, -1);
        }
    }

    private long[] deserializePattern(String serializedPattern)
            throws IOException, ClassNotFoundException {

        ByteArrayInputStream in = new ByteArrayInputStream(
                Base64.decode(serializedPattern, Base64.DEFAULT)
        );

        return (long[]) new ObjectInputStream(in).readObject();
    }
}
