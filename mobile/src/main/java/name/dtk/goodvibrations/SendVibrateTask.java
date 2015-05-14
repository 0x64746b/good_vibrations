package name.dtk.goodvibrations;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;

public class SendVibrateTask
        extends AsyncTask<Void, Void, Void>
        implements ResultCallback<MessageApi.SendMessageResult> {

    private final static String TAG = SendVibrateTask.class.getSimpleName();
    private final static String COMMAND = "VIBRATE";

    private GoogleApiClient mClient;
    private String mPattern;


    public SendVibrateTask(GoogleApiClient client, String pattern) {
        mClient = client;
        mPattern = pattern;
    }

    protected Void doInBackground(Void... voids) {

        Log.d(TAG, String.format("Sending %s command...", COMMAND));

        Collection<String> nodes = getNodes();

        if (nodes.size() == 0) {
            Log.i(TAG, "No devices connected");
            return null;
        }

        for (String nodeID: nodes) {
            Log.d(TAG, String.format("Commanding node %s to vibrate", nodeID));
            sendVibrateMessage(nodeID);
        }

        return null;
    }

    private Collection<String> getNodes() {
        HashSet<String> nodeIDs = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mClient).await();

        for (Node node: nodes.getNodes()) {
            nodeIDs.add(node.getId());
        }

        return nodeIDs;
    }

    private void sendVibrateMessage(String nodeId) {
        Wearable.MessageApi.sendMessage(
                mClient,
                nodeId,
                SendVibrateTask.COMMAND,
                mPattern.getBytes(Charset.forName("UTF-8"))
        ).setResultCallback(this);
    }

    @Override
    public void onResult(MessageApi.SendMessageResult result) {
        com.google.android.gms.common.api.Status status = result.getStatus();

        if (!status.isSuccess()) {
            Log.e(
                TAG,
                String.format("Sending message failed with status code %s", status.getStatusCode())
            );
        } else {
            Log.d(
                TAG,
                String.format(
                        "Received result for message: [%d] %s ",
                        status.getStatusCode(),
                        status.getStatusMessage()
                )
            );
        }
    }
}

