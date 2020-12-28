package com.stevekung.login_form;

import com.google.gson.Gson;

public class Constants
{
    public static final String API = "http://aritdoc.lpru.ac.th/api/api2/authentication"; // URL API ของ LPRU
    public static final String ALIVE = "http://aritdoc.lpru.ac.th/api/api2/alive"; // URL สำหรับเช็คว่า login อยู่หรือไม่
    public static final String FILE_SERVICE = "http://aritdoc.lpru.ac.th/api/syn/new"; // URL สำหรับ upload file

    public static final Gson GSON = new Gson();
}