package com.app.networkscanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Activity2 extends Activity {
    RelativeLayout background;
    ImageView imageView;
    Button button;
    TextView textwait;
    TextView resultTextView;
    EditText edittext;
    private AdView mAdView;
    private ExecutorService executor;
    private Handler handler;
    private boolean foundOpenPort;
    private InterstitialAd mInterstitialAd;

    // Массив портов для сканирования
    private static final int[] PORTS_TO_SCAN = {443, 80, 8080, 22, 135, 139, 5555};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // Обработка успешной инициализации рекламы (если требуется).
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        button = findViewById(R.id.ScanButton);
        textwait = findViewById(R.id.textwait);
        resultTextView = findViewById(R.id.resultTextView);
        edittext = findViewById(R.id.edittext);
        imageView = findViewById(R.id.imageView);
        background = findViewById(R.id.background);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                background.setBackgroundResource(R.drawable.white);
            }
        });

        executor = Executors.newFixedThreadPool(10);
        handler = new Handler();
        foundOpenPort = false;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textwait.setText("Scanning...");
                resultTextView.setText("");

                String ipAddress = edittext.getText().toString();
                scanPorts(ipAddress);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Показ рекламы (замените "YOUR_INTERSTITIAL_AD_UNIT_ID" на свой Ad Unit ID)
                InterstitialAd.load(Activity2.this, "ca-app-pub-3940256099942544/1033173712", adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        if (mInterstitialAd != null) {
                            mInterstitialAd.show(Activity2.this);
                            // Установите обработчики событий для рекламы (onAdDismissedFullScreenContent и т. д.)
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Обработка ошибки загрузки рекламы
                    }
                });
            }
        }, 7000); // 7 секунд (7000 миллисекунд)
    }

    private void scanPorts(String ipAddress) {
        for (int port : PORTS_TO_SCAN) {
            final int currentPort = port;

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(ipAddress, currentPort), 100);
                        socket.close();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String currentResult = "Port " + currentPort + " is open\n";
                                resultTextView.append(currentResult);
                            }
                        });

                        foundOpenPort = true;
                    } catch (IOException e) {
                        // Обработка ошибки при подключении.
                    }

                    if (currentPort == PORTS_TO_SCAN[PORTS_TO_SCAN.length - 1] && !foundOpenPort) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                resultTextView.setText("No open ports found.");
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}