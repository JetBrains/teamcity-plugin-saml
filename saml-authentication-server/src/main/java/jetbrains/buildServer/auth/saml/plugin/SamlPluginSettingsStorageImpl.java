package jetbrains.buildServer.auth.saml.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.IOGuard;
import lombok.Getter;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;

public class SamlPluginSettingsStorageImpl implements SamlPluginSettingsStorage {
    private final ObjectMapper objectMapper;

    @Getter
    private final Path configPath;

    public SamlPluginSettingsStorageImpl(Path configPath) {
        this.objectMapper = new ObjectMapper();
        this.configPath = configPath;
    }

    @Override
    public void init() throws IOException {
        load();
    }

    @Override
    public SamlPluginSettings load() throws IOException {
        if (!this.configPath.toFile().exists() || this.configPath.toFile().length() == 0) {
            save(new SamlPluginSettings());
        }

        try {
            SamlPluginSettings result = this.objectMapper.readValue(this.configPath.toFile(), SamlPluginSettings.class);

            // some clean-up of additional certs
            if (result.getAdditionalCerts().stream().allMatch(StringUtils::isEmpty)) {
                result.getAdditionalCerts().clear();
            }

            return result;
        } catch (RuntimeException ex) {
            Loggers.SERVER.error("Cannot load SAML plugin settings", ex);
            throw ex;
        }
    }

    @Override
    public void save(SamlPluginSettings settings) throws IOException {
        IOGuard.allowDiskWrite(() -> this.objectMapper.writeValue(this.configPath.toFile(), settings));
    }
}
