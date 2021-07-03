package com.example.callback.drowsiness;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import br.com.bloder.magic.view.MagicButton;

public class monitor extends Fragment {
    MagicButton b;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root;
        root = inflater.inflate(R.layout.activity_monitor_menu,container,false);
        b = (MagicButton) root.findViewById(R.id.magic_button);
        b.setMagicButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(root.getContext(),FaceTrackerActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                getActivity().finish();

            }
        });
        return root;

    }
}
