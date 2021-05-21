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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class AdzunaClient {
    private static final String URL_TEMPLATE = "http://api.adzuna.com/v1/api/jobs/us/search/1?app_id=%s&app_key=%s&where=%s&what=%s&content-type=application/json";
    private static final String DEFAULT_KEYWORD = "developer";
    private static final String API_ID = "843d08c9";
    private static final String API_KEY = "ec758f0c0602f297a5a9ede505e7f772";

    public List<Item> search(double lat, double lon, String keyword) {
        String url;
        String location;

        System.out.println(lat);
        System.out.println(lon);
        System.out.println(keyword);
        
        location = getLocation(lat, lon);
        System.out.println(location);
        System.out.println("line 42");

        try {
            location = URLEncoder.encode(location, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (keyword.equals("NA")) {
//            keyword = DEFAULT_KEYWORD;
            url = String.format("http://api.adzuna.com/v1/api/jobs/us/search/1?app_id=%s&app_key=%s&where=%s&content-type=application/json", API_ID, API_KEY, location);
        } else {
            try {
                keyword = URLEncoder.encode(keyword, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            url = String.format(URL_TEMPLATE, API_ID, API_KEY, location, keyword);
        }


        System.out.println(url);
        System.out.println("line 46");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println("line 49");
        // Create a custom response handler
        // <option + enter> on "response" can replace the lambda
        String finalLocation = location;
        ResponseHandler<List<Item>> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("line 55");
                return Collections.emptyList();

            }
            System.out.println("line 58");
            HttpEntity entity = response.getEntity();
            System.out.println("line 60");
            if (entity == null) {
                System.out.println("line 61");
                return Collections.emptyList();
            }
            System.out.println("line 63");
            String retSrc = EntityUtils.toString(entity);
            // parsing JSON
            System.out.println("line 66");
            JSONObject result = new JSONObject(retSrc); //Convert String to JSON Object
            System.out.println("line 68");
            JSONArray arr = result.getJSONArray("results");
            System.out.println("line 62");
            System.out.println(arr);

            List<Item> items = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++)
            {
                JSONObject job = arr.getJSONObject(i);
                System.out.println("In the for look");
                items.add(new Item.Builder().id(job.getString("id"))
                                              .title(job.getString("title"))
                                              .description(job.getString("description"))
                                              .url(job.getString("redirect_url"))
                                              .location(finalLocation).build());
            }

//            JSONObject sub_result = arr.getJSONObject(0);
//            JSONArray sub_arr = sub_result.getJSONArray("address_components");
//            String city = sub_arr.getJSONObject(3).getString("long_name");
//            System.out.println(city);
//            System.out.println("The adzuna result is");
//            System.out.println(result);
//
//            ObjectMapper mapper = new ObjectMapper();
            
//            InputStream inputStream = entity.getContent();
//            Item[] items = mapper.readValue(inputStream, Item[].class);
//            return Arrays.asList(items);

            //return Arrays.asList(mapper.readValue(entity.getContent(), Item[].class));
//            List<Item> items = Arrays.asList(mapper.readValue(entity.getContent(), Item[].class));
            // System.out.println(items);
            System.out.println("The first item of search function is");
            System.out.println(items.get(0));
            extractKeywords(items);
            System.out.println("extracted Keywords");
            return items;
        };

        try {
            System.out.println("line 110");
            return httpclient.execute(new HttpGet(url), responseHandler);
        } catch (IOException e) {
            System.out.println("line 113");
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
        System.out.println("line 146");
        System.out.println(descriptions.get(0));
        // otherwise use for-loop
//        List<String> descriptions = new ArrayList<>();
//        for (Item item : items) {
//            descriptions.add(item.getDescription());
//        }

        List<Set<String>> keywordList = monkeyLearnClient.extract(descriptions);

        System.out.println("line 156");
        System.out.println(keywordList.get(0).size());

        for (int i = 0; i < items.size(); i++) {
            if (items.size() != keywordList.size() && i > keywordList.size() - 1) {
                items.get(i).setKeywords(new HashSet<>());
                continue;
            }
            items.get(i).setKeywords(keywordList.get(i));
        }
        // System.out.println("private" + items);
    }

    private String getLocation(double lat, double lon) {

        System.out.println("Retriving location");

        String URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=AIzaSyBm3xvswhlBetZILgHZ8j2MpoHhPlW_WzA";

        String url2 = String.format(URL, lat, lon);
        System.out.println(url2);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        System.out.println("line 179");
        // Create a custom response handler
        // <option + enter> on "response" can replace the lambda
        ResponseHandler<String> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                return "";
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return "";
            }
            System.out.println("line 190");
            String retSrc = EntityUtils.toString(entity);
            System.out.println("line 191");
            // parsing JSON
            JSONObject result = new JSONObject(retSrc); //Convert String to JSON Object
            System.out.println(result);

            JSONArray arr = result.getJSONArray("results");
            JSONObject sub_result = arr.getJSONObject(0);
            JSONArray sub_arr = sub_result.getJSONArray("address_components");
            String city = sub_arr.getJSONObject(3).getString("long_name");
            System.out.println("The city retrived is");
            System.out.println(city);
            return city;
        };

        try {
            return httpclient.execute(new HttpGet(url2), responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "New York";
    }

//    public static void main(String[] args){
//        List<Item> results = search(40.71,-73.96, "developer");
//        System.out.println(results.get(0));
//    }

}
