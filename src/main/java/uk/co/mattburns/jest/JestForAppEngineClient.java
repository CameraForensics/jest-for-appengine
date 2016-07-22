package uk.co.mattburns.jest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.searchbox.action.Action;
import io.searchbox.client.AbstractJestClient;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class JestForAppEngineClient implements JestClient {

    private final URL host;
    private final String username;
    private final String password;

    private final Gson gson = new GsonBuilder()
            .setDateFormat(AbstractJestClient.ELASTIC_SEARCH_DATE_FORMAT)
            .create();

    /**
     * JestClient that works on App Engine
     *
     * @param host Url of Elasticsearch node including port and protocol. eg "http://example.com:9200/" or "https://1.1.1.1:9999/"
     */
    public JestForAppEngineClient(URL host) {
        this(host, null, null);
    }

    /**
     * JestClient that works on App Engine
     *
     * @param host Url of Elasticsearch node including port and protocol. eg "http://example.com:9200/" or "https://1.1.1.1:9999/"
     * @param username for basic auth (pass null to ignore)
     * @param password for basic auth (pass null to ignore)
     */
    public JestForAppEngineClient(URL host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    @Override
    public <T extends JestResult> T execute(Action<T> clientRequest) throws IOException {
        String payload = clientRequest.getData(gson);
        int responseCode = -1;
        String response = "";
        String reasonPhrase = "";
        try {
            URL url = new URL(host.toString() + clientRequest.getURI());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod(clientRequest.getRestMethodName());

            if (username != null && password != null) {
                // then set basic auth header
                connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes()));
            }

            if(payload != null) {
                //If there is no body data, this throws a null, unless we check for payload nullness
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(payload);
                writer.close();
            }

            responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                response += line;
            }
            reader.close();
        } catch (Exception e) {
            reasonPhrase += e.getMessage();
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            if (reasonPhrase.isEmpty()) {
                reasonPhrase = response;
                response = "";
            }
        }
        return clientRequest.createNewElasticSearchResult(response, responseCode, reasonPhrase, gson);
    }

    @Override
    public <T extends JestResult> void executeAsync(Action<T> clientRequest, JestResultHandler<? super T> jestResultHandler) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void shutdownClient() {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void setServers(Set<String> servers) {
        throw new NotImplementedException("TODO");
    }
}
