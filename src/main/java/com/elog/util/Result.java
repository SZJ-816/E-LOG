package com.elog.util;

import java.util.HashMap;

/**
 * 统一响应结果
 */
public class Result extends HashMap<String, Object> {
    
    public static Result success() {
        Result r = new Result();
        r.put("success", true);
        r.put("message", "操作成功");
        return r;
    }
    
    public static Result success(String message) {
        Result r = new Result();
        r.put("success", true);
        r.put("message", message);
        return r;
    }
    
    public static Result success(Object data) {
        Result r = new Result();
        r.put("success", true);
        r.put("data", data);
        return r;
    }
    
    public static Result error(String message) {
        Result r = new Result();
        r.put("success", false);
        r.put("message", message);
        return r;
    }
    
    public Result data(Object value) {
        this.put("data", value);
        return this;
    }
    
    public Result message(String message) {
        this.put("message", message);
        return this;
    }
}