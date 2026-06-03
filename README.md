https://github.com/user-attachments/assets/2196c087-aa1f-47bb-b78f-a96235cbe891

# LAB 14 : Sauvegarde des données – SharedPreferences et fichiers (avec bonnes pratiques de sécurité)

Persistance locale sécurisée sous Android (Java) : SharedPreferences, EncryptedSharedPreferences, fichiers internes, cache et stockage externe app-specific.

---

## Prérequis

- Android Studio Hedgehog (ou plus récent)
- JDK 11 ou supérieur
- Un appareil ou émulateur API 24+
- Connexion Internet uniquement pour le premier Gradle sync (téléchargement des dépendances)

---

## Structure du projet

```
SecureStorageLabJava/
└── app/src/main/
    ├── AndroidManifest.xml
    ├── java/com/example/securestoragejava/
    │   ├── ui/
    │   │   └── MainActivity.java          ← écran principal
    │   ├── prefs/
    │   │   ├── AppPrefs.java              ← SharedPreferences (non sensible)
    │   │   └── SecurePrefs.java           ← EncryptedSharedPreferences (token)
    │   ├── files/
    │   │   ├── InternalTextStore.java     ← fichiers texte UTF-8 internes
    │   │   └── StudentsJsonStore.java     ← JSON interne (liste d'étudiants)
    │   ├── cache/
    │   │   └── CacheStore.java            ← stockage temporaire (cacheDir)
    │   ├── external/
    │   │   └── ExternalAppFilesStore.java ← export externe app-specific
    │   └── model/
    │       └── Student.java               ← modèle de données
    └── res/
        ├── layout/activity_main.xml
        └── values/{strings, themes, colors}.xml
```

---

## Étapes — Mise en place dans Android Studio

### Étape 1 — Créer le projet

1. Ouvrir Android Studio → **New Project**
2. Choisir **Empty Views Activity**
3. Remplir les champs :
   - **Name** : `SecureStorageLabJava`
   - **Package name** : `com.example.securestoragejava`
   - **Save location** : dossier de votre choix
   - **Language** : `Java`
   - **Minimum SDK** : `API 24`
4. Cliquer **Finish** et attendre la fin du Gradle sync initial

---

### Étape 2 — Ajouter la dépendance Security Crypto

Ouvrir `app/build.gradle` et ajouter dans le bloc `dependencies` :

```groovy
implementation "androidx.security:security-crypto:1.1.0-alpha06"
```

Le bloc complet doit ressembler à :

```groovy
dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation "androidx.security:security-crypto:1.1.0-alpha06"
}
```

