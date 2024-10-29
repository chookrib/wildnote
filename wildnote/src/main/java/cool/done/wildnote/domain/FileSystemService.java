package cool.done.wildnote.domain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * 文件服务
 */
public class FileSystemService {

    /**
     * 读取目录
     */
    public ArrayList<String> getFiles(String path){
        ArrayList<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();
        for(int i = 0; i < tempList.length; i++){
            if(tempList[i].isFile()){
                files.add(tempList[i].toString());
            }
            if(tempList[i].isDirectory()){
                getFiles(tempList[i].toString());
            }
        }
        return files;
    }

    /**
     * 读取文件
     */
    public String getFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }
}
