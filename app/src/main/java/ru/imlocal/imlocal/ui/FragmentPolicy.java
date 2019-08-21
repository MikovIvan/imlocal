package ru.imlocal.imlocal.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.imlocal.imlocal.R;


public class FragmentPolicy extends Fragment {
    public String policy_url = "https://imlocal.ru/policy_mobile.html";
    public FragmentPolicy() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static String downloadUrl(String address) throws Exception
    {
        // Скачать URL в строку
        HttpURLConnection connection = null;
        URL url = new URL(address);
        connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        assert (connection.getResponseCode() > 0);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_policy, container, false);
        // Сообщаем о загрузке
        TextView tv = (TextView) v.findViewById(R.id.policy_text);
        tv.setText(Html.fromHtml("<!DOCTYPE html><html><title></title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><link rel=\"stylesheet\" href=\"https://www.w3schools.com/w3css/4/w3mobile.css\"><body><div class=\"w3-container w3-card\"><br><br><br><h1>Загрузка...</h1></div></body>"));
        // Поток для сетевых операций
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Скачать
                String html = "Ошибка при загрузке политики конфиденциальности.";
                try {
                    html = downloadUrl(policy_url);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Отобразить
                final String htmlx = html;
                Context ctx = getActivity();
                ((FragmentActivity) ctx).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tv.setText(Html.fromHtml(htmlx, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            tv.setText(Html.fromHtml(htmlx));
                        }
                    }
                });
            }
        }).start();

        return v;
    }
}
