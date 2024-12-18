package cool.done.wildnotesvc.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ObjectMapper Service
 */
@Service
public class ObjectMapperService {

    private static ObjectMapper objectMapper;

    @Autowired
    private ObjectMapper autowiredObjectMapper;

    @PostConstruct
    public void init() {
        objectMapper = autowiredObjectMapper;
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
