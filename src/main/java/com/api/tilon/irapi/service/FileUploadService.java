package com.api.tilon.irapi.service;


import com.api.tilon.irapi.common.CommonFunc;
import com.api.tilon.irapi.controller.NoticeController;
import com.api.tilon.irapi.dao.NoticeDao;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileUploadService {
    @Autowired
    NoticeDao noticeDao;

    @Autowired
    CommonFunc commonFunc;

    @Value("${spring.servlet.multipart.location}")
    private String upperUploadPath;

    @Value("${database.file.upload.path}")
    private String filepathForDB;
    private static final Logger logger
            = LoggerFactory.getLogger(NoticeController.class);

    public JSONObject handleFileUpload(JSONObject jsonObj, MultipartFile uploadfile) throws IOException {
        JSONObject result = new JSONObject();
        Map<String, Object> singleRowData = new HashMap<>();
        List<Map<String, Object>> output = new ArrayList<>();

        String dirCategoryName = jsonObj.get("dirCategory").toString();

        System.out.println("Multipart Content Type : " + uploadfile.getContentType());

        //폴더가 이미 만들어져있으면 그 폴더에 저장, 없으면 폴더 만들기
        //makeFolder("차하위폴더명");
        String dirPath = makeFolder(dirCategoryName);

        /////////////////////check for filename validity, and return new filename or revoke request //////////////////////////
        String fileName = returnValidFileName(uploadfile, dirPath);

        //////////////////////filename and path db update /////////////////////////////
        if (dirCategoryName.equals("notice") || dirCategoryName.equals("info")) {
            singleRowData = updateFileNameAndPathInDB(jsonObj, fileName);
        }
        //////////////////////FileStorage Upload  ////////////////////////////////////
        boolean uploadSuccess = transferFileToStorage(uploadfile, dirPath, fileName);
        if (uploadSuccess) {
            singleRowData.put("fileUploadResult", "success");
            result.put("status", "0000");
        } else {
            singleRowData.put("fileUploadResult", "fail");
            result.put("status", "0001");
        }
        output.add(singleRowData);
        result.put("output", output);
        return result;
    }

    public String makeFolder(String dirName) {
        String dirPath = "";
        String todayDate = commonFunc.getDateToday(1);
        dirPath = upperUploadPath + "/" + dirName + "/" + todayDate;

        File uploadPathFolder = new File(dirPath);
        //File newFile= new File(dir폴더명);->부모 디렉토리를 파라미터로 인스턴스 생성
        if (uploadPathFolder.exists() == false) {
            uploadPathFolder.mkdirs();
            //만약 uploadPathFolder가 존재하지않는다면 makeDirectory하라
            //mkdir(): 디렉토리에 상위 디렉토리가 존재하지 않을경우에는 생성이 불가능한 함수
            //mkdirs(): 디렉토리의 상위 디렉토리가 존재하지 않을 경우에는 상위 디렉토리까지 모두 생성하는 함수
        }
        return dirPath;
    }

    public String returnValidFileName(MultipartFile uploadfile, String dirPath) {
        String originalName = uploadfile.getOriginalFilename();
        String fileName = originalName.substring(originalName.lastIndexOf("//") + 1);
        String fileNameWithoutExtensionName = fileName.substring(0, fileName.lastIndexOf("."));
        String extensionName = fileName.substring(fileName.lastIndexOf("."));

        File serverFile = new File(dirPath + "/" + originalName);
        System.out.println("serverFile:" + serverFile);

        String makeFolderResultDir = String.valueOf(dirPath).substring(String.valueOf(dirPath).lastIndexOf("\\") + 1);

        System.out.println("dirPath:" + dirPath);

        try {
            for (int i = 1; serverFile.exists(); i++) {
                System.out.println("filename already exists, appending suffix...");
                serverFile = new File(dirPath + "/" + fileNameWithoutExtensionName + "(" + i + ")" + extensionName);
                //filename for Windows
                fileName = String.valueOf(serverFile).substring(String.valueOf(serverFile).lastIndexOf("\\") + 1);
                System.out.println("fileName " + i + ":" + fileName);
                if (i > 50) break;
            }
        } catch (Exception e) {
            fileName = originalName.substring(originalName.lastIndexOf("//") + 1);
        }
        return fileName;
    }

    public JSONObject updateFileNameAndPathInDB(JSONObject jsonObj, String fileName) {
        JSONObject result = new JSONObject();

        String dirCategoryName = jsonObj.get("dirCategory").toString();
        int seq = Integer.parseInt(jsonObj.get("seq").toString());

        //should put "/down/upload/files" instead of "\\\\FileStorage\\Storage\\Upload\\files" when updating DB
        String dbUploadPath = filepathForDB + "/" + dirCategoryName + "/" + commonFunc.getDateToday(1);

        JSONObject updateParam = new JSONObject();
        updateParam.put("seq", seq);
        updateParam.put("filepath", dbUploadPath);
        updateParam.put("fileUpload", fileName);

        try {
            if (dirCategoryName.equals("notice")) { //공지사항인 경우
                noticeDao.updateFilePath(updateParam);
            }
//            if (dirCategoryName.equals("faq")) { //FAQ인 경우
//                faqDao.updateFilePath(updateParam);
//            }
            result.put("DBFilePathUpdate", "success");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("DBFilePathUpdate", "fail");
        }

        return result;
    }


    public boolean transferFileToStorage(MultipartFile uploadfile, String dirPath, String validatedFileName) {
        //File.separator
        //만약 DataDir 밑에 exam.jpg라는 파일을 원한다고 할때,
        //윈도우는 "DataDir\\"exam.jpg", 리눅스는 "DataDir/exam.jpg"
        //형식을 자바에서 표현: "DataDir" +File.separator + "exam.jpg"
        String saveName = dirPath + File.separator + validatedFileName;
        //Paths.get() 메서드는 특정 경로의 파일 정보를 가져옴
        Path savePath = Paths.get(saveName);
        System.out.println(savePath);

        try {
            //uploadFile에 파일을 업로드 하는 메서드 transferTo(file)
            uploadfile.transferTo(savePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public JSONObject reviseFileUpload(JSONObject jsonObj) {
        JSONObject result = new JSONObject();
        Map<String, Object> singleRowData = new HashMap<>();
        List<Map<String, Object>> output = new ArrayList<>();

        String dirCategoryName = jsonObj.get("dirCategory").toString();
        int seq = Integer.parseInt(jsonObj.get("seq").toString());


        JSONObject updateParam = new JSONObject();
        updateParam.put("seq", seq);
        updateParam.put("fileUpload", "");

        try {
            if (dirCategoryName.equals("notice")) {
                noticeDao.reviseFileUpload(updateParam);
            }
//            if (dirCategoryName.equals("faq")) {
//                faqDao.reviseFileUpload(updateParam);
//            }

            singleRowData.put("DBFilePathUpdate", "success");
            result.put("status", "0000");
        } catch (Exception e) {
            e.printStackTrace();
            singleRowData.put("DBFilePathUpdate", "fail");
            result.put("status", "0001");
        }

        output.add(singleRowData);
        result.put("output", output);
        return result;
    }
}
