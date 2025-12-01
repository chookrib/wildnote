package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteTreeNodeDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 笔记资源管理 Controller
 */
@RestController
public class NoteExploreController {

    private final NoteExploreService noteExploreService;

    public NoteExploreController(NoteExploreService noteExploreService) {
        this.noteExploreService = noteExploreService;
    }

    /**
     * 重新加载所有笔记
     */
    @RequestMapping(value = "/api/explore/reload", method = RequestMethod.GET)
    public Result exploreReload() {
        noteExploreService.loadRoot();
        return Result.ok();
    }

    /**
     * 取所有笔记
     */
    @RequestMapping(value = "/api/explore/all", method = RequestMethod.GET)
    public Result exploreAll() {
        //ArrayList<NoteTreeNode> result = new ArrayList<>(
        //        noteExploreService.getNoteMap().values().stream().sorted(
        //                Comparator.comparing(NoteTreeNode::getAbsPath)
        //        ).toList());
        //return Result.okData(Map.of("list", result));
        return Result.okData(Map.of(
                "list",
                noteExploreService.getNoteMap().values().stream().map(NoteTreeNodeDto::fromEntity).toList()
        ));
    }

    /**
     * 读取笔记文件内容，根据 POST 参数
     */
    @RequestMapping(value = "/api/explore/get-content", method = RequestMethod.POST)
    public Result exploreGetContent(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String content = noteExploreService.getFileContent(path);
        return Result.okData(Map.of("content", content));
    }

    ///**
    // * 读取笔记文件内容，根据 QueryString 参数
    // */
    //@RequestMapping(value = "/api/explore/get-note-content", method = RequestMethod.GET)
    //public Result exploreGetContentByGet(HttpServletRequest request) {
    //    String path = RequestValueHelper.getRequestParamStringTrimReq(request, "path");
    //    String content = noteExploreService.getFileContent(path);
    //    return Result.okData(Map.of("content", content));
    //}

    /**
     * 保存笔记文件内容
     */
    @RequestMapping(value = "/api/explore/save-content", method = RequestMethod.POST)
    public Result exploreSaveContent(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String content = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "content");
        noteExploreService.saveFileContent(path, content);
        return Result.ok();
    }

    //==================================================================================================================

    /**
     * 创建笔记文件
     */
    @RequestMapping(value = "/api/explore/create-file", method = RequestMethod.POST)
    public Result exploreCreateFile(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        noteExploreService.createFile(path);
        return Result.ok();
    }

    /**
     * 删除笔记文件
     */
    @RequestMapping(value = "/api/explore/delete-file", method = RequestMethod.POST)
    public Result exploreDeleteFile(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        noteExploreService.deleteFile(path);
        return Result.ok();
    }

    /**
     * 移动笔记文件
     */
    @RequestMapping(value = "/api/explore/move-file", method = RequestMethod.POST)
    public Result exploreMoveFile(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String sourcePath = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "sourcePath");
        String targetPath = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "targetPath");
        if(sourcePath == targetPath) {
            noteExploreService.moveFile(sourcePath, targetPath);
        }
        return Result.ok();
    }

    /**
     * 创建笔记文件夹
     */
    @RequestMapping(value = "/api/explore/create-directory", method = RequestMethod.POST)
    public Result exploreCreateDirectory(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        noteExploreService.createDirectory(path);
        return Result.ok();
    }

    /**
     * 删除笔记文件夹
     */
    @RequestMapping(value = "/api/explore/delete-directory", method = RequestMethod.POST)
    public Result exploreDeleteDirectory(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        noteExploreService.deleteDirectory(path);
        return Result.ok();
    }

    /**
     * 移动笔记文件夹
     */
    @RequestMapping(value = "/api/explore/move-directory", method = RequestMethod.POST)
    public Result exploreMoveDirectory(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String sourcePath = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "sourcePath");
        String targetPath = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "targetPath");
        if(sourcePath == targetPath) {
            noteExploreService.moveDirectory(sourcePath, targetPath);
        }
        return Result.ok();
    }
}
