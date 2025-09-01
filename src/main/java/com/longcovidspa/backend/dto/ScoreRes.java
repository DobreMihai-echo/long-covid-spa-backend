package com.longcovidspa.backend.dto;

import java.util.List;
import java.util.Map;

public class ScoreRes {
    public String generatedAt;
    public Map<String,Integer> scores;
    public List<String> trends;
    public List<Map<String,Object>> explain;
    public Map<String,Object> snapshot;
    public Boolean coldStart;
    public String modelVersion;
}
