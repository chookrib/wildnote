package cool.done.wildnote.server.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.domain.NoteTreeNode;
import cool.done.wildnote.server.utility.JacksonUtility;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;

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
     * 取所有笔记
     */
    @RequestMapping(value = "/api/note/all", method = RequestMethod.GET)
    public Result noteAll() {
        ArrayList<NoteTreeNode> result = new ArrayList<>(
                noteExploreService.getNoteMap().values().stream().sorted(
                        Comparator.comparing(NoteTreeNode::getAbsPath)
                ).toList());
        return Result.okData(result);
    }

    /**
     * 读取笔记，根据POST参数
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.POST)
    public Result noteGetViaPost(@RequestBody String requestBody) {
        JsonNode json = JacksonUtility.readTree(requestBody);
        String path = json.path("path").asText();
        String content = noteExploreService.getNote(path);
        return Result.okData(content);
    }

    /**
     * 读取笔记，根据QueryString参数
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.GET)
    public Result noteGetViaGet(@RequestParam String path) {
        String content = noteExploreService.getNote(path);
        return Result.okData(content);
    }

    /**
     * 保存笔记
     */
    @RequestMapping(value = "/api/note/save", method = RequestMethod.POST)
    public Result noteSave(@RequestBody String requestBody) {
        JsonNode json = JacksonUtility.readTree(requestBody);
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
        JsonNode json = JacksonUtility.readTree(requestBody);
        String path = json.path("path").asText();
        noteExploreService.createNote(path);
        return Result.ok();
    }

    /**
     * 删除笔记
     */
    @RequestMapping(value = "/api/note/delete", method = RequestMethod.POST)
    public Result noteDelete(@RequestBody String requestBody) {
        JsonNode json = JacksonUtility.readTree(requestBody);
        String path = json.path("path").asText();
        noteExploreService.deleteNote(path);
        return Result.ok();
    }
}
