package cool.done.wildnote.server.adapter.driving;

import cool.done.wildnote.server.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

@Controller
public class ManifestController {

    private static final Logger logger = LoggerFactory.getLogger(ManifestController.class);

    /**
     * 应用清单 Manifest
     */
    @RequestMapping(value = "/manifest/manifest-{host}.json", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String manifestHostJson(@PathVariable String host) {
        return String.format("""
                {
                  "short_name": "%s",
                  "name": "",
                  "icons": [
                    {
                      "src": "img/logo192c.png",
                      "type": "image/png",
                      "sizes": "192x192"
                    },
                    {
                      "src": "img/logo512c.png",
                      "type": "image/png",
                      "sizes": "512x512"
                    }
                  ],
                  "start_url": ".",
                  "background_color": "#ffffff",
                  "display": "standalone",
                  "theme_color": "#001529"
                }
                """, host);
    }
}
