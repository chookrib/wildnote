package cool.done.wildnote.server.adapter.driving;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import cool.done.wildnote.server.application.NoteExploreService;
import cool.done.wildnote.server.application.NoteTreeNodeDto;
import cool.done.wildnote.server.utility.JsonUtility;
import cool.done.wildnote.server.utility.ValueUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 笔记搜索管理 Controller
 */
@RestController
public class NoteSearchController {

    private static final Cache<String, String> downloadTokenCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Value("${app.ripgrep-exe-path:}")
    private String ripgrepExePath;

    @Value("${app.everything-http-port:}")
    private String everythingHttpPort;

    // @Value("${app.everything-es-exe-path:}")
    // private String everythingEsExePath;

    // @Value("${app.everything-es-opt-path:}")
    // private String everythingEsOptPath;

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
                notes.stream().sorted(
                        java.util.Comparator.comparing(
                                item -> item.getPath() == null ? "" : item.getPath(),
                                String.CASE_INSENSITIVE_ORDER
                        )
                ).toList()
        ));
    }

    /**
     * 搜索笔记内容
     */
    @RequestMapping(value = "/api/search/content", method = RequestMethod.POST)
    public Result searchContent(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String keyword = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "keyword");
        keyword = keyword.replace("\"", "\\\"");
        // System.out.println(keyword);

        if (ValueUtility.isEmptyString(ripgrepExePath)) {
            throw new ControllerException("没有指定 ripgrep exe 路径");
        }

        var nodeMap = noteExploreService.getNoteMap();
        var notes = new ArrayList<NoteTreeNodeDto>();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ripgrepExePath,
                    "-F",   // 字面量搜索
                    "-l",   // 只列出文件
                    "-i",   // 忽略大小写
                    keyword,
                    noteExploreService.getNoteRootAbsPath()
            );

            Process process = pb.start();
            // System.out.println("执行命令: " + String.join(" ", pb.command()));
            process.getOutputStream().close();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(),
                            StandardCharsets.UTF_8    // Charset.forName("GBK")
                    )
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (nodeMap.containsKey(line)) {
                        notes.add(NoteTreeNodeDto.fromEntity(nodeMap.get(line)));
                    }
                }
            }

            return Result.okData(Map.of(
                    "list",
                    notes.stream().sorted(
                            java.util.Comparator.comparing(
                                    item -> item.getPath() == null ? "" : item.getPath(),
                                    String.CASE_INSENSITIVE_ORDER
                            )
                    ).toList()
            ));
        } catch (Exception ex) {
            throw new ControllerException(ex);
        }
    }

    /**
     * 搜索内容测试
     */
    @RequestMapping(value = "/api/search/content/test", method = RequestMethod.POST)
    public Result searchContentTest(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String keyword = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "keyword");
        keyword = keyword
                //.replace("\\", "\\\\")    // 无需转义 \
                .replace("\"", "\\\"")
        ;
        // System.out.println(keyword);

        if (ValueUtility.isEmptyString(ripgrepExePath)) {
            throw new ControllerException("没有指定 ripgrep exe 路径");
        }

        var result = new ArrayList<String>();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ripgrepExePath,
                    "-F",   // 字面量搜索
                    "-l",   // 只列出文件
                    "-i",   // 忽略大小写
                    // "--no-messages",
                    keyword,    // 查询参数两端无需添加双引号
                    "C:\\Program Files\\FileZilla Server"   // 路径两端无需添加双引号，路径结尾不能有 \
            );

            // 不合并 stderr 到 stdout
            // pb.redirectErrorStream(true);

            Process process = pb.start();
            System.out.println("执行命令: " + String.join(" ", pb.command()));
            process.getOutputStream().close();  // 避免其等待输入导致阻塞

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(),
                            StandardCharsets.UTF_8    // Charset.forName("GBK")
                    )
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(line);
                }
            }

            // 忽略错误
            // rg: C:\Program Files\FileZilla Server\Logs\filezilla-server.log: 拒绝访问。 (os error 5)
            // int exitCode = process.waitFor();
            // if (exitCode != 0) {
            //    throw new ControllerException("rg 执行失败，exitCode=" + exitCode);
            // }

            return Result.okData(Map.of(
                    "list",
                    result.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList()
            ));
        } catch (Exception ex) {
            throw new ControllerException(ex);
        }
    }

    // /**
    //  * 调用 Everything 搜索
    //  */
    // @RequestMapping(value = "/api/search/everything", method = RequestMethod.POST)
    // public Result searchEverything(@RequestBody String requestBody) {
    //     var requestJson = RequestValueHelper.getRequestJson(requestBody);
    //     String keyword = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "keyword");
    //     // System.out.println(keyword);
    //
    //     if (ValueUtility.isEmptyString(everythingEsExePath)) {
    //         throw new ControllerException("没有指定 Everything es exe 路径");
    //     }
    //
    //     // if (ValueUtility.isEmptyString(everythingEsOptPath)) {
    //     //     throw new ControllerException("没有指定 Everything es opt 路径");
    //     // }
    //
    //     var result = new ArrayList<String>();
    //     try {
    //         ProcessBuilder pb = new ProcessBuilder(
    //                 everythingEsExePath,
    //                 "-instance", "\"WildNoteInstance\"",
    //                 // "-path"
    //                 // everythingEsOptPath,    // 盘符需要以 \ 结尾，文件夹有没有 \ 都可以
    //                 keyword
    //         );
    //
    //         Process process = pb.start();
    //         // System.out.println("执行命令: " + String.join(" ", pb.command()));
    //         process.getOutputStream().close();
    //
    //         try (BufferedReader reader = new BufferedReader(
    //                 new InputStreamReader(process.getInputStream(),
    //                         // StandardCharsets.UTF_8
    //                         Charset.forName("GBK")
    //                 )
    //         )) {
    //             String line;
    //             while ((line = reader.readLine()) != null) {
    //                 result.add(line);
    //             }
    //         }
    //
    //         if (result.size() > 1000) {
    //             throw new ControllerException("搜索结果超过1000条，请缩小搜索范围");
    //         }
    //
    //         String downloadToken = UUID.randomUUID().toString();
    //         downloadTokenCache.put(downloadToken, downloadToken);
    //
    //         return Result.okData(Map.of(
    //                 "list",
    //                 result.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList(),
    //                 "downloadToken",
    //                 downloadToken
    //         ));
    //     } catch (Exception ex) {
    //         throw new ControllerException(ex);
    //     }
    // }

    /**
     * 调用 Everything 搜索
     */
    @RequestMapping(value = "/api/search/everything", method = RequestMethod.POST)
    public Result searchEverything(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        String keyword = RequestValueHelper.getRequestJsonStringTrimReq(requestJson, "keyword");
        // System.out.println(keyword);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:" + everythingHttpPort + "/?search=" + keyword + "&json=1&path_column=1&size_column=1&date_modified_column=1&sort=path";

        String response = restTemplate.getForObject(url, String.class);
        JsonNode responseJson = JsonUtility.deserialize(response);

        String downloadToken = UUID.randomUUID().toString();
        downloadTokenCache.put(downloadToken, downloadToken);

        return Result.okData(Map.of(
                "list", responseJson.get("results"),
                "downloadToken", downloadToken
        ));
    }

    // /**
    //  * Everything 搜索结果下载
    //  */
    // @RequestMapping(value = "/api/search/everything/download", method = RequestMethod.GET)
    // public void searchEverythingDownload(HttpServletRequest request, HttpServletResponse response) {
    //     String path = RequestValueHelper.getRequestParamStringTrimReq(request, "path");
    //     String token = RequestValueHelper.getRequestParamStringTrimReq(request, "token");
    //     String tokenCache = downloadTokenCache.getIfPresent(token);
    //         if (tokenCache == null) {
    //         throw new ControllerException("下载令牌已过期");
    //     }
    //     // System.out.println(path);
    //
    //     try {
    //         Path filePath = Paths.get(path).normalize();
    //
    //         if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
    //             throw new ControllerException("文件不存在");
    //         }
    //         if (!Files.isReadable(filePath)) {
    //             throw new ControllerException("文件不可读");
    //         }
    //
    //         String fileName = filePath.getFileName().toString();
    //         // String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    //         String contentDisposition = ContentDisposition.attachment()
    //                 .filename(fileName, StandardCharsets.UTF_8)
    //                 .build()
    //                 .toString();
    //         // response.setHeader("Content-Disposition",
    //         //         "attachment; filename=\"" + fileName.replace("\"", "\\\"") +
    //         //                 "\"; filename*=UTF-8''" + encodedFileName);
    //         response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    //         // System.out.println(response.getHeader("Content-Disposition"));
    //
    //         String contentType = Files.probeContentType(filePath);
    //         if (ValueUtility.isEmptyString(contentType)) {
    //             contentType = "application/octet-stream";
    //         }
    //         response.setContentType(contentType);
    //         response.setHeader("Content-Length", String.valueOf(Files.size(filePath)));
    //
    //         try (
    //                 var inputStream = new BufferedInputStream(Files.newInputStream(filePath));
    //                 var outputStream = response.getOutputStream()
    //         ) {
    //             byte[] buffer = new byte[8192];
    //             int len;
    //             while ((len = inputStream.read(buffer)) != -1) {
    //                 outputStream.write(buffer, 0, len);
    //             }
    //             outputStream.flush();
    //         }
    //     } catch (Exception ex) {
    //         throw new ControllerException(ex);
    //     }
    // }

    /**
     * Everything 搜索结果下载
     */
    @RequestMapping(value = "/api/search/everything/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> searchEverythingDownload(HttpServletRequest request) {
        String path = RequestValueHelper.getRequestParamStringTrimReq(request, "path");
        String token = RequestValueHelper.getRequestParamStringTrimReq(request, "token");
        String tokenCache = downloadTokenCache.getIfPresent(token);
        if (tokenCache == null) {
            throw new ControllerException("下载令牌已过期");
        }

        try {
            Path filePath = Paths.get(path).normalize();

            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                throw new ControllerException("文件不存在");
            }
            if (!Files.isReadable(filePath)) {
                throw new ControllerException("文件不可读");
            }

            HttpHeaders headers = new HttpHeaders();
            String fileName = filePath.getFileName().toString();
            // String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            // headers.set("Content-Disposition",
            //         "attachment; filename=\"" + fileName.replace("\"", "\\\"") +
            //                 "\"; filename*=UTF-8''" + encodedFileName);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(fileName, StandardCharsets.UTF_8)
                            .build()
            );
            // System.out.println(headers.get("Content-Disposition"));

            String contentType = Files.probeContentType(filePath);
            if (ValueUtility.isEmptyString(contentType)) {
                contentType = "application/octet-stream";
            }
            headers.set("Content-Type", contentType);

            // headers.set("Content-Length", String.valueOf(Files.size(filePath)));

            InputStreamResource resource = new InputStreamResource(
                    new BufferedInputStream(Files.newInputStream(filePath))
            );
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(Files.size(filePath))
                    .body(resource);
        } catch (Exception ex) {
            throw new ControllerException(ex);
        }
    }
}
