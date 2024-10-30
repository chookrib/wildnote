package cool.done.wildnote.adapter.driven;

import cool.done.wildnote.domain.IFileRepository;
import cool.done.wildnote.domain.FilePath;
import cool.done.wildnote.domain.ValidationException;
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
    public ArrayList<FilePath> getFilePaths(String path){
        ArrayList<FilePath> filePaths = new ArrayList<>();
        File[] files = new File(path).listFiles();

        if (files != null) {
            List fileList = Arrays.asList(files);
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && !o2.isFile()) {
                        return -1;
                    }
                    if (!o1.isFile() && o2.isDirectory()) {
                        return 1;
                    }
                    return o1.getName().compareTo(o2.getName());
                }});

            for (File file : files) {
                if (file.isFile()) {
                    filePaths.add(new FilePath(file.getPath(), file.getName(), false,
                            new ArrayList<FilePath>()));
                }
                if (file.isDirectory()) {
                    filePaths.add(new FilePath(file.getPath(), file.getName(), true,
                            getFilePaths(file.getPath())));
                }
            }
        }
        return filePaths;
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
