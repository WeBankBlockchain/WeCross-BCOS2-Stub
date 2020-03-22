package com.webank.wecross.stub.bcos.common;

import com.moandjiezana.toml.Toml;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class BCOSToml {
    private final String path;

    public BCOSToml(String path) throws IOException {
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
