package cool.done.wildnotesvc.domain;

import java.io.File;
import java.util.ArrayList;

/**
 * 文件IHandler
 */
public interface IFileHandler {

    /**
     * 读取目录
     */
    ArrayList<File> getFiles(String path);

    /**
     * 读取文件
     */
    String getFile(String path);

    /**
     * 保存文件
     */
    void saveFile(String path, String content);

    /**
     * 创建文件
     */
    void createFile(String path);

    /**
     * 删除文件
     */
    void deleteFile(String path);
}
