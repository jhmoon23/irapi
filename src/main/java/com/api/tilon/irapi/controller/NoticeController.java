package com.api.tilon.irapi.controller;

import com.api.tilon.irapi.service.FileUploadService;
import com.api.tilon.irapi.service.NoticeService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
@CrossOrigin
@Controller
@RequestMapping(value = "/notice", method = RequestMethod.POST)
public class NoticeController {

    @Autowired
    NoticeService noticeService;

    @Autowired
    FileUploadService fileUploadService;


    @RequestMapping("/list")
    @ResponseBody
    public String getNoticeList(HttpServletRequest req,
                                @RequestParam("searchTitle") String searchTitle,
                                @RequestParam("countsPerPage") String countsPerPage,
                                @RequestParam("pageNumber") String pageNumber
    ) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("searchTitle", searchTitle);
        jsonParam.put("countsPerPage", countsPerPage);
        jsonParam.put("pageNumber", pageNumber);

        JSONObject returnVal = noticeService.getNoticeList(jsonParam);
        return returnVal.toString();
    }

    @RequestMapping("/content")
    @ResponseBody
    public String getSingleNotice(HttpServletRequest req,
                                  @RequestParam("seq") String seq
    ) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("seq", seq);

        JSONObject returnVal = noticeService.getSingleNotice(jsonParam);
        return returnVal.toString();
    }


    @RequestMapping("/paging")
    @ResponseBody
    public String getNumberOfPages(HttpServletRequest req,
                                   @RequestParam("searchTitle") String searchTitle,
                                   @RequestParam("countsPerPage") String countsPerPage,
                                   @RequestParam("pageNumber") String pageNumber
    ) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("searchTitle", searchTitle);
        jsonParam.put("countsPerPage", countsPerPage);
        jsonParam.put("pageNumber", pageNumber);

        JSONObject json = noticeService.getNumberOfPages(jsonParam);
        return json.toString();
    }

    @RequestMapping("/new")
    @ResponseBody
    public String putUpNotice(HttpServletRequest req,
                              @RequestParam("noticeSubject") String noticeSubject,
                              @RequestParam("noticeContents") String noticeContents
    ) {
        JSONObject jsonParam = new JSONObject();
        JSONObject json = new JSONObject();

        noticeSubject = noticeSubject.trim();
        jsonParam.put("noticeSubject", noticeSubject);
        jsonParam.put("noticeContents", noticeContents);
        jsonParam.put("userid", req.getHeader("userid"));
        json = noticeService.putUpNotice(jsonParam);
        return json.toString();
    }

    @RequestMapping("/revision")
    @ResponseBody
    public String reviseNotice(HttpServletRequest req,
                               @RequestParam("seq") String seq,
                               @RequestParam("noticeSubject") String noticeSubject,
                               @RequestParam("noticeContents") String noticeContents
    ) {
        JSONObject jsonParam = new JSONObject();
        JSONObject json = new JSONObject();
        jsonParam.put("seq", seq);
        jsonParam.put("noticeSubject", noticeSubject);
        jsonParam.put("noticeContents", noticeContents);
        jsonParam.put("userid", req.getHeader("userid"));

        //파일에 대한 update, delete 기능이 수정에 들어감;
        //새로 올라가는 파일에 대해선 fileUploadController에서 수행한다.
        //여기서는 파일을 제외한 공지내용에 대한 update만 수행

        json = noticeService.reviseNotice(jsonParam);
        return json.toString();
    }

}