package cool.done.wildnotesvc.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.done.wildnotesvc.adapter.driven.FileHandler;
import cool.done.wildnotesvc.domain.NoteIndex;
import cool.done.wildnotesvc.domain.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 笔记Controller
 */
@RestController
public class NoteController {

    @Autowired
    private NoteService noteService;
    @Autowired
    private FileHandler fileHandler;

    /**
     * 读取笔记列表
     */
    @RequestMapping(value = "/api/note/index", method = RequestMethod.GET)
    public Result getNoteIndex() throws IOException {
        ArrayList<NoteIndex> notes  = noteService.getNotes();
        return Result.successData(notes);
    }

    /**
     * 读取笔记
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.POST)
    public Result getNote(@RequestBody String requestBody) throws IOException {
        JsonNode json = new ObjectMapper().readTree(requestBody);
        String path = json.path("path").asText();
        String note  = noteService.getNote(path);
        return Result.successData(note);
    }

    /**
     * 读取笔记
     */
    @RequestMapping(value = "/api/note/get", method = RequestMethod.GET)
    public Result getNoteByQueryString(@RequestParam String path) throws IOException {
        String note  = noteService.getNote(path);
        return Result.successData(note);
    }

    /**
     * 保存笔记
     */
    @RequestMapping(value = "/api/note/save", method = RequestMethod.POST)
    public Result saveNote(@RequestBody String requestBody) throws IOException {
        JsonNode json = new ObjectMapper().readTree(requestBody);
        String path = json.path("path").asText();
        String content = json.path("content").asText();
        noteService.saveNote(path, content);
        return Result.success();
    }

    /**
     * 创建笔记
     */
    @RequestMapping(value = "/api/note/create", method = RequestMethod.POST)
    public Result createNote(@RequestBody String requestBody) throws IOException {
        JsonNode json = new ObjectMapper().readTree(requestBody);
        String path = json.path("path").asText();
        fileHandler.createFile(path);
        return Result.success();
    }

    /**
     * 删除笔记
     */
    @RequestMapping(value = "/api/note/delete", method = RequestMethod.POST)
    public Result deleteNote(@RequestBody String requestBody) throws IOException {
        JsonNode json = new ObjectMapper().readTree(requestBody);
        String path = json.path("path").asText();
        fileHandler.deleteFile(path);
        return Result.success();
    }
}
