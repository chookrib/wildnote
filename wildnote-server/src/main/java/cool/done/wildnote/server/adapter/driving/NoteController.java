package cool.done.wildnote.server.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import cool.done.wildnote.server.common.util.JacksonUtils;
import cool.done.wildnote.server.domain.NotePath;
import cool.done.wildnote.server.domain.NoteRemindCron;
import cool.done.wildnote.server.domain.NoteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 笔记Controller
 */
@RestController
public class NoteController {

    private final NoteService noteService;

    @Value("${wildnote.note-remind-log:}")
    private String noteRemindLog;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 取所有笔记
     */
    @RequestMapping(value = "/api/note/all", method = RequestMethod.GET)
    public Result getAllNote() {
        ArrayList<NotePath> result = new ArrayList<>(
                noteService.getNotePathMap().values().stream().sorted(
                        Comparator.comparing(NotePath::getAbsPath)
                ).toList());
        return Result.successData(result);
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
     * 取所有定时任务
     */
    @RequestMapping(value = "/api/note/cron", method = RequestMethod.GET)
    public Result getCron() {
        List<NoteRemindCron> result = noteService.getAllCron();
        return Result.successData(result);
    }

    /**
     * 取最新提醒日志
     */
    @RequestMapping(value = "/api/note/remind", method = RequestMethod.GET)
    public Result getRemind(@RequestParam(defaultValue = "1024") int size) {
        try (RandomAccessFile raf = new RandomAccessFile(noteRemindLog, "r")) {
            long fileLength = raf.length();
            long start = Math.max(0, fileLength - size);
            raf.seek(start);
            byte[] buffer = new byte[(int)Math.min(size, fileLength)];
            raf.readFully(buffer);

            String result = new String(buffer, StandardCharsets.UTF_8);
            if(start > 0) {
                //去除被载断的行
                result = result.substring(result.indexOf("\n") + 1);
            }
            //反转行顺序
            result = result.lines()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                java.util.Collections.reverse(list);
                                return String.join("\n", list);
                            }
                    ));
            return Result.successData(result);
        } catch (Exception e) {
            return Result.successData(e.getMessage());
        }
    }
}
