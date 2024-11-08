package cool.done.wildnotesvc.adapter.driven;

import cool.done.wildnotesvc.domain.IFileRepository;
import cool.done.wildnotesvc.domain.ValidationException;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文件Repository
 */
@Repository
public class FileRepository implements IFileRepository {
    /**
     * 读取目录
     */
    @Override
    public ArrayList<File> getFiles(String path){
        ArrayList<File> files = new ArrayList<>();
        File[] list = new File(path).listFiles();
        if(list == null)
            return files;

        Arrays.sort(list, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                if(file1.isDirectory() && file2.isFile())
                    return -1;
                if(file1.isFile() && file2.isDirectory())
                    return 1;
                return file1.getName().compareTo(file2.getName());
            }
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
    public String getFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    /**
     * 保存文件
     */
    @Override
    public void saveFile(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }

    /**
     * 创建文件
     */
    @Override
    public void createFile(String path) throws IOException {
        if(Files.exists(Paths.get(path)))
            throw new ValidationException("文件已存在");

        Files.writeString(Paths.get(path), "");
    }

    /**
     * 删除文件
     */
    @Override
    public void deleteFile(String path) throws IOException {
        Files.delete(Paths.get(path));
    }
}
