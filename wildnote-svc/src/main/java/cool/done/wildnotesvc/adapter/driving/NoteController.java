package cool.done.wildnotesvc.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnotesvc.common.util.JacksonUtils;
import cool.done.wildnotesvc.domain.NoteIndexNode;
import cool.done.wildnotesvc.domain.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 笔记Controller
 */
@RestController
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 读取笔记列表
     */
    @RequestMapping(value = "/api/note/index", method = RequestMethod.GET)
    public Result getNoteIndex() {
        ArrayList<NoteIndexNode> notes = new ArrayList<>(
                noteService.getNoteMap().values().stream().sorted(
                        Comparator.comparing(NoteIndexNode::getAbsPath)
                ).toList());
        return Result.successData(notes);
    }

    /**
     * 读取笔记，根据POST参数
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.POST)
    public Result getNote(@RequestBody String requestBody) {
        JsonNode json = JacksonUtils.readTree(requestBody);
        String path = json.path("path").asText();
        String note  = noteService.getNote(path);
        return Result.successData(note);
    }

    /**
     * 读取笔记，根据QueryString参数
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.GET)
    public Result getNoteByQueryString(@RequestParam String path) {
        String note  = noteService.getNote(path);
        return Result.successData(note);
    }

    /**
     * 保存笔记
     */
    @RequestMapping(value = "/api/note/save", method = RequestMethod.POST)
    public Result saveNote(@RequestBody String requestBody) {
        JsonNode json = JacksonUtils.readTree(requestBody);
        String path = json.path("path").asText();
        String content = json.path("content").asText();
        noteService.saveNote(path, content);
        return Result.success();
    }

    /**
     * 创建笔记
     */
    @RequestMapping(value = "/api/note/create", method = RequestMethod.POST)
    public Result createNote(@RequestBody String requestBody) {
        JsonNode json = JacksonUtils.readTree(requestBody);
        String path = json.path("path").asText();
        noteService.createNote(path);
        return Result.success();
    }

    /**
     * 删除笔记
     */
    @RequestMapping(value = "/api/note/delete", method = RequestMethod.POST)
    public Result deleteNote(@RequestBody String requestBody) {
        JsonNode json = JacksonUtils.readTree(requestBody);
        String path = json.path("path").asText();
        noteService.deleteNote(path);
        return Result.success();
    }

    /**
     * 获取所有定时任务
     */
    @RequestMapping(value = "/api/note/cron", method = RequestMethod.GET)
    public Result getCorn() {
        List<String> crons = noteService.getAllCron();
        return Result.successData(crons);
    }
}
