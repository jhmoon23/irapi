package com.api.tilon.irapi.dao;

import org.apache.ibatis.annotations.Mapper;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

@Mapper
public interface NoticeDao {
    public List<Map<String, Object>> getNoticeList(JSONObject param);

    public void putUpNotice(JSONObject param);

    public int selectSeqById(String userid);

    public void updateFilePath(JSONObject param);

    public void reviseFileUpload(JSONObject param);

    public List<Map<String, Object>> getSingleNotice(JSONObject param);

    public void putNoticeDeleteFlag(JSONObject param);

    public void reviseNotice(JSONObject param);

    public int getTotalRowCount();
}
