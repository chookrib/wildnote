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
    @RequestMapping(value = "/api/explore/all-note", method = RequestMethod.GET)
    public Result exploreAllNote() {
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
     * 读取笔记文件，根据 POST 参数
     */
    @RequestMapping(value = "/api/explore/get-note-file", method = RequestMethod.POST)
    public Result exploreGetNoteFileByPost(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String content = noteExploreService.getFileContent(path);
        return Result.okData(Map.of("content", content));
    }

    /**
     * 读取笔记文件，根据 QueryString 参数
     */
    @RequestMapping(value = "/api/explore/get-note-file", method = RequestMethod.GET)
    public Result exploreGetNoteFileByGet(HttpServletRequest request) {
        String path = RequestValueHelper.getRequestParamStringTrimReq(request, "path");
        String content = noteExploreService.getFileContent(path);
        return Result.okData(Map.of("content", content));
    }

    /**
     * 保存笔记文件
     */
    @RequestMapping(value = "/api/explore/save-note-file", method = RequestMethod.POST)
    public Result exploreSaveNoteFile(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String content = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "content");
        noteExploreService.saveFileContent(path, content);
        return Result.ok();
    }

    /**
     * 创建笔记文件
     */
    @RequestMapping(value = "/api/explore/create-note-file", method = RequestMethod.POST)
    public Result exploreCreateNoteFile(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String notePath = noteExploreService.createFile(path);
        return Result.okData(Map.of("path", notePath));
    }

    /**
     * 删除笔记文件
     */
    @RequestMapping(value = "/api/explore/delete-note-file", method = RequestMethod.POST)
    public Result exploreDeleteNoteFile(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        noteExploreService.deleteFile(path);
        return Result.ok();
    }

    /**
     * 移动笔记文件
     */
    @RequestMapping(value = "/api/explore/move-note-file", method = RequestMethod.POST)
    public Result exploreMoveNoteFile(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String sourcePath = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "sourcePath");
        String targetPath = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "targetPath");
        noteExploreService.moveFile(sourcePath, targetPath);
        return Result.ok();
    }

    /**
     * 创建笔记文件夹
     */
    @RequestMapping(value = "/api/explore/create-note-folder", method = RequestMethod.POST)
    public Result exploreCreateNoteFolder(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String notePath = noteExploreService.createFolder(path);
        return Result.okData(Map.of("path", notePath));
    }

    /**
     * 删除笔记文件夹
     */
    @RequestMapping(value = "/api/explore/delete-note-folder", method = RequestMethod.POST)
    public Result exploreDeleteNoteFolder(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        noteExploreService.deleteFolder(path);
        return Result.ok();
    }

    /**
     * 移动笔记文件夹
     */
    @RequestMapping(value = "/api/explore/move-note-folder", method = RequestMethod.POST)
    public Result exploreMoveNoteFolder(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String sourcePath = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "sourcePath");
        String targetPath = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "targetPath");
        noteExploreService.moveFolder(sourcePath, targetPath);
        return Result.ok();
    }
}
