package com.stevekung.login_form;

/**
 * ถ้ามีข้อมูล json เพิ่มให้ใส่ field ที่นี่
 * ชื่อต้องตรงกับ json ที่ส่งมา
 */
public class LoginData
{
    private final boolean status;

    public LoginData(boolean status)
    {
        this.status = status;
    }

    public boolean isLoggedIn()
    {
        return this.status;
    }
}