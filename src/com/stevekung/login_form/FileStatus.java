package com.stevekung.login_form;

public class FileStatus
{
    private final boolean status;
    private final String url;

    public FileStatus(boolean status, String url)
    {
        this.status = status;
        this.url = url;
    }

    public boolean isSuccess()
    {
        return this.status;
    }

    public String getURL()
    {
        return this.url;
    }
}