package com.api.tilon.irapi.service;

import com.api.tilon.irapi.common.CommonFunc;
import com.api.tilon.irapi.dao.NoticeDao;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class NoticeService {

    @Autowired
    NoticeDao noticeDao;
    @Autowired
    CommonFunc commonFunc;
    @Value("${file.url}")
    private String fileurl;


    public JSONObject getNoticeList(JSONObject param) {
        JSONObject result = new JSONObject();

        // 파라미터로 넘겨 받은 countsPerPage를 인티저로 변환해서 변수에 저장
        int countsPerPage = Integer.parseInt(param.get("countsPerPage").toString());
        int pageNumber;

        // 만약 pageNumber 파라미터 값이 아무것도 없으면, pageNumber는 1로 세팅
        if (param.get("pageNumber").toString().equals("")) {
            pageNumber = 1;
        } else {
            // pageNumber가 있을 경우 넘어온 값으로 저장
            pageNumber = Integer.parseInt(param.get("pageNumber").toString());
            if (pageNumber <= 0) {
                pageNumber = 1;
            }
        }


        int limitCount = countsPerPage;
        int rowNum = (pageNumber * limitCount) - limitCount;

        param.put("rowNum", rowNum);
        param.put("limitCount", limitCount);


        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = noticeDao.getNoticeList(param);
            for(Map<String, Object> idx : list){
                if(idx.get("FilePath") != null && !String.valueOf(idx.get("FilePath")).equals("")){
                    idx.put("FilePath", fileurl+idx.get("FilePath").toString());
                }
            }
            list = commonFunc.trimBothEndOfPtags(list);
            list = commonFunc.dateparser(list);
            result.put("status", "0000");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "0001");
        }
        result.put("output", list);
        return result;
    }

    public JSONObject getSingleNotice(JSONObject param) {
        JSONObject result = new JSONObject();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            list = noticeDao.getSingleNotice(param);
            for(Map<String, Object> idx : list){
                if(idx.get("FilePath") != null && !String.valueOf(idx.get("FilePath")).equals("")){
                    idx.put("FilePath", fileurl+idx.get("FilePath").toString());
                }
            }
//            list = commonFunc.trimBothEndOfPtags(list);
//            list = commonFunc.dateparser(list);
            result.put("status", "0000");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "0001");
        }
        result.put("output", list);
        return result;
    }

    public JSONObject putUpNotice(JSONObject jsonParam) {
        JSONObject result = new JSONObject();

        Map<String, Object> singleRowData = new HashMap<>();
        List<Map<String, Object>> output = new ArrayList<>();

        try {
            noticeDao.putUpNotice(jsonParam);
            //그 사람이 쓴 공지사항중 seq가 가장최근인 행의 seq를 select해서 리턴
            int noticeIdx = noticeDao.selectSeqById(jsonParam.get("userid").toString());
            singleRowData.put("seq", noticeIdx);
            output.add(singleRowData);

            result.put("output", output);
            result.put("status", "0000");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "0001");
        }
        return result;
    }

    public JSONObject getNumberOfPages(JSONObject jsonObject) {
        JSONObject result = new JSONObject();
        //전체 건수 조회
        jsonObject.put("rowNum", 0);
        jsonObject.put("limitCount", 999999);
        int countsPerPage = Integer.parseInt(jsonObject.get("countsPerPage").toString());

        Map<String, Object> singleRowData = new HashMap<>();
        List<Map<String, Object>> output = new ArrayList<>();

        int pageCounts = 1;
        try {
            int recordCounts = noticeDao.getNoticeList(jsonObject).size();
            System.out.println(recordCounts);
            pageCounts = recordCounts / countsPerPage;
            if (recordCounts % countsPerPage > 0) {
                pageCounts++;
            }
            singleRowData.put("rowCount", recordCounts);
            singleRowData.put("pageCount", pageCounts);
            singleRowData.put("totalRowCount", getTotalRowCount());
            output.add(singleRowData);

            result.put("output", output);
            result.put("status", "0000");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "0001");
        }

        return result;
    }

    public int getTotalRowCount() {
        int totalRowCount = 0;
        try {
            totalRowCount = noticeDao.getTotalRowCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalRowCount;
    }

    public JSONObject reviseNotice(JSONObject jsonParam) {
        JSONObject result = new JSONObject();

        Map<String, Object> singleRowData = new HashMap<>();
        List<Map<String, Object>> output = new ArrayList<>();

        String parsedContent = commonFunc.pTagWrapper(jsonParam.get("noticeContents").toString());
        jsonParam.put("noticeContents", parsedContent);

        try {
            noticeDao.reviseNotice(jsonParam);
            singleRowData.put("result", "success");
            output.add(singleRowData);

            result.put("output", output);
            result.put("status", "0000");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "0001");
        }
        return result;
    }
}
