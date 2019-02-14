package ca.bischke.apps.filevault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private final String TAG = "FileVault";
    private final String STORAGE_ROOT = Environment.getExternalStorageDirectory().toString();
    private final String STORAGE_VAULT = STORAGE_ROOT + File.separator + "FileVault";
    private String currentDirectory;
    private boolean sortByName = true;

    private IvParameterSpec iv;
    private byte[] salt;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Sets Activity Layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adds the Toolbar to the Layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        createVaultDirectory();

        try
        {
            encrypt("SHIBA", new File(STORAGE_VAULT + "/corgi.jpg"), new File(STORAGE_VAULT + "/corgi2.jpg"));
            decrypt("SHIBA", new File(STORAGE_VAULT + "/corgi2.jpg"), new File(STORAGE_VAULT + "/corgi3.jpg"));
        }
        catch (Exception ex)
        {
            Log.d(TAG, ex.getMessage());
        }

        listFiles(STORAGE_ROOT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Adds Menu to the Toolbar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        // Handles Menu item clicks
        switch(id)
        {
            case R.id.action_settings:
                break;
            case R.id.action_by_name:
                sortByName = true;
                listFiles(currentDirectory);
                break;
            case R.id.action_by_date:
                sortByName = false;
                listFiles(currentDirectory);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        // Handles NavigationView item clicks
        switch(id)
        {
            case R.id.nav_file_explorer:
                break;
            case R.id.nav_settings:
                break;
            default:
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            listFiles(getParentDirectory(currentDirectory));
        }
    }

    private ArrayList<File> getSortedFiles(ArrayList<File> files)
    {
        if (sortByName)
        {
            return getFilesSortedByName(files);
        }
        else
        {
            return getFilesSortedByDate(files);
        }
    }

    private ArrayList<File> getFilesSortedByName(ArrayList<File> files)
    {
        Collections.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File file1, File file2)
            {
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });

        return files;
    }

    private ArrayList<File> getFilesSortedByDate(ArrayList<File> files)
    {
        Collections.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File file1, File file2)
            {
                long f1 = file1.lastModified();
                long f2 = file2.lastModified();

                if (f1 > f2)
                {
                    return -1;
                }
                else if (f1 < f2)
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        });

        return files;
    }

    private void listFiles(String path)
    {
        Log.d(TAG, "Listing Files in " + path);
        currentDirectory = path;
        clearLayoutFiles();
        scrollToTop();
        displayCurrentDirectory();

        File directory = new File(path);
        File[] fileArray = directory.listFiles();
        ArrayList<File> files = new ArrayList<>(Arrays.asList(fileArray));

        // Sort Files Alphabetically
        getSortedFiles(files);

        final LinearLayout layoutFiles = findViewById(R.id.layout_files);

        for (File file : files)
        {
            final FileLayout fileLayout = new FileLayout(this, file);

            final File file1 = file;
            final boolean isDirectory = file.isDirectory();
            final String newPath = path + File.separator + file.getName();

            fileLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (isDirectory)
                    {
                        listFiles(newPath);
                    }
                    else
                    {
                        // TODO: CONFIGURE TO ONLY DO THIS IF IT IS AN IMAGE FILE
                        startImageViewActivity(file1);

                        // TODO: Setup Encrypt Button to execute moveFileToVaultDirectory(file1)
                    }
                }
            });

            layoutFiles.addView(fileLayout);
        }
    }

    public void startImageViewActivity(File file)
    {
        Intent intent = new Intent(this, ViewImageActivity.class);
        intent.putExtra("FILE_PATH", file.getAbsolutePath());
        startActivity(intent);
    }

    private String getParentDirectory(String path)
    {
        if (path.equals(STORAGE_ROOT))
        {
            return STORAGE_ROOT;
        }

        if (path.length() > 0)
        {
            int endIndex = path.lastIndexOf('/');

            if (endIndex != -1)
            {
                return path.substring(0, endIndex);
            }
        }

        return STORAGE_ROOT;
    }

    private String getCurrentDirectory()
    {
        if (currentDirectory.equals(STORAGE_ROOT))
        {
            return getString(R.string.internal_storage);
        }
        else
        {
            int startIndex = currentDirectory.lastIndexOf('/') + 1;
            return currentDirectory.substring(startIndex, currentDirectory.length());
        }
    }

    private void clearLayoutFiles()
    {
        LinearLayout layoutFiles = findViewById(R.id.layout_files);

        if (layoutFiles.getChildCount() > 0)
        {
            layoutFiles.removeAllViews();
        }
    }

    private void scrollToTop()
    {
        ScrollView scrollView = findViewById(R.id.scrollview);
        scrollView.scrollTo(0, 0);
    }

    private void displayCurrentDirectory()
    {
        setTitle(getCurrentDirectory());
    }

    private void createVaultDirectory()
    {
        File directory = new File(STORAGE_VAULT);

        if (!directory.exists())
        {
            if (directory.mkdirs())
            {
                Log.d(TAG, "Vault Directory created");
            }
            else
            {
                Log.d(TAG, "Vault Directory could not be created");
            }
        }
        else
        {
            Log.d(TAG, "Vault Directory already exists");
        }
    }

    private void moveFileToVaultDirectory(File file)
    {
        File directory = new File(STORAGE_VAULT);

        if (!directory.exists())
        {
            createVaultDirectory();
        }

        String fileName = file.getName();
        File to = new File(directory.toString() + File.separator + fileName);

        if (file.renameTo(to))
        {
            Log.d(TAG, fileName + " moved to Vault Directory");
        }
        else
        {
            Log.d(TAG, fileName + " could not be moved");
        }
    }

    private byte[] getFileContent(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] fileContent = null;

        try
        {
            fileInputStream = new FileInputStream(file);

            fileContent = new byte[(int)file.length()];
            fileInputStream.read(fileContent);
        }
        catch (FileNotFoundException e)
        {
            Log.d(TAG, "File not found");
        }
        catch (IOException e)
        {
            Log.d(TAG, "Exception when reading file");
        }
        finally
        {
            if (fileInputStream != null)
            {
                try
                {
                    fileInputStream.close();
                }
                catch (IOException e)
                {
                    Log.d(TAG, "Exception when closing FileInputStream");
                }
            }
        }

        return fileContent;
    }

    private IvParameterSpec getIV()
    {
        if (iv == null)
        {
            String ivPreference = "FV-IV";
            SharedPreferences sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);

            // if SharedPreference already exists
            if (sharedPreferences.contains(ivPreference))
            {
                String ivString = sharedPreferences.getString(ivPreference, null);
                byte[] ivBytes = ivString.getBytes();

                iv = new IvParameterSpec(ivBytes);
                return iv;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            byte[] ivBytes = generateRandomByteArray(16);
            String ivString = new String(ivBytes);
            editor.putString(ivPreference, ivString);
            editor.apply();

            iv = new IvParameterSpec(ivBytes);
            return iv;
        }

        return iv;
    }

    private byte[] getSalt()
    {
        if (salt == null)
        {
            String saltPreference = "FV-Salt";
            SharedPreferences sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);

            // if SharedPreference already exists
            if (sharedPreferences.contains(saltPreference))
            {
                String saltString = sharedPreferences.getString(saltPreference, null);
                salt = saltString.getBytes();

                return salt;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            salt = generateRandomByteArray(16);
            String saltString = new String(salt);
            editor.putString(saltPreference, saltString);
            editor.apply();

            return salt;
        }

        return salt;
    }

    private byte[] generateRandomByteArray(int size)
    {
        SecureRandom random = new SecureRandom();
        byte[] randomByteArray = new byte[size];
        random.nextBytes(randomByteArray);

        return randomByteArray;
    }

    private SecretKey generateKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 10000;
        int outputLength = 128;

        char[] passwordArray = password.toCharArray();
        KeySpec keySpec = new PBEKeySpec(passwordArray, salt, iterations, outputLength);

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

        return secretKey;
    }

    public void encrypt(String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        doCrypto(Cipher.ENCRYPT_MODE, password, inputFile, outputFile);
    }

    public void decrypt(String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        doCrypto(Cipher.DECRYPT_MODE, password, inputFile, outputFile);
    }

    public void doCrypto(int cipherMode, String password, File inputFile, File outputFile)
            throws GeneralSecurityException, IOException
    {
        SecretKey secretKey = generateKey(password, getSalt());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(cipherMode, secretKey, getIV());

        FileInputStream inputStream = new FileInputStream(inputFile);
        byte[] inputBytes = new byte[(int) inputFile.length()];
        inputStream.read(inputBytes);

        byte[] outputBytes = cipher.doFinal(inputBytes);

        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(outputBytes);

        inputStream.close();
        outputStream.close();
    }
}