Cliquer sur **Sync Now** (bandeau jaune en haut de l'éditeur) et attendre la fin.

---

### Étape 3 — Créer les packages

Dans le panneau **Project** (vue Android), faire un clic droit sur le package principal `com.example.securestoragejava` → **New > Package** pour créer les packages suivants :

| Package à créer | Rôle |
|---|---|
| `com.example.securestoragejava.ui` | Activité principale |
| `com.example.securestoragejava.prefs` | Préférences |
| `com.example.securestoragejava.files` | Fichiers internes |
| `com.example.securestoragejava.cache` | Cache temporaire |
| `com.example.securestoragejava.external` | Stockage externe |
| `com.example.securestoragejava.model` | Modèles de données |

---

### Étape 4 — Ajouter les fichiers Java

Pour chaque fichier, faire un clic droit sur le package cible → **New > Java Class** → coller le contenu.

#### 4.1 — `model/Student.java`

```java
package com.example.securestoragejava.model;

public class Student {
    public final int    id;
    public final String name;
    public final int    age;

    public Student(int id, String name, int age) {
        this.id   = id;
        this.name = name;
        this.age  = age;
    }
}
```

#### 4.2 — `prefs/AppPrefs.java`

Copier le contenu de `app/src/main/java/com/example/securestoragejava/prefs/AppPrefs.java`.

Points clés :
- `MODE_PRIVATE` : fichier accessible uniquement par l'application
- `apply()` (asynchrone) recommandé pour l'UI
- `commit()` (synchrone, retourne boolean) utile quand confirmation immédiate nécessaire

#### 4.3 — `prefs/SecurePrefs.java`

Copier le contenu de `.../prefs/SecurePrefs.java`.

Points clés :
- `MasterKey` utilise le **Keystore Android** (clé non exportable)
- Clés **et** valeurs sont chiffrées sur le disque (AES256-SIV / AES256-GCM)
- **Ne jamais logger le token** — afficher uniquement `token.length()`

#### 4.4 — `files/InternalTextStore.java`

Copier le contenu de `.../files/InternalTextStore.java`.

#### 4.5 — `files/StudentsJsonStore.java`

Copier le contenu de `.../files/StudentsJsonStore.java`.

#### 4.6 — `cache/CacheStore.java`

Copier le contenu de `.../cache/CacheStore.java`.

#### 4.7 — `external/ExternalAppFilesStore.java`

Copier le contenu de `.../external/ExternalAppFilesStore.java`.

#### 4.8 — `ui/MainActivity.java`

Copier le contenu de `.../ui/MainActivity.java`.

---

### Étape 5 — Remplacer le layout

Ouvrir `res/layout/activity_main.xml`, passer en mode **Code** (onglet en haut à droite), et remplacer tout le contenu par le fichier `activity_main.xml` fourni.

---

### Étape 6 — Mettre à jour `AndroidManifest.xml`

Vérifier que l'activité principale pointe bien vers `com.example.securestoragejava.ui.MainActivity` :

```xml
<activity
    android:name=".ui.MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

---

### Étape 7 — Mettre à jour `res/values/`

Remplacer (ou créer) les fichiers suivants :

**`strings.xml`**
```xml
<resources>
    <string name="app_name">SecureStorageLabJava</string>
</resources>
```

**`themes.xml`**
```xml
<resources>
    <style name="Theme.SecureStorageLabJava"
           parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <item name="colorPrimary">@color/purple_500</item>
        <item name="colorPrimaryVariant">@color/purple_700</item>
        <item name="colorOnPrimary">@color/white</item>
    </style>
</resources>
```

**`colors.xml`**
```xml
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

---

### Étape 8 — Compiler et lancer

1. Connecter un appareil (ou démarrer un émulateur API 24+)
2. Cliquer le bouton **Run ▶** (ou `Shift+F10`)
3. L'application se lance avec l'écran principal

---

## Utilisation de l'application

| Bouton | Action |
|---|---|
| **Sauvegarder prefs** | Enregistre nom / langue / thème via `AppPrefs` et le token via `SecurePrefs` (chiffré). Écrit aussi un résumé dans le cache. |
| **Charger prefs** | Lit les préférences et affiche `tokenLength` (jamais la valeur brute). |
| **Sauvegarder fichier JSON** | Génère `students.json` et `note.txt` dans le stockage interne. |
| **Charger fichier JSON** | Lit et affiche la liste des étudiants + la note. |
| **Exporter vers stockage externe** | Écrit `export.txt` dans `getExternalFilesDir()` et affiche le chemin absolu. |
| **Effacer tout** | Supprime prefs, secure prefs, fichiers internes, cache. Remet l'UI à zéro. |

---

## Vérification via Device File Explorer

1. Menu **View > Tool Windows > Device File Explorer**
2. Naviguer vers `/data/data/com.example.securestoragejava/`

| Chemin | Contenu attendu |
|---|---|
| `files/students.json` | Liste JSON des étudiants |
| `files/note.txt` | Message de confirmation UTF-8 |
| `cache/last_ui.txt` | Résumé de la dernière sauvegarde |
| `shared_prefs/app_prefs.xml` | Prefs en clair (nom, langue, thème) |
| `shared_prefs/secure_prefs` | Prefs chiffrées (clés et valeurs illisibles) |

---

## Vérification Logcat

Filtrer par tag `SecureStorageJava` dans Logcat :

```
D/SecureStorageJava: Prefs sauvegardées ok=true, name=Alice, lang=fr, theme=dark
D/SecureStorageJava: Prefs chargées name=Alice, lang=fr, theme=dark, tokenLength=32
D/SecureStorageJava: Fichiers internes écrits : students.json, note.txt
D/SecureStorageJava: Export externe écrit : /storage/.../export.txt
D/SecureStorageJava: Nettoyage terminé (aucune donnée sensible loggée).
```

**À vérifier impérativement** : aucune ligne ne doit contenir la valeur brute du token.

---

## Bonnes pratiques sécurité appliquées

1. **Aucun token/mot de passe en clair** dans les logs ou l'interface
2. **EncryptedSharedPreferences** pour tous les secrets (clés + valeurs chiffrées)
3. **MODE_PRIVATE** pour fichiers internes et SharedPreferences
4. **Token masqué** à l'écran (`inputType="textPassword"`)
5. **Nettoyage complet** : `clear()` prefs, `deleteFile()`, `purge()` cache
6. **Cache réservé au temporaire** régénérable (jamais de secrets)
7. **Export externe** limité à `getExternalFilesDir()` (app-specific, pas public)
8. **Exceptions gérées** sans fuite d'information (message générique à l'UI)
9. **Encodage UTF-8** imposé pour tous les fichiers texte
10. **Longueur du token** uniquement affiché (jamais la valeur)

---

## Dépannage

### Gradle sync échoue sur security-crypto
- Vérifier la connexion Internet lors du premier sync
- File > Invalidate Caches / Restart, puis re-sync
- Vérifier que le `repositories` de `settings.gradle` contient `google()` et `mavenCentral()`

### Crash `EncryptedSharedPreferences` au démarrage
- Vérifier que le Min SDK est bien `24` dans `build.gradle`
- Entourer l'appel dans un `try/catch Exception` et afficher un message générique

### Fichiers invisibles dans Device File Explorer
- Lancer l'application au moins une fois et cliquer "Sauvegarder fichier JSON"
- Vérifier que le bon device est sélectionné en haut du Device File Explorer
- Chemin : `/data/data/com.example.securestoragejava/files/`

### Liste JSON vide après "Charger fichier JSON"
- Cliquer d'abord "Sauvegarder fichier JSON" pour créer le fichier
- Si le fichier est corrompu, cliquer "Effacer tout" puis re-sauvegarder

### `Switch` deprecated (API 35+)
Remplacer `android.widget.Switch` par `com.google.android.material.switchmaterial.SwitchMaterial` dans le layout et dans `MainActivity.java` si vous ciblez API 35+.

---

## Récapitulatif des concepts

| Mécanisme | Classe | Usage |
|---|---|---|
| SharedPreferences | `AppPrefs` | Préférences UI non sensibles |
| EncryptedSharedPreferences | `SecurePrefs` | Secrets (tokens, clés API) |
| Fichier interne texte | `InternalTextStore` | Notes, logs locaux |
| Fichier interne JSON | `StudentsJsonStore` | Données structurées locales |
| Cache | `CacheStore` | Données temporaires régénérables |
| Stockage externe app-specific | `ExternalAppFilesStore` | Export contrôlé |

`apply()` → asynchrone, sans retour, recommandé pour l'UI  
`commit()` → synchrone, retourne `boolean`, utile si confirmation immédiate requise
