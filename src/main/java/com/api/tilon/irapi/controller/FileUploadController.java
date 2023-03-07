package com.api.tilon.irapi.controller;

import com.api.tilon.irapi.service.FileUploadService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@CrossOrigin
@Controller
@RequestMapping(value = "/upload", method = RequestMethod.POST)
public class FileUploadController {
    @Autowired
    FileUploadService fileUploadService;

    @RequestMapping("/files")
    @ResponseBody
    public String handleFileUpload(HttpServletRequest req,
                                   @RequestParam("dirCategory") String dirCategory,
                                   @RequestParam(value = "seq", required = false, defaultValue = "") String seq,
                                   @RequestParam("file") MultipartFile file) throws IOException {


        JSONObject json = new JSONObject();
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("dirCategory",dirCategory);
        jsonParam.put("seq",seq);

        if (file == null || file.isEmpty()) {
            //null로 오면 해당 파일에 대한 update to null을 실행해야함
            json =  fileUploadService.reviseFileUpload(jsonParam);
        } else {
            json = fileUploadService.handleFileUpload(jsonParam, file);
        }
        return json.toString();
    }
}
