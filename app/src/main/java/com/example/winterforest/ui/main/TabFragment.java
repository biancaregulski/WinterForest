package com.example.winterforest.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.winterforest.R;


public class TabFragment extends Fragment {

    int position;
    public TextView description1, description2;
    public ImageView exampleImage;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        TabFragment tabFragment = new TabFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_how_to_play, container, false);
        description1 = root.findViewById(R.id.descr1);
        description2 = root.findViewById(R.id.descr2);
        exampleImage = root.findViewById(R.id.exampleImage);
        if (position == 0) {
            description1.setText(R.string.how_to_play_descr1);
            description2.setText(R.string.how_to_play_descr2);
            exampleImage.setImageResource(R.drawable.example_image_transparent);
        }
        else if (position == 1) {
            description1.setText(R.string.how_to_play_descr3);
            description2.setText(R.string.how_to_play_descr4);
            exampleImage.setImageResource(R.drawable.example_image_transparent2);
        }
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
