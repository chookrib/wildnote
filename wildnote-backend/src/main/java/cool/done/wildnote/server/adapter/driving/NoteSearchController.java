package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteTreeNodeDto;
import cool.done.wildnote.server.utility.ValueUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 笔记搜索管理 Controller
 */
@RestController
public class NoteSearchController {

    @Value("${app.ripgrep-path:}")
    private String ripgrepPath;

    private final NoteExploreService noteExploreService;

    public NoteSearchController(NoteExploreService noteExploreService) {
        this.noteExploreService = noteExploreService;
    }

    /**
     * 搜索笔记内容
     */
    @RequestMapping(value = "/api/search/path", method = RequestMethod.POST)
    public Result searchPath(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String keyword = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "keyword");
        // System.out.println(keyword);

        var notes = noteExploreService.getNoteMap().values().stream().filter(
                        note -> note.getRelPath().toLowerCase().contains(keyword.toLowerCase())
                ).map(NoteTreeNodeDto::fromEntity)
                .toList();

        return Result.okData(Map.of(
                "list",
                notes
        ));
    }

    /**
     * 搜索笔记内容
     */
    @RequestMapping(value = "/api/search/content", method = RequestMethod.POST)
    public Result searchContent(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String keyword = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "keyword");
        keyword = keyword
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
        ;
        // System.out.println(keyword);

        if (ValueUtility.isEmptyString(ripgrepPath)) {
            throw new ControllerException("没有指定 ripgrep 路径");
        }

        var nodeMap = noteExploreService.getNoteMap();
        var notes = new ArrayList<NoteTreeNodeDto>();
        // var result = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ripgrepPath,
                    "-F",   // 字面量搜索
                    "-l",   // 只列出文件
                    "-i",   // 忽略大小写
                    keyword,
                    noteExploreService.getNoteRootAbsPath());

            pb.redirectErrorStream(true); // 合并 stderr 到 stdout
            Process process = pb.start();
            process.getOutputStream().close();  // 避免子进程等待 stdin 导致阻塞，未转义 \ 时搜索 \" 会阻塞

            // System.out.println("执行命令: " + String.join(" ", pb.command()));

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()
                            , StandardCharsets.UTF_8    //, Charset.forName("GBK")
                    )
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (nodeMap.containsKey(line)) {
                        // results.add(line);
                        notes.add(NoteTreeNodeDto.fromEntity(nodeMap.get(line)));
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ControllerException("rg执行失败，exitCode=" + exitCode);
            }

            return Result.okData(Map.of(
                    "list",
                    notes
            ));
        } catch (Exception ex) {
            throw new ControllerException(ex);
        }
    }
}
