package cool.done.wildnote.server.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteTreeNodeDto;
import cool.done.wildnote.server.application.SettingService;
import cool.done.wildnote.server.utility.JsonUtility;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 笔记资源管理 Controller
 */
@RestController
public class NoteExploreController {

    private final NoteExploreService noteExploreService;
    private final SettingService settingService;

    public NoteExploreController(NoteExploreService noteExploreService, SettingService settingService) {
        this.noteExploreService = noteExploreService;
        this.settingService = settingService;
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
        JsonNode json = JsonUtility.readTree(requestBody);
        String path = json.path("path").asText();
        String content = noteExploreService.getNote(path);
        return Result.okData(Map.of("content", content));
    }

    /**
     * 读取笔记，根据QueryString参数
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.GET)
    public Result noteGetViaGet(@RequestParam String path) {
        String content = noteExploreService.getNote(path);
        return Result.okData(Map.of("content", content));
    }

    /**
     * 保存笔记
     */
    @RequestMapping(value = "/api/note/save", method = RequestMethod.POST)
    public Result noteSave(@RequestBody String requestBody) {
        JsonNode json = JsonUtility.readTree(requestBody);
        String path = json.path("path").asText();
        String content = json.path("content").asText();
        noteExploreService.saveNote(path, content);
        return Result.ok();
    }

    /**
     * 创建笔记
     */
    @RequestMapping(value = "/api/note/create", method = RequestMethod.POST)
    public Result noteCreate(@RequestBody String requestBody) {
        JsonNode json = JsonUtility.readTree(requestBody);
        String path = json.path("path").asText();
        noteExploreService.createNote(path);
        return Result.ok();
    }

    /**
     * 删除笔记
     */
    @RequestMapping(value = "/api/note/delete", method = RequestMethod.POST)
    public Result noteDelete(@RequestBody String requestBody) {
        JsonNode json = JsonUtility.readTree(requestBody);
        String path = json.path("path").asText();
        noteExploreService.deleteNote(path);
        return Result.ok();
    }

    // =================================================================================================================

    /**
     * 取收藏路径
     */
    @RequestMapping(value = "/api/note/favorite/get", method = RequestMethod.GET)
    public Result noteFavoriteGet() {
        return Result.okData(Map.of("paths", settingService.getFavorite()));
    }

    /**
     * 存收藏路径
     */
    @RequestMapping(value = "/api/note/favorite/set", method = RequestMethod.POST)
    public Result noteFavorite(@RequestBody String requestBody) {
        JsonNode json = JsonUtility.readTree(requestBody);
        JsonNode pathsNode = json.path("paths");
        List<String> paths = new ArrayList<>();
        if (pathsNode.isArray()) {
            for (JsonNode node : pathsNode) {
                paths.add(node.asText());
            }
        }
        // System.out.println("paths=" + paths);
        settingService.setFavorite(paths);
        return Result.ok();
    }
}
