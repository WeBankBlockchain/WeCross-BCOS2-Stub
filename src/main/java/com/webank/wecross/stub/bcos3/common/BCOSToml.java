package com.webank.wecross.stub.bcos3.common;

import com.moandjiezana.toml.Toml;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class BCOSToml {
    private final String path;

    public BCOSToml(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Toml getToml() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource(getPath());
        return new Toml().read(resource.getInputStream());
    }
}
