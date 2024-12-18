package cool.done.wildnotesvc.adapter.driven;

import cool.done.wildnotesvc.domain.IFileHandler;
import cool.done.wildnotesvc.domain.ValidationException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 文件Handler
 */
@Component
public class FileHandler implements IFileHandler {
    /**
     * 读取目录
     */
    @Override
    public ArrayList<File> getFiles(String path){
        ArrayList<File> files = new ArrayList<>();
        File[] list = new File(path).listFiles();
        if(list == null)
            return files;

        Arrays.sort(list, (file1, file2) -> {
            if(file1.isDirectory() && file2.isFile())
                return -1;
            if(file1.isFile() && file2.isDirectory())
                return 1;
            return file1.getName().compareTo(file2.getName());
        });

        for (File file : list) {
            files.add(file);
            if(file.isDirectory())
                files.addAll(getFiles(file.getPath()));
        }

        return files;
    }

    /**
     * 读取文件
     */
    @Override
    public String getFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存文件
     */
    @Override
    public void saveFile(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建文件
     */
    @Override
    public void createFile(String path) {
        if(Files.exists(Paths.get(path)))
            throw new ValidationException("文件已存在");
        try {
            Files.writeString(Paths.get(path), "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文件
     */
    @Override
    public void deleteFile(String path) {
        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
