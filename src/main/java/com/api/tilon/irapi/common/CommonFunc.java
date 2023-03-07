package com.api.tilon.irapi.common;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class CommonFunc {

    public HttpEntity<String> apicaller(String url, String data) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> requestMessage = new HttpEntity<>(data, httpHeaders);

        return restTemplate.postForEntity(url, requestMessage, String.class);
    }

    public List<Map<String, Object>> dateparser(List<Map<String, Object>> list) {
        //value가 timestamp 타입일 때 변환하기
        for (int i = 0; i < list.size(); i++) {
            Iterator<Map.Entry<String, Object>> iterator = list.get(i).entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof Timestamp) {
                    list.get(i).put(key, String.valueOf(value));
                }
            }
        }
        return list;
    }

    public static List<Map<String, Object>> convertToTree(List<Map<String, Object>> data, String parent) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Object item : data) {
            String id = (String) ((Map<String, Object>) item).get("id");
            String text = (String) ((Map<String, Object>) item).get("text");
            String parentid = (String) ((Map<String, Object>) item).get("parent");
            Integer groupDepth = (Integer) ((Map<String, Object>) item).get("gDepth");
            if (parent.equals(parentid)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", id);
                node.put("text", text);
                node.put("children", convertToTree(data, id));
                if(groupDepth > 1){ //depth가 1이하일때 접혀있는 상태
                    node.put("opened", false);
                }else {
                    node.put("opened", true);
                }
                tree.add(node);
            }
        }
        return tree;
    }

    //option 1 : yyyyMMdd;
    //option 2 : yyyyMMdd_HHmmss;
    public String getDateToday(int dateFormatOption) {

        Date date = new Date();
        String dateFormat = "yyyyMMdd";

        if (dateFormatOption == 2) {
            dateFormat = "yyyyMMdd_HHmmss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        String dateIdentifier = formatter.format(date);

        return dateIdentifier;
    }


    public String pTagWrapper(String content) {
        String outputString = "";
        content = content.replaceAll("\n", "<br/>");
        content = content.replaceAll("'", "`");
        content = content.replaceAll("\\\\", "\\\\\\\\");

        outputString = "<p>" + content + "</p>";
        return outputString;
    }

    public List<Map<String, Object>> trimBothEndOfPtags(List<Map<String, Object>> list) {

        String contentKey = "content";
        if (list.size() > 0) {
            if (list.get(0).containsKey("content")) contentKey = "content";
            if (list.get(0).containsKey("Content")) contentKey = "Content";
            if (list.get(0).containsKey("CONTENT")) contentKey = "CONTENT";
            if (list.get(0).containsKey("contents")) contentKey = "contents";
            if (list.get(0).containsKey("Contents")) contentKey = "Contents";
            if (list.get(0).containsKey("CONTENTS")) contentKey = "CONTENTS";
            try {
                for (int i = 0; i < list.size(); i++) {
                    String trimmedContent = list.get(i).get(contentKey).toString();
                    if (list.get(i).get(contentKey).toString().length() > 3 && list.get(i).get(contentKey).toString().startsWith("<")) {
                        trimmedContent = trimmedContent.substring(3, trimmedContent.lastIndexOf("</p>"));
                        list.get(i).put(contentKey, trimmedContent);
                    }
                }
            } catch (StringIndexOutOfBoundsException ex) {
                ex.printStackTrace();
                return list;
            }
        }
        return list;
    }
}
