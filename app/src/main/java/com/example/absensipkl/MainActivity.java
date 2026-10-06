package com.example.absensipkl;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // INPUT
    private EditText etNama;
    private EditText etPembimbing;
    private EditText etTanggal;
    private EditText etTugas;

    // STATUS KEHADIRAN
    private RadioGroup rgStatus;

    // BUTTON
    private Button btnSimpan;
    private Button btnLihat;
    private Button btnHapus;
    private Button btnReset;

    // TEXTVIEW
    private TextView tvJumlah;
    private TextView tvRiwayat;

    // SHARED PREFERENCES
    private SharedPreferences mPreferences;

    private final String sharedPrefFile =
            "com.example.absensipkl";

    // FILE INTERNAL STORAGE
    private final String namaFile =
            "absensi_pkl.txt";

    // JUMLAH ABSENSI
    private int jumlahAbsensi = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        // =====================================================
        // HUBUNGKAN JAVA DENGAN XML
        // =====================================================

        etNama = findViewById(R.id.etNama);
        etPembimbing = findViewById(R.id.etPembimbing);
        etTanggal = findViewById(R.id.etTanggal);
        etTugas = findViewById(R.id.etTugas);

        rgStatus = findViewById(R.id.rgStatus);

        btnSimpan = findViewById(R.id.btnSimpan);
        btnLihat = findViewById(R.id.btnLihat);
        btnHapus = findViewById(R.id.btnHapus);
        btnReset = findViewById(R.id.btnReset);

        tvJumlah = findViewById(R.id.tvJumlah);
        tvRiwayat = findViewById(R.id.tvRiwayat);


        // =====================================================
        // SHARED PREFERENCES
        // =====================================================

        mPreferences =
                getSharedPreferences(
                        sharedPrefFile,
                        MODE_PRIVATE
                );


        // AMBIL DATA YANG PERNAH DISIMPAN

        String namaTersimpan =
                mPreferences.getString(
                        "nama",
                        ""
                );

        String pembimbingTersimpan =
                mPreferences.getString(
                        "pembimbing",
                        ""
                );

        jumlahAbsensi =
                mPreferences.getInt(
                        "count",
                        0
                );


        // TAMPILKAN DATA

        etNama.setText(namaTersimpan);
        etPembimbing.setText(pembimbingTersimpan);

        tampilkanJumlahAbsensi();


        // =====================================================
        // EVENT BUTTON
        // =====================================================

        btnSimpan.setOnClickListener(view -> {
            simpanAbsensi();
        });


        btnLihat.setOnClickListener(view -> {
            lihatRiwayat();
        });


        btnHapus.setOnClickListener(view -> {
            hapusRiwayat();
        });


        btnReset.setOnClickListener(view -> {
            resetIdentitas();
        });
    }


    // =========================================================
    // SIMPAN ABSENSI
    // =========================================================

    private void simpanAbsensi() {

        String nama =
                etNama.getText()
                        .toString()
                        .trim();

        String pembimbing =
                etPembimbing.getText()
                        .toString()
                        .trim();

        String tanggal =
                etTanggal.getText()
                        .toString()
                        .trim();

        String tugas =
                etTugas.getText()
                        .toString()
                        .trim();


        int selectedId =
                rgStatus.getCheckedRadioButtonId();


        // =====================================================
        // VALIDASI INPUT
        // =====================================================

        if (nama.isEmpty()
                || pembimbing.isEmpty()
                || tanggal.isEmpty()
                || tugas.isEmpty()
                || selectedId == -1) {

            Toast.makeText(
                    this,
                    "Lengkapi semua data absensi terlebih dahulu",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =====================================================
        // AMBIL STATUS KEHADIRAN
        // =====================================================

        RadioButton radioStatus =
                findViewById(selectedId);

        String status =
                radioStatus.getText()
                        .toString()
                        .trim();


        // =====================================================
        // FORMAT DATA YANG AKAN DISIMPAN
        // =====================================================

        String data =
                "Tanggal     : " + tanggal + "\n"
                        + "Nama        : " + nama + "\n"
                        + "Pembimbing  : " + pembimbing + "\n"
                        + "Status      : " + status + "\n"
                        + "Tugas       : " + tugas + "\n"
                        + "----------------------------------------\n\n";


        // =====================================================
        // INTERNAL STORAGE
        // =====================================================

        File file =
                new File(
                        getFilesDir(),
                        namaFile
                );


        try {

            // true = tambahkan data baru
            // tanpa menghapus data sebelumnya

            FileWriter writer =
                    new FileWriter(
                            file,
                            true
                    );


            writer.write(data);

            writer.flush();

            writer.close();


            // =================================================
            // TAMBAH JUMLAH ABSENSI
            // =================================================

            jumlahAbsensi++;


            // =================================================
            // SIMPAN NAMA, PEMBIMBING, DAN COUNT
            // KE SHAREDPREFERENCES
            // =================================================

            SharedPreferences.Editor editor =
                    mPreferences.edit();

            editor.putString(
                    "nama",
                    nama
            );

            editor.putString(
                    "pembimbing",
                    pembimbing
            );

            editor.putInt(
                    "count",
                    jumlahAbsensi
            );

            editor.apply();


            tampilkanJumlahAbsensi();


            Toast.makeText(
                    this,
                    "Absensi berhasil disimpan",
                    Toast.LENGTH_SHORT
            ).show();


            // =================================================
            // KOSONGKAN INPUT HARIAN
            // =================================================

            etTanggal.setText("");

            etTugas.setText("");

            rgStatus.clearCheck();


            // =================================================
            // LANGSUNG PERBARUI RIWAYAT
            // =================================================

            lihatRiwayat();


        } catch (IOException e) {

            Toast.makeText(
                    this,
                    "Gagal menyimpan absensi",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =========================================================
    // LIHAT RIWAYAT
    // =========================================================

    private void lihatRiwayat() {

        File file =
                new File(
                        getFilesDir(),
                        namaFile
                );


        if (!file.exists()) {

            tvRiwayat.setText(
                    "Belum ada data absensi."
            );

            return;
        }


        StringBuilder isiFile =
                new StringBuilder();


        try {

            BufferedReader reader =
                    new BufferedReader(
                            new FileReader(file)
                    );


            String baris;


            while ((baris = reader.readLine()) != null) {

                isiFile.append(baris);

                isiFile.append("\n");
            }


            reader.close();


            if (isiFile.length() == 0) {

                tvRiwayat.setText(
                        "Belum ada data absensi."
                );

            } else {

                tvRiwayat.setText(
                        isiFile.toString()
                );
            }


        } catch (IOException e) {

            tvRiwayat.setText(
                    "Gagal membaca data absensi."
            );
        }
    }


    // =========================================================
    // HAPUS RIWAYAT
    // =========================================================

    private void hapusRiwayat() {

        File file =
                new File(
                        getFilesDir(),
                        namaFile
                );


        if (!file.exists()) {

            Toast.makeText(
                    this,
                    "Belum ada riwayat absensi",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        boolean berhasil =
                deleteFile(namaFile);


        if (berhasil) {

            jumlahAbsensi = 0;


            // SIMPAN COUNT MENJADI 0

            SharedPreferences.Editor editor =
                    mPreferences.edit();

            editor.putInt(
                    "count",
                    0
            );

            editor.apply();


            tampilkanJumlahAbsensi();


            tvRiwayat.setText(
                    "Belum ada data absensi."
            );


            Toast.makeText(
                    this,
                    "Riwayat absensi berhasil dihapus",
                    Toast.LENGTH_SHORT
            ).show();


        } else {

            Toast.makeText(
                    this,
                    "Riwayat gagal dihapus",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    // =========================================================
    // RESET IDENTITAS
    // =========================================================

    private void resetIdentitas() {

        SharedPreferences.Editor preferencesEditor =
                mPreferences.edit();


        // HANYA HAPUS NAMA DAN PEMBIMBING
        // COUNT TIDAK IKUT DIHAPUS

        preferencesEditor.remove("nama");

        preferencesEditor.remove("pembimbing");

        preferencesEditor.apply();


        etNama.setText("");

        etPembimbing.setText("");


        Toast.makeText(
                this,
                "Identitas berhasil direset",
                Toast.LENGTH_SHORT
        ).show();
    }


    // =========================================================
    // TAMPILKAN JUMLAH ABSENSI
    // =========================================================

    private void tampilkanJumlahAbsensi() {

        tvJumlah.setText(
                "Total absensi tersimpan: "
                        + jumlahAbsensi
        );
    }


    // =========================================================
    // SIMPAN SHAREDPREFERENCES SAAT ACTIVITY DI-PAUSE
    // =========================================================

    @Override
    protected void onPause() {

        super.onPause();


        SharedPreferences.Editor preferencesEditor =
                mPreferences.edit();


        preferencesEditor.putString(
                "nama",
                etNama.getText()
                        .toString()
                        .trim()
        );


        preferencesEditor.putString(
                "pembimbing",
                etPembimbing.getText()
                        .toString()
                        .trim()
        );


        preferencesEditor.putInt(
                "count",
                jumlahAbsensi
        );


        preferencesEditor.apply();
    }
}