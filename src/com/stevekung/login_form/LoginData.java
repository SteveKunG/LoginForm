package com.stevekung.login_form;

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