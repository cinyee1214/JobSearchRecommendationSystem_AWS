package com.laioffer.job.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.entity.Item;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;

import java.io.IOException;
//import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class GitHubClient {
    private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
    private static final String DEFAULT_KEYWORD = "developer";

    public List<Item> search(double lat, double lon, String keyword) {
        String url;
        if (keyword.equals("NA")) {
//            keyword = DEFAULT_KEYWORD;
            url = String.format("https://jobs.github.com/positions.json?lat=%s&long=%s", lat, lon);
        } else {
            try {
                keyword = URLEncoder.encode(keyword, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            url = String.format(URL_TEMPLATE, keyword, lat, lon);
        }

        System.out.println(url);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        // Create a custom response handler
        // <option + enter> on "response" can replace the lambda
        ResponseHandler<List<Item>> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                return Collections.emptyList();
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return Collections.emptyList();
            }

            ObjectMapper mapper = new ObjectMapper();
            
//            InputStream inputStream = entity.getContent();
//            Item[] items = mapper.readValue(inputStream, Item[].class);
//            return Arrays.asList(items);

            //return Arrays.asList(mapper.readValue(entity.getContent(), Item[].class));
            List<Item> items = Arrays.asList(mapper.readValue(entity.getContent(), Item[].class));
            // System.out.println(items);
            extractKeywords(items);
            // System.out.println(items);
            return items;
        };

        try {
            return httpclient.execute(new HttpGet(url), responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
        // better than return new ArrayList<>();
    }

    private void extractKeywords(List<Item> items) {
        MonkeyLearnClient monkeyLearnClient = new MonkeyLearnClient();

        // reactive programming (pipeline)
        List<String> descriptions = items.stream()
                .map(Item::getDescription)
                .collect(Collectors.toList());

        // otherwise use for-loop
//        List<String> descriptions = new ArrayList<>();
//        for (Item item : items) {
//            descriptions.add(item.getDescription());
//        }

        List<Set<String>> keywordList = monkeyLearnClient.extract(descriptions);
        for (int i = 0; i < items.size(); i++) {
            if (items.size() != keywordList.size() && i > keywordList.size() - 1) {
                items.get(i).setKeywords(new HashSet<>());
                continue;
            }
            items.get(i).setKeywords(keywordList.get(i));
        }
        // System.out.println("private" + items);
    }

}
