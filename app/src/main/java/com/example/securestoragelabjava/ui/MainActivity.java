package com.example.securestoragelabjava.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securestoragelabjava.R;
import com.example.securestoragelabjava.cache.CacheStore;
import com.example.securestoragelabjava.external.ExternalAppFilesStore;
import com.example.securestoragelabjava.files.InternalTextStore;
import com.example.securestoragelabjava.files.StudentsJsonStore;
import com.example.securestoragelabjava.model.Student;
import com.example.securestoragelabjava.prefs.AppPrefs;
import com.example.securestoragelabjava.prefs.SecurePrefs;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SecureStorageJava";
    private final List<String> langs = Arrays.asList("fr", "en", "ar");

    private EditText etName;
    private EditText etToken;
    private Spinner  spLang;
    private Switch   swDark;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName   = findViewById(R.id.etName);
        etToken  = findViewById(R.id.etToken);
        spLang   = findViewById(R.id.spLang);
        swDark   = findViewById(R.id.swDark);
        tvResult = findViewById(R.id.tvResult);

        setupLangSpinner();

        Button btnSavePrefs = findViewById(R.id.btnSavePrefs);
        Button btnLoadPrefs = findViewById(R.id.btnLoadPrefs);
        Button btnSaveJson  = findViewById(R.id.btnSaveJson);
        Button btnLoadJson  = findViewById(R.id.btnLoadJson);
        Button btnExport    = findViewById(R.id.btnExport);
        Button btnClear     = findViewById(R.id.btnClear);

        btnSavePrefs.setOnClickListener(v -> savePrefs());
        btnLoadPrefs.setOnClickListener(v -> loadPrefsToUi());
        btnSaveJson .setOnClickListener(v -> saveJsonFile());
        btnLoadJson .setOnClickListener(v -> loadJsonFile());
        btnExport   .setOnClickListener(v -> exportExternal());
        btnClear    .setOnClickListener(v -> clearAll());

        loadPrefsToUi();
    }

    private void setupLangSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, langs);
        spLang.setAdapter(adapter);
    }

    private void savePrefs() {
        String name  = etName.getText().toString().trim();
        String lang  = langs.get(Math.max(0, spLang.getSelectedItemPosition()));
        String theme = swDark.isChecked() ? "dark" : "light";

        boolean ok = AppPrefs.save(this, name, lang, theme, false);

        String token = etToken.getText().toString();
        if (!token.isBlank()) {
            try {
                SecurePrefs.saveToken(this, token);
            } catch (Exception e) {
                show("Token encryption error: " + e.getMessage());
                return;
            }
        }

        Log.d(TAG, "Prefs saved ok=" + ok + ", name=" + name + ", lang=" + lang + ", theme=" + theme);

        try {
            CacheStore.write(this, "last_ui.txt",
                    "name=" + name + ", lang=" + lang + ", theme=" + theme);
        } catch (Exception ignored) {}

        show("✅ Prefs saved.\nname="  + name
                + "\nlang="  + lang
                + "\ntheme=" + theme
                + "\ntoken: encrypted (not shown).");
    }

    private void loadPrefsToUi() {
        AppPrefs.Triple triple = AppPrefs.load(this);

        etName.setText(triple.name);
        swDark.setChecked("dark".equals(triple.theme));

        int idx = langs.indexOf(triple.lang);
        spLang.setSelection(idx >= 0 ? idx : 0);

        int tokenLen = 0;
        try {
            String token = SecurePrefs.loadToken(this);
            tokenLen = (token == null) ? 0 : token.length();
        } catch (Exception ignored) {}

        Log.d(TAG, "Prefs loaded name=" + triple.name + ", tokenLength=" + tokenLen);
        show("📂 Prefs loaded.\nname="  + triple.name
                + "\nlang="        + triple.lang
                + "\ntheme="       + triple.theme
                + "\ntokenLength=" + tokenLen);
    }

    private void saveJsonFile() {
        List<Student> students = Arrays.asList(
                new Student(1, "Amina", 20),
                new Student(2, "Omar",  21),
                new Student(3, "Sara",  19)
        );
        try {
            StudentsJsonStore.save(this, students);
            InternalTextStore.writeUtf8(this, "note.txt", "JSON saved (UTF-8).");
        } catch (Exception e) {
            show("JSON save error: " + e.getMessage());
            return;
        }
        Log.d(TAG, "Internal files written: students.json, note.txt");
        show("✅ JSON file saved.\nstudents=" + students.size());
    }

    private void loadJsonFile() {
        List<Student> students = StudentsJsonStore.load(this);
        String note;
        try {
            note = InternalTextStore.readUtf8(this, "note.txt");
        } catch (Exception e) {
            note = "(note.txt missing)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("📂 JSON loaded.\nnote=").append(note)
                .append("\nstudents=").append(students.size()).append("\n");
        for (Student s : students) {
            sb.append("  id=").append(s.id)
                    .append(" name=").append(s.name)
                    .append(" age=").append(s.age).append("\n");
        }
        Log.d(TAG, "JSON loaded: students=" + students.size());
        show(sb.toString());
    }

    private void exportExternal() {
        try {
            String path = ExternalAppFilesStore.write(this, "export.txt",
                    "External export — " + System.currentTimeMillis());
            if (path == null) { show("External storage unavailable."); return; }
            String content = ExternalAppFilesStore.read(this, "export.txt");
            Log.d(TAG, "External export written: " + path);
            show("✅ External export done.\nPath: " + path + "\nRead: " + content);
        } catch (Exception e) {
            show("External export error: " + e.getMessage());
        }
    }

    private void clearAll() {
        AppPrefs.clear(this);
        try { SecurePrefs.clear(this); } catch (Exception ignored) {}
        StudentsJsonStore.delete(this);
        InternalTextStore.delete(this, "note.txt");
        ExternalAppFilesStore.delete(this, "export.txt");
        int purged = CacheStore.purge(this);

        etName.setText("");
        etToken.setText("");
        swDark.setChecked(false);
        spLang.setSelection(0);

        Log.d(TAG, "Full cleanup done.");
        show("🗑️ All cleared.\n• prefs: clear\n• secure prefs: clear\n• students.json: deleted\n• note.txt: deleted\n• export.txt: deleted\n• cache purged: " + purged + " file(s)");
    }

    private void show(String text) {
        tvResult.setText(text);
    }
}