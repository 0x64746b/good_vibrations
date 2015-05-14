package name.dtk.goodvibrations;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;


public class VibrationPreference extends DialogPreference {

    private final static String TAG = VibrationPreference.class.getSimpleName();
    private final static String DEFAULT_PATTERN =
        "rO0ABXVyAAJbSnggBLUSsXWTAgAAeHAAAAAEAAAAAAAAA+gAAAAAAAAC9wAAAAAAAAH8AAAAAAAA";

    private long[] mInitialValues = new long[] {2000, 1000, 500, 1000};

    private SeekBar mDelaySlider;
    private SeekBar mVibrationSlider1;
    private SeekBar mPauseSlider;
    private SeekBar mVibrationSlider2;

    public VibrationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.vibration_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String serializedPattern;

        if (restorePersistedValue) {
            serializedPattern = getPersistedString(DEFAULT_PATTERN);
        } else {
            serializedPattern = (String) defaultValue;
            persistString(serializedPattern);
        }

        try {
            mInitialValues = deserializePattern(serializedPattern);
        } catch (IOException | ClassNotFoundException error) {
            Log.e(
                TAG,
                String.format("Failed to deserialize pattern %s: %s", serializedPattern, error)
            );
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        View dialog = LayoutInflater.from(getContext()).inflate(R.layout.vibration_dialog, null);

        mDelaySlider = setupSlider(
            dialog,
            R.id.delaySlider,
            R.id.delayValue,
            (int) mInitialValues[0]
        );

        mVibrationSlider1 = setupSlider(
            dialog,
            R.id.vibrationSlider1,
            R.id.vibrationValue1,
            (int) mInitialValues[1]
        );

        mPauseSlider = setupSlider(
            dialog,
            R.id.pauseSlider,
            R.id.pauseValue,
            (int) mInitialValues[2]
        );

        mVibrationSlider2 = setupSlider(
            dialog,
            R.id.vibrationSlider2,
            R.id.vibrationValue2,
            (int) mInitialValues[3]
        );

        builder.setView(dialog);
    }

    private SeekBar setupSlider(View dialog, int sliderID, int labelID, int initialValue) {
        SeekBar slider = (SeekBar) dialog.findViewById(sliderID);
        TextView label = (TextView) dialog.findViewById(labelID);

        slider.setOnSeekBarChangeListener(new SliderListener(label));
        slider.setProgress(initialValue);

        return slider;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String preference = DEFAULT_PATTERN;

            mInitialValues[0] = mDelaySlider.getProgress();
            mInitialValues[1] = mVibrationSlider1.getProgress();
            mInitialValues[2] = mPauseSlider.getProgress();
            mInitialValues[3] = mVibrationSlider2.getProgress();

            try {
                preference = serializePattern(mInitialValues);
            } catch (IOException error) {
                Log.e(
                    TAG,
                    String.format(
                        "Failed to serialize pattern %s: %s. Using default pattern instead.",
                        Arrays.toString(mInitialValues),
                        error
                    )
                );
            }

            persistString(preference);
        }
    }

    private String serializePattern(long[] pattern) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ObjectOutputStream(out).writeObject(pattern);

        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
    }

    private long[] deserializePattern(String serializedPattern)
            throws IOException, ClassNotFoundException {

        ByteArrayInputStream in = new ByteArrayInputStream(
                Base64.decode(serializedPattern, Base64.DEFAULT)
        );

        return (long[]) new ObjectInputStream(in).readObject();
    }


    public static class SliderListener implements SeekBar.OnSeekBarChangeListener {

        TextView mLabel;

        public SliderListener(TextView label) {
            mLabel = label;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mLabel.setText(String.valueOf(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

}
