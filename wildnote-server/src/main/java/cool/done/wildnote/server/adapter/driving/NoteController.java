//package cool.done.wildnote.server.adapter.driving;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import cool.done.wildnote.server.domain.NoteRemindCronFailed;
//import cool.done.wildnote.server.utility.JacksonUtility;
//import cool.done.wildnote.server.domain.NotePath;
//import cool.done.wildnote.server.domain.NoteRemindCron;
//import cool.done.wildnote.server.domain.NoteService;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//
///**
// * 笔记 Controller
// */
//@RestController
//public class NoteController {
//
//    private final NoteService noteService;
//
//    public NoteController(NoteService noteService) {
//        this.noteService = noteService;
//    }
//
//    /**
//     * 取所有笔记
//     */
//    @RequestMapping(value = "/api/note/all", method = RequestMethod.GET)
//    public Result getAllNote() {
//        ArrayList<NotePath> result = new ArrayList<>(
//                noteService.getNotePathMap().values().stream().sorted(
//                        Comparator.comparing(NotePath::getAbsPath)
//                ).toList());
//        return Result.okData(result);
//    }
//
//    /**
//     * 读取笔记，根据POST参数
//     */
//    @RequestMapping(value = "/api/note/get", method = RequestMethod.POST)
//    public Result getNote(@RequestBody String requestBody) {
//        JsonNode json = JacksonUtility.readTree(requestBody);
//        String path = json.path("path").asText();
//        String note  = noteService.getNote(path);
//        return Result.okData(note);
//    }
//
//    /**
//     * 读取笔记，根据QueryString参数
//     */
//    @RequestMapping(value = "/api/note/get", method = RequestMethod.GET)
//    public Result getNoteByQueryString(@RequestParam String path) {
//        String note  = noteService.getNote(path);
//        return Result.okData(note);
//    }
//
//    /**
//     * 保存笔记
//     */
//    @RequestMapping(value = "/api/note/save", method = RequestMethod.POST)
//    public Result saveNote(@RequestBody String requestBody) {
//        JsonNode json = JacksonUtility.readTree(requestBody);
//        String path = json.path("path").asText();
//        String content = json.path("content").asText();
//        noteService.saveNote(path, content);
//        return Result.ok();
//    }
//
//    /**
//     * 创建笔记
//     */
//    @RequestMapping(value = "/api/note/create", method = RequestMethod.POST)
//    public Result createNote(@RequestBody String requestBody) {
//        JsonNode json = JacksonUtility.readTree(requestBody);
//        String path = json.path("path").asText();
//        noteService.createNote(path);
//        return Result.ok();
//    }
//
//    /**
//     * 删除笔记
//     */
//    @RequestMapping(value = "/api/note/delete", method = RequestMethod.POST)
//    public Result deleteNote(@RequestBody String requestBody) {
//        JsonNode json = JacksonUtility.readTree(requestBody);
//        String path = json.path("path").asText();
//        noteService.deleteNote(path);
//        return Result.ok();
//    }
//
//    /**
//     * 取所有定时任务
//     */
//    @RequestMapping(value = "/api/note/cron", method = RequestMethod.GET)
//    public Result getCron() {
//        List<NoteRemindCron> result = noteService.getAllCron();
//        return Result.okData(result);
//    }
//
//    /**
//     * 取所有失败的定时任务
//     */
//    @RequestMapping(value = "/api/note/cron/failed", method = RequestMethod.GET)
//    public Result getCronFailed() {
//        List<NoteRemindCronFailed> result = noteService.getAllCronFailed();
//        return Result.okData(result);
//    }
//}
