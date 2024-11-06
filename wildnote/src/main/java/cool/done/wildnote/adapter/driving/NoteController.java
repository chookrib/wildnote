package cool.done.wildnote.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.done.wildnote.adapter.driven.FileRepository;
import cool.done.wildnote.domain.NoteIndex;
import cool.done.wildnote.domain.NoteService;
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
    private FileRepository fileRepository;

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
        fileRepository.createFile(path);
        return Result.success();
    }

    /**
     * 删除笔记
     */
    @RequestMapping(value = "/api/note/delete", method = RequestMethod.POST)
    public Result deleteNote(@RequestBody String requestBody) throws IOException {
        JsonNode json = new ObjectMapper().readTree(requestBody);
        String path = json.path("path").asText();
        fileRepository.deleteFile(path);
        return Result.success();
    }
}
