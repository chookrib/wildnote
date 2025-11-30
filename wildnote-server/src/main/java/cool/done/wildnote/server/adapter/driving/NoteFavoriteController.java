package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.application.NoteSettingService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 笔记收藏 Controller
 */
@RestController
public class NoteFavoriteController {

    private final NoteSettingService noteSettingService;

    public NoteFavoriteController(NoteSettingService noteSettingService) {
        this.noteSettingService = noteSettingService;
    }

    /**
     * 取收藏路径
     */
    @RequestMapping(value = "/api/favorite/get", method = RequestMethod.GET)
    public Result favoriteGet() {
        return Result.okData(Map.of("paths", noteSettingService.getFavorite()));
    }

    /**
     * 存收藏路径
     */
    @RequestMapping(value = "/api/favorite/set", method = RequestMethod.POST)
    public Result favoriteSet(@RequestBody String requestBody) {
        var requestJson = RequestValueHelper.getRequestJson(requestBody);
        List<String> paths = RequestValueHelper.getRequestJsonStringTrimList(requestJson, "paths");
        // System.out.println("paths=" + paths);
        noteSettingService.setFavorite(paths);
        return Result.ok();
    }
}
