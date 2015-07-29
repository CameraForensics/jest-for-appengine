package uk.co.mattburns.jest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.searchbox.action.Action;
import io.searchbox.client.AbstractJestClient;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import org.apache.commons.lang3.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

public class JestForAppEngineClient implements JestClient {

    private URL host;

    protected Gson gson = new GsonBuilder()
            .setDateFormat(AbstractJestClient.ELASTIC_SEARCH_DATE_FORMAT)
            .create();

    public JestForAppEngineClient(URL host) {
        this.host = host;
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

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(payload);
            writer.close();

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

        return clientRequest.createNewElasticSearchResult(response, responseCode, reasonPhrase, gson);
    }

    @Override
    public <T extends JestResult> void executeAsync(Action<T> clientRequest, JestResultHandler<T> jestResultHandler) {
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
