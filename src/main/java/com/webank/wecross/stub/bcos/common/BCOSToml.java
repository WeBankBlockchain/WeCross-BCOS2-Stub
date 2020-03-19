package com.webank.wecross.stub.bcos.common;

import com.moandjiezana.toml.Toml;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class BCOSToml {
    private final String path;
    private final Toml toml;

    public BCOSToml(String path) throws IOException {
        this.path = path;
        this.toml = getToml();
    }

    public String getPath() {
        return path;
    }

    public Toml getToml() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Path path = Paths.get(resolver.getResource(getPath()).getURI());
        String fileContent = new String(Files.readAllBytes(path));
        return new Toml().read(fileContent);
    }
}
