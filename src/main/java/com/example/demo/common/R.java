package com.example.demo.common;

import java.util.HashMap;

import jakarta.annotation.Nullable;

// type ResponseData
public class R extends HashMap<String, Object> {

    private static final String CODE = "code";
    private final static String MESSAGE = "message";
    private final static String VLAUE = "value";
    private final static String SHOW = "show";

    public static final String SHOW_DEFAULT = "0"; // don't show anything
    public static final String SHOW_SUCCESS = "1";
    public static final String SHOW_ERROR = "2";
    public static final String SHOW_WARNING = "3";

    public static final String SUCCESS_CODE = "200";
    public static final String ERROR_CODE = "500";

    public static final String ACCESS_DENY_CODE = "403";
    public final static String SUCCESS_MSG = "success";
    public final static String ERROR_MSG = "error";

    private R(boolean success) {
        if (success) {
            put(CODE, SUCCESS_CODE);
            put(MESSAGE, SUCCESS_MSG);
        } else {
            put(CODE, ERROR_CODE);
            put(MESSAGE, ERROR_MSG);
        }
        put(SHOW, SHOW_DEFAULT);
    }

    public static R ok() {
        return new R(true);
    }

    public static R okShow(String showType, @Nullable String message) {
        R r = new R(true);
        if (message != null) {
            r.put(MESSAGE, message);
        }
        r.put(SHOW, showType);
        return r;
    }

    public static R ok(Object value) {
        R r = new R(true);
        r.put(VLAUE, value);
        return r;
    }

    public static R okShow(Object value, String showType, String message) {
        R r = new R(true);
        r.put(VLAUE, value);
        r.put(SHOW, showType);
        if(message != null) {
            r.put(MESSAGE, message);
        }
        return r;
    }
    public static R okShow( String message) {
        R r = new R(true);
        r.put(SHOW, SHOW_SUCCESS);
        if(message != null) {
            r.put(MESSAGE, message);
        }
        return r;
    }

    public static R ok(String msg, Object value) {
        R r = new R(true);
        r.put(VLAUE, value);
        r.put(MESSAGE, msg);
        return r;
    }

    public static R okShow(String code, Object value, String showType, String message) {
        R r = new R(true);
        r.put(VLAUE, value);
        r.put(CODE, code);
        r.put(SHOW, showType);
        if(message != null) {
            r.put(MESSAGE, message);
        }
        return r;
    }

    public static R errorShow(String code, Object value, String showType, String message) {
        R r = new R(true);
        r.put(VLAUE, value);
        r.put(CODE, code);
        r.put(SHOW, showType);
        if(message != null) {
            r.put(MESSAGE, message);
        }
        return r;
    }

    public static R error() {
        return new R(false);
    }

    public static R errorShow(String message) {
        R r = new R(false);
        r.put(SHOW, SHOW_ERROR);
        if(message != null) {
            r.put(MESSAGE, message);
        }
        return r;
    }
}
