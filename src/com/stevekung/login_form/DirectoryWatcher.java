package com.stevekung.login_form;

import java.io.File;
import java.util.*;

public abstract class DirectoryWatcher extends TimerTask
{
    private final String path;
    private final HashMap<File, Long> dir = new HashMap<>();
    private final DirectoryFilterWatcher watcher;
    private File[] filesArray;

    public DirectoryWatcher(String path, String filter)
    {
        this.path = path;
        this.watcher = new DirectoryFilterWatcher(filter);
        this.filesArray = new File(path).listFiles(this.watcher);

        for (File element : this.filesArray)
        {
            this.dir.put(element, new Long(element.lastModified()));
        }
    }

    @Override
    public void run()
    {
        HashSet<File> checkedFiles = new HashSet<>();
        this.filesArray = new File(this.path).listFiles(this.watcher);

        // สแกนไฟล์และเช็คสถานะ
        for (File element : this.filesArray)
        {
            Long current = this.dir.get(element);
            checkedFiles.add(element);

            if (current == null)
            {
                // เช็คไฟล์ที่ถูกเพิ่ม
                this.dir.put(element, new Long(element.lastModified()));
                this.onFileAdd(element);
            }
            else if (current.longValue() != element.lastModified())
            {
                // เช็คไฟล์ที่ถูกแก้
                this.dir.put(element, new Long(element.lastModified()));
            }
        }

        // เช็คไฟล์ที่ถูกลบ
        @SuppressWarnings("unchecked")
        Set<File> ref = ((HashMap<File, Long>) this.dir.clone()).keySet();
        ref.removeAll(checkedFiles);
        Iterator<File> it = ref.iterator();

        while (it.hasNext())
        {
            File deletedFile = it.next();
            this.dir.remove(deletedFile);
        }
    }

    protected abstract void onFileAdd(File file);
}