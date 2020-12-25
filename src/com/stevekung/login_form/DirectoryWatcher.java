package com.stevekung.login_form;

import java.io.File;
import java.util.*;

public abstract class DirectoryWatcher extends TimerTask
{
    private final String path;
    private final HashMap<File, Long> dir = new HashMap<>();
    private final DirectoryFilterWatcher watcher;
    private File[] filesArray;

    public DirectoryWatcher(String path)
    {
        this(path, "");
    }

    public DirectoryWatcher(String path, String filter)
    {
        this.path = path;
        this.watcher = new DirectoryFilterWatcher(filter);
        this.filesArray = new File(path).listFiles(this.watcher);

        // transfer to the hashmap be used a reference and keep the lastModfied value
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

        // scan the files and check for modification/addition
        for (File element : this.filesArray)
        {
            Long current = this.dir.get(element);
            checkedFiles.add(element);
            
            if (current == null)
            {
                // new file
                this.dir.put(element, new Long(element.lastModified()));
                this.onChange(element, Action.ADD);
            }
            else if (current.longValue() != element.lastModified())
            {
                // modified file
                this.dir.put(element, new Long(element.lastModified()));
                this.onChange(element, Action.MODIFY);
            }
        }

        // now check for deleted files
        @SuppressWarnings("unchecked")
        Set<File> ref = ((HashMap<File, Long>) this.dir.clone()).keySet();
        ref.removeAll(checkedFiles);
        Iterator<File> it = ref.iterator();
        
        while (it.hasNext())
        {
            File deletedFile = it.next();
            this.dir.remove(deletedFile);
            this.onChange(deletedFile, Action.DELETE);
        }
    }

    protected abstract void onChange(File file, Action action);
    
    public enum Action
    {
        ADD, MODIFY, DELETE;
    }
}