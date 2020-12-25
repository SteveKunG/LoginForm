package com.stevekung.login_form;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilterWatcher implements FileFilter
{
    private final String filter;

    public DirectoryFilterWatcher()
    {
        this.filter = "";
    }

    public DirectoryFilterWatcher(String filter)
    {
        this.filter = filter;
    }

    @Override
    public boolean accept(File file)
    {
        if ("".equals(this.filter))
        {
            return true;
        }
        return file.getName().endsWith(this.filter);
    }
}