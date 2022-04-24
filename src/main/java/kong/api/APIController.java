package kong.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import kong.FindAndForwardApplication;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class APIController
{
    private static final String APPLICATION_NAME = "Shopper Panel AutoSender";
    private static final String USER_ID = "me";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_MODIFY);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private NetHttpTransport httpTransport;
    private Gmail service;


    public APIController()
    {
        try
        {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credentials = getCredentials(httpTransport);

            service = new Gmail.Builder(httpTransport, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        catch (GeneralSecurityException | IOException e)
        {
            e.printStackTrace();
        }
    }

    private Credential getCredentials(NetHttpTransport httpTransport)
    {
        Credential credential = null;

        try
        {
            InputStream inputStream = new FileInputStream(new File(CREDENTIALS_FILE_PATH));

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));

            GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            credential =
                    new AuthorizationCodeInstalledApp(googleAuthorizationCodeFlow, receiver).authorize("user");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return credential;
    }
}
