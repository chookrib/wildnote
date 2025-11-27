package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteSettingService;
import cool.done.wildnote.server.application.NoteTreeNodeDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 笔记资源管理 Controller
 */
@RestController
public class NoteExploreController {

    private final NoteExploreService noteExploreService;
    private final NoteSettingService noteSettingService;

    public NoteExploreController(
            NoteExploreService noteExploreService,
            NoteSettingService noteSettingService
    ) {
        this.noteExploreService = noteExploreService;
        this.noteSettingService = noteSettingService;
    }

    /**
     * 重新加载所有笔记
     */
    @RequestMapping(value = "/api/note/reload", method = RequestMethod.GET)
    public Result noteReload() {
        noteExploreService.loadDirectory();
        return Result.ok();
    }

    /**
     * 取所有笔记
     */
    @RequestMapping(value = "/api/note/all", method = RequestMethod.GET)
    public Result noteAll() {
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
     * 读取笔记，根据POST参数
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.POST)
    public Result noteGetViaPost(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String content = noteExploreService.getFileContent(path);
        return Result.okData(Map.of("content", content));
    }

    /**
     * 读取笔记，根据QueryString参数
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.GET)
    public Result noteGetViaGet(HttpServletRequest request) {
        String path = RequestValueHelper.getRequestParamStringTrimReq(request, "path");
        String content = noteExploreService.getFileContent(path);
        return Result.okData(Map.of("content", content));
    }

    /**
     * 保存笔记
     */
    @RequestMapping(value = "/api/note/save", method = RequestMethod.POST)
    public Result noteSave(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String content = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "content");
        noteExploreService.saveFileContent(path, content);
        return Result.ok();
    }

    /**
     * 创建笔记
     */
    @RequestMapping(value = "/api/note/create", method = RequestMethod.POST)
    public Result noteCreate(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String notePath = noteExploreService.createFile(path);
        return Result.okData(Map.of("path", notePath));
    }

    /**
     * 删除笔记
     */
    @RequestMapping(value = "/api/note/delete", method = RequestMethod.POST)
    public Result noteDelete(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        noteExploreService.deleteFile(path);
        return Result.ok();
    }

    /**
     * 创建笔记文件夹
     */
    @RequestMapping(value = "/api/note/create-folder", method = RequestMethod.POST)
    public Result noteCreateFolder(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        String notePath = noteExploreService.createDirectory(path);
        return Result.okData(Map.of("path", notePath));
    }

    /**
     * 删除笔记
     */
    @RequestMapping(value = "/api/note/delete-folder", method = RequestMethod.POST)
    public Result noteDeleteFolder(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String path = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "path");
        noteExploreService.deleteDirectory(path);
        return Result.ok();
    }

    // =================================================================================================================

    /**
     * 取收藏路径
     */
    @RequestMapping(value = "/api/note/favorite/get", method = RequestMethod.GET)
    public Result noteFavoriteGet() {
        return Result.okData(Map.of("paths", noteSettingService.getFavorite()));
    }

    /**
     * 存收藏路径
     */
    @RequestMapping(value = "/api/note/favorite/set", method = RequestMethod.POST)
    public Result noteFavoriteSet(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        List<String> paths = RequestValueHelper.getRequestJsonStringTrimList(requestJson, "paths");
        // System.out.println("paths=" + paths);
        noteSettingService.setFavorite(paths);
        return Result.ok();
    }
}
