package cool.done.wildnote.domain;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 文件IRepository
 */
public interface IFileRepository {

    /**
     * 读取目录
     */
    ArrayList<FilePath> getFilePaths(String path);

    /**
     * 读取文件
     */
    String getFile(String path) throws IOException;

    /**
     * 保存文件
     */
    void saveFile(String path, String content) throws IOException;

    /**
     * 创建文件
     */
    void createFile(String path) throws IOException;

    /**
     * 删除文件
     */
    void deleteFile(String path) throws IOException;
}
